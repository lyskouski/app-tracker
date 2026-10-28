// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlin.test.Test
import kotlin.test.assertEquals

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK

class TodayStatusTest {
    // 2026-10-28 is a Wednesday.
    private val today = "2026-10-28"

    @Test
    fun detectsStatusOfMark() {
        val habit = Habit(1, "Read")
        assertEquals(TodayStatus.NOT_DONE, todayStatus(habit, today, null))
        assertEquals(TodayStatus.NOT_DONE, todayStatus(habit, today, "/"))
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, "X"))
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, "▣"))
        assertEquals(TodayStatus.SKIPPED, todayStatus(habit, today, INACTIVE_MARK))
    }

    @Test
    fun skipsUnmarkedInactiveDay() {
        // Monday only.
        val habit = Habit(1, "Gym", activeDays = 0x01)
        assertEquals(TodayStatus.SKIPPED, todayStatus(habit, today, null))
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, "X"))
    }

    @Test
    fun comparesNumericValueWithTarget() {
        val habit = Habit(1, "Water", targetValue = 8.0, targetType = "glasses")
        assertEquals(TodayStatus.NOT_DONE, todayStatus(habit, today, "5"))
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, "8"))
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, "10"))
    }

    @Test
    fun ordersByStatusKeepingHabitOrder() {
        val habits = listOf(
            Habit(1, "Skipped"),
            Habit(2, "Done"),
            Habit(3, "Open"),
            Habit(4, "Archived", isArchived = true),
            Habit(5, "Open too"),
        )
        val marks = mapOf(
            CellKey(habitId = 1, date = today) to INACTIVE_MARK,
            CellKey(habitId = 2, date = today) to "X",
        )
        assertEquals(
            listOf(
                3L to TodayStatus.NOT_DONE,
                5L to TodayStatus.NOT_DONE,
                2L to TodayStatus.DONE,
                1L to TodayStatus.SKIPPED,
            ),
            groupByTodayStatus(habits, marks, today).map { (habit, status) -> habit.id to status },
        )
    }
}
