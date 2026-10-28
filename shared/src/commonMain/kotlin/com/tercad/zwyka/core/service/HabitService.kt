// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation
import com.tercad.zwyka.core.repository.DayStateRepository
import com.tercad.zwyka.core.repository.HabitRepository
import com.tercad.zwyka.core.time.currentWeekDates
import com.tercad.zwyka.defines.WeekStart

class HabitService(
    private val habits: HabitRepository,
    private val dayStates: DayStateRepository,
    private val reminders: ReminderService,
    private val clock: Clock = Clock.System,
) {
    suspend fun getAll(): List<Habit> = habits.getAll()

    /**
     * Creates ([id] is null) or updates a habit.
     *
     * @return all habits after the change.
     */
    suspend fun save(id: Long?, draft: HabitDraft, weekStart: WeekStart): List<Habit> {
        val savedId = if (id == null) {
            habits.insert(draft)
        } else {
            habits.update(id, draft).let { id }
        }
        applyActiveDays(savedId, draft.activeDays, weekStart)
        // Habit title is shown in its reminders.
        reminders.sync()
        return habits.getAll()
    }

    suspend fun delete(id: Long) {
        habits.delete(id)
        reminders.sync()
    }

    /** Marks the habit as updated (its marks or scheduled times changed). */
    suspend fun touch(id: Long) = habits.touch(id)

    /** Changes only the week days of a habit. */
    suspend fun updateActiveDays(id: Long, activeDays: Int, weekStart: WeekStart) {
        habits.updateActiveDays(id, activeDays)
        applyActiveDays(id, activeDays, weekStart)
    }

    /*
     * Inactive days of the current week without a mark get "—" for every not archived habit;
     * existing marks are kept. Future inactive days are only displayed as "—" (see `displayedMark`),
     * so they have to be stored once their week comes, otherwise they turn into missed days.
     */
    suspend fun fillInactiveDays(weekStart: WeekStart) {
        val weekDates = currentWeekDates(clock.todayIn(TimeZone.currentSystemDefault()), weekStart)
        val marked = dayStates
            .getRange(weekDates.first().toString(), weekDates.last().toString())
            .mapTo(HashSet()) { it.habitId to it.date }
        habits.getAll().filterNot { it.isArchived }.forEach { habit ->
            weekDates
                .map { it.toString() }
                .filter { date ->
                    (habit.id to date) !in marked &&
                        !habit.isActiveOn(LocalDate.parse(date).dayOfWeek) &&
                        !habit.isBeforeCreation(date, null)
                }
                .forEach { dayStates.upsert(DayState(habit.id, it, INACTIVE_MARK)) }
        }
    }

    /*
     * Inactive days of the current week without a mark get "—",
     * reactivated "—" days are cleared; actual marks are kept.
     */
    private suspend fun applyActiveDays(habitId: Long, activeDays: Int, weekStart: WeekStart) {
        val weekDates = currentWeekDates(clock.todayIn(TimeZone.currentSystemDefault()), weekStart)
        val existingMarks = dayStates
            .getRange(weekDates.first().toString(), weekDates.last().toString())
            .filter { it.habitId == habitId }
            .associate { it.date to it.mark }
        weekDates.forEach { date ->
            val isActive = activeDays and (1 shl date.dayOfWeek.ordinal) != 0
            val mark = existingMarks[date.toString()]
            if (!isActive && mark == null) {
                dayStates.upsert(DayState(habitId, date.toString(), INACTIVE_MARK))
            } else if (isActive && mark == INACTIVE_MARK) {
                dayStates.delete(habitId, date.toString())
            }
        }
    }
}
