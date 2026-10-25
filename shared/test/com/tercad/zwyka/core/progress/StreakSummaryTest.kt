// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK

class StreakSummaryTest {
    private val today = LocalDate.parse("2026-10-25")

    @Test
    fun countsLongestAndCurrentStreaks() {
        val marks = mapOf(
            "2026-10-10" to "X",
            "2026-10-11" to "|",
            "2026-10-12" to INACTIVE_MARK,
            "2026-10-13" to "·",
            // 2026-10-14 is not marked: the streak breaks.
            "2026-10-23" to "X",
            "2026-10-24" to "X",
        )
        assertEquals(StreakSummary(max = 3, current = 2), calculateStreakSummary(Habit(1, "Read"), marks, today))
    }

    @Test
    fun countsNumericValuesOfTarget() {
        val habit = Habit(1, "Walk", targetValue = 10000.0, targetType = "steps")
        val marks = mapOf("2026-10-24" to "2000", "2026-10-25" to "3500")
        assertEquals(StreakSummary(max = 2, current = 2), calculateStreakSummary(habit, marks, today))
    }

    @Test
    fun hasNoStreakWithoutMarks() {
        assertEquals(StreakSummary(0, 0), calculateStreakSummary(Habit(1, "Read"), emptyMap(), today))
    }
}
