// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.notifications

import androidx.compose.runtime.staticCompositionLocalOf
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlin.time.Clock
import kotlin.time.Duration
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

import com.tercad.zwyka.data.ALL_DAYS
import com.tercad.zwyka.data.formatTime
import com.tercad.zwyka.infrastructures.Database

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

object NoReminderScheduler : ReminderScheduler {
    override fun schedule(reminders: List<Reminder>) = Unit
}

val LocalReminderScheduler = staticCompositionLocalOf<ReminderScheduler> { NoReminderScheduler }

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

/*
 * Reminders kept by the running application (Desktop, Web): nothing is shown
 * while the application is closed.
 */
class InProcessReminderScheduler(
    private val scope: CoroutineScope,
    private val show: (Reminder) -> Unit,
) : ReminderScheduler {
    private var job: Job? = null

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
                    }
                }
            }
        }
    }
}

/*
 * Rebuilds reminders of all HabitTime with enabled notification.
 */
suspend fun syncReminders(database: Database?, scheduler: ReminderScheduler) {
    if (database == null || scheduler === NoReminderScheduler) return
    val queries = database.databaseQueries
    val habits = queries.selectAllHabits().awaitAsList().associate { it.id to it.title }
    val intents = queries.selectAllIntents().awaitAsList().associate { it.id to it.title }
    val reminders = queries.selectAllHabitTimes().awaitAsList()
        .filter { it.notify != 0L }
        .mapNotNull { habitTime ->
            val title = habitTime.habitId?.let { habits[it] }
                ?: habitTime.intentId?.let { intents[it] }
                ?: return@mapNotNull null
            val range = "${formatTime(habitTime.timeStart.toInt())} – ${formatTime(habitTime.timeEnd.toInt())}"
            Reminder(
                id = habitTime.id,
                title = title,
                text = listOfNotNull(range, habitTime.note).joinToString("\n"),
                minuteOfDay = habitTime.timeStart.toInt(),
                activeDays = habitTime.activeDays.toInt(),
            )
        }
    scheduler.schedule(reminders)
}
