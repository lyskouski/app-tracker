// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK

class NegativeHabitTest {
    private val habit = Habit(1, "Sweets", isNegative = true)

    @Test
    fun invertsProgressOfMark() {
        assertEquals(1f, markProgress(habit, null))
        assertEquals(1f, markProgress(habit, " "))
        assertEquals(1f, markProgress(habit, "·"))
        assertEquals(0.75f, markProgress(habit, "\\"))
        assertEquals(0.5f, markProgress(habit, "|"))
        assertEquals(0f, markProgress(habit, "X"))
        assertEquals(0.75f, markProgress(habit.copy(targetValue = 8.0), "2"))
    }

    @Test
    fun isDoneWhileNotMarked() {
        val today = "2026-10-28"
        assertEquals(TodayStatus.DONE, todayStatus(habit, today, null))
        assertEquals(TodayStatus.NOT_DONE, todayStatus(habit, today, "/"))
        assertEquals(TodayStatus.SKIPPED, todayStatus(habit, today, INACTIVE_MARK))
    }

    @Test
    fun countsStreaksOfEmptyDaysTillToday() {
        val dates = (20..28).map { "2026-10-$it" }
        val marks = mapOf(
            CellKey(1, "2026-10-22") to "X",
            CellKey(1, "2026-10-24") to INACTIVE_MARK,
        )
        assertEquals(
            mapOf(CellKey(1, "2026-10-21") to 2, CellKey(1, "2026-10-26") to 3),
            calculateStreaks(listOf(habit), dates, marks, today = "2026-10-26"),
        )
    }

    @Test
    fun countsStreakSummaryFromCreation() {
        val today = LocalDate.parse("2026-10-25")
        val createdAt = LocalDate.parse("2026-10-20").atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        val created = habit.copy(createdAt = createdAt)
        assertEquals(StreakSummary(max = 6, current = 6), calculateStreakSummary(created, emptyMap(), today))
        assertEquals(
            StreakSummary(max = 3, current = 2),
            calculateStreakSummary(created, mapOf("2026-10-23" to "|"), today),
        )
        assertEquals(
            StreakSummary(max = 5, current = 0),
            calculateStreakSummary(created, mapOf("2026-10-25" to "X"), today),
        )
    }
}
