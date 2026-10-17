// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import kotlin.time.Clock
import kotlin.time.Instant

import com.tercad.zwyka.core.notifications.CatchUpReminderScheduler
import com.tercad.zwyka.core.notifications.Reminder
import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.core.notifications.lastTriggerBetween
import com.tercad.zwyka.core.repository.HabitRepository
import com.tercad.zwyka.core.repository.HabitTimeRepository
import com.tercad.zwyka.core.repository.IntentRepository
import com.tercad.zwyka.core.repository.SettingsRepository
import com.tercad.zwyka.core.time.formatTime

private const val REMINDERS_SHOWN_KEY = "remindersShownAt"

/*
 * Keeps platform reminders in line with the scheduled times that have notification enabled.
 */
class ReminderService(
    private val habits: HabitRepository,
    private val intents: IntentRepository,
    private val habitTimes: HabitTimeRepository,
    private val settings: SettingsRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    private var caughtUp = false

    fun requestPermission() = scheduler.requestPermission()

    /** Rebuilds all reminders; call after any change of habits, intents or scheduled times. */
    suspend fun sync() {
        val reminders = buildReminders()
        (scheduler as? CatchUpReminderScheduler)?.let { catchUp(it, reminders) }
        scheduler.schedule(reminders)
    }

    private suspend fun buildReminders(): List<Reminder> {
        val habitTitles = habits.getAll().associate { it.id to it.title }
        val intentTitles = intents.getAll().associate { it.id to it.title }
        return habitTimes.getAll()
            .filter { it.notify }
            .mapNotNull { habitTime ->
                val title = habitTime.habitId?.let { habitTitles[it] }
                    ?: habitTime.intentId?.let { intentTitles[it] }
                    ?: return@mapNotNull null
                val range = "${formatTime(habitTime.timeStart)} – ${formatTime(habitTime.timeEnd)}"
                Reminder(
                    id = habitTime.id,
                    title = title,
                    text = listOfNotNull(range, habitTime.note).joinToString("\n"),
                    minuteOfDay = habitTime.timeStart,
                    activeDays = habitTime.activeDays,
                )
            }
    }

    /*
     * Every reminder due while the application was running has been shown, so
     * the moment of the last shown one (or of the application start) bounds
     * the reminders missed while it was closed. Done once per application run.
     */
    private suspend fun catchUp(scheduler: CatchUpReminderScheduler, reminders: List<Reminder>) {
        if (caughtUp) return
        caughtUp = true
        val now = clock.now()
        val since = settings.getAll()[REMINDERS_SHOWN_KEY]
            ?.toLongOrNull()
            ?.let(Instant::fromEpochMilliseconds)
        if (since != null) {
            scheduler.showNow(reminders.filter { it.lastTriggerBetween(since, now) != null })
        }
        markShown(now)
        scheduler.onShown = ::markShown
    }

    private suspend fun markShown(moment: Instant) {
        settings.put(REMINDERS_SHOWN_KEY, moment.toEpochMilliseconds().toString())
    }
}
