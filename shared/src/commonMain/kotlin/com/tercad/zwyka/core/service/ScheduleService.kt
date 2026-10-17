// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.HabitTimeDraft
import com.tercad.zwyka.core.model.HabitTimeOwner
import com.tercad.zwyka.core.model.Intent
import com.tercad.zwyka.core.model.WeekStart
import com.tercad.zwyka.core.repository.HabitTimeRepository
import com.tercad.zwyka.core.repository.IntentRepository

/** Everything shown by the calendar. */
data class Schedule(
    val habits: List<Habit>,
    val intents: List<Intent>,
    val habitTimes: List<HabitTime>,
)

/*
 * Scheduled time ranges of habits and intents.
 *
 * Week days of a habit / intent are the union of the days of its time ranges.
 */
class ScheduleService(
    private val habitTimes: HabitTimeRepository,
    private val intents: IntentRepository,
    private val habitService: HabitService,
    private val reminders: ReminderService,
) {
    suspend fun load(): Schedule = Schedule(
        habits = habitService.getAll(),
        intents = intents.getAll(),
        habitTimes = habitTimes.getAll(),
    )

    /**
     * Creates ([existing] is null) or updates a time range; a new intent is created on demand.
     *
     * @return the schedule after the change.
     */
    suspend fun save(existing: HabitTime?, draft: HabitTimeDraft, weekStart: WeekStart): Schedule {
        var habitId: Long? = null
        var intentId: Long? = null
        when (val owner = draft.owner) {
            is HabitTimeOwner.OfHabit -> habitId = owner.habitId
            is HabitTimeOwner.OfIntent -> {
                intentId = owner.intentId
                    ?.also { intents.updateTitle(it, owner.title) }
                    ?: intents.insert(owner.title, draft.activeDays)
            }
        }
        val habitTime = HabitTime(
            id = existing?.id ?: 0,
            habitId = habitId,
            intentId = intentId,
            timeStart = draft.timeStart,
            timeEnd = draft.timeEnd,
            note = draft.note,
            activeDays = draft.activeDays,
            notify = draft.notify,
        )
        if (existing == null) habitTimes.insert(habitTime) else habitTimes.update(habitTime)
        return afterChange(
            habitIds = setOfNotNull(habitId, existing?.habitId),
            intentIds = setOfNotNull(intentId, existing?.intentId),
            weekStart = weekStart,
        )
    }

    /** @return the schedule after the change. */
    suspend fun delete(habitTime: HabitTime, weekStart: WeekStart): Schedule {
        habitTimes.delete(habitTime.id)
        return afterChange(
            habitIds = setOfNotNull(habitTime.habitId),
            intentIds = setOfNotNull(habitTime.intentId),
            weekStart = weekStart,
        )
    }

    private suspend fun afterChange(
        habitIds: Set<Long>,
        intentIds: Set<Long>,
        weekStart: WeekStart,
    ): Schedule {
        accumulateActiveDays(habitIds, intentIds, weekStart)
        // Intents exist only while they are scheduled.
        intents.deleteUnused()
        reminders.sync()
        return load()
    }

    /*
     * Week days of a habit / intent = union of the days of all its time ranges.
     * Owners without time ranges keep their days.
     */
    private suspend fun accumulateActiveDays(
        habitIds: Set<Long>,
        intentIds: Set<Long>,
        weekStart: WeekStart,
    ) {
        val all = habitTimes.getAll()
        val habits = habitService.getAll()
        fun union(times: List<HabitTime>) = times.fold(0) { acc, it -> acc or it.activeDays }
        habitIds.forEach { habitId ->
            val days = union(all.filter { it.habitId == habitId })
            if (days != 0 && days != habits.firstOrNull { it.id == habitId }?.activeDays) {
                habitService.updateActiveDays(habitId, days, weekStart)
            }
        }
        intentIds.forEach { intentId ->
            val days = union(all.filter { it.intentId == intentId })
            if (days != 0) intents.updateActiveDays(intentId, days)
        }
    }
}
