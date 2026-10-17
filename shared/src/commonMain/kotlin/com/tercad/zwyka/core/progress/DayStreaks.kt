// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.LocalDate

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.StatusMarks
import com.tercad.zwyka.core.model.isActiveOn

/** Sparse marks of the table, keyed by habit and date. */
fun List<DayState>.toMarks(): Map<CellKey, String> =
    associate { CellKey(habitId = it.habitId, date = it.date) to it.mark }

/*
 * Mark of a day: stored one, "—" for future inactive days, otherwise " ".
 */
fun displayedMark(
    habit: Habit,
    date: String,
    marks: Map<CellKey, String>,
    today: String?,
): String =
    marks[CellKey(habitId = habit.id, date = date)]
        ?: if (
            today != null && date > today &&
            !habit.isActiveOn(LocalDate.parse(date).dayOfWeek)
        ) INACTIVE_MARK else " "

/*
 * Day Streak = sequential days with a status mark.
 *
 * "—" is not counted but keeps the streak going.
 * The result is attached to the first free day after the streak.
 */
fun calculateStreaks(
    habits: List<Habit>,
    dates: List<String>,
    marks: Map<CellKey, String>,
    today: String?,
): Map<CellKey, Int> {
    val result = HashMap<CellKey, Int>()
    for (habit in habits) {
        var streak = 0
        for (date in dates) {
            val mark = displayedMark(habit, date, marks, today)
            when {
                StatusMarks.isStatus(mark) -> streak++
                mark == INACTIVE_MARK -> Unit
                else -> {
                    if (streak > 0 && mark.isBlank()) {
                        result[CellKey(habitId = habit.id, date = date)] = streak
                    }
                    streak = 0
                }
            }
        }
    }
    return result
}
