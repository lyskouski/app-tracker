// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.notifications

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

import com.tercad.zwyka.core.model.ALL_DAYS

/*
 * Weekly reminder: shown at [minuteOfDay] on every day of [activeDays]
 * (bit 0 is Monday ... bit 6 is Sunday).
 */
data class Reminder(
    val id: Long,
    val title: String,
    val text: String,
    val minuteOfDay: Int,
    val activeDays: Int,
)

/*
 * Platform notifications (Android, iOS, Desktop, Web).
 */
interface ReminderScheduler {
    /** Replaces all scheduled reminders with [reminders]. */
    fun schedule(reminders: List<Reminder>)

    /** Asks for the permission to show notifications, where the platform requires it. */
    fun requestPermission() {}
}

/*
 * Scheduler that delivers reminders only while the application runs:
 * reminders missed meanwhile are shown when it is opened again.
 */
interface CatchUpReminderScheduler : ReminderScheduler {
    /** Shows [reminders] at once. */
    fun showNow(reminders: List<Reminder>)

    /** Called with the moment of each reminder shown on time. */
    var onShown: suspend (Instant) -> Unit
}

object NoReminderScheduler : ReminderScheduler {
    override fun schedule(reminders: List<Reminder>) = Unit
}

/** Next moment (strictly after [now]) the reminder is due, or null without active days. */
fun Reminder.nextTrigger(
    now: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant? {
    if (activeDays and ALL_DAYS == 0) return null
    val today = now.toLocalDateTime(timeZone).date
    val time = LocalTime(minuteOfDay / 60, minuteOfDay % 60)
    for (offset in 0..7) {
        val date = today.plus(offset, DateTimeUnit.DAY)
        if (activeDays and (1 shl date.dayOfWeek.ordinal) == 0) continue
        val trigger = LocalDateTime(date, time).toInstant(timeZone)
        if (trigger > now) return trigger
    }
    return null
}

/** Latest occurrence of the reminder within (since, now], or null. */
fun Reminder.lastTriggerBetween(since: Instant, now: Instant): Instant? {
    // Weekly reminders: a week back covers every occurrence.
    var cursor = maxOf(since, now - 7.days)
    var last: Instant? = null
    while (true) {
        val trigger = nextTrigger(cursor) ?: break
        if (trigger > now) break
        last = trigger
        cursor = trigger
    }
    return last
}

/*
 * Reminders kept by the running application (Desktop, Web).
 */
class InProcessReminderScheduler(
    private val scope: CoroutineScope,
    private val show: (Reminder) -> Unit,
) : CatchUpReminderScheduler {
    private var job: Job? = null

    override var onShown: suspend (Instant) -> Unit = {}

    override fun showNow(reminders: List<Reminder>) = reminders.forEach(show)

    override fun schedule(reminders: List<Reminder>) {
        job?.cancel()
        job = scope.launch {
            reminders.forEach { reminder ->
                launch {
                    while (true) {
                        val trigger = reminder.nextTrigger(Clock.System.now()) ?: break
                        // Short steps follow wall-clock changes (sleep mode, time zone).
                        while (true) {
                            val left = trigger - Clock.System.now()
                            if (left <= Duration.ZERO) break
                            delay(minOf(left, 1.minutes))
                        }
                        show(reminder)
                        onShown(trigger)
                    }
                }
            }
        }
    }
}
