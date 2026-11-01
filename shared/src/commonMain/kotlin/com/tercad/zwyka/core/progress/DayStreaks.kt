// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.createdDate
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation

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

/** Day of a streak: a progress mark (see [isProgressMark]), or no mark for a habit to avoid. */
fun isStreakMark(habit: Habit, mark: String?): Boolean =
    if (habit.isNegative) mark.isNullOrBlank() else isProgressMark(habit, mark)

/*
 * Day Streak = sequential days with a progress mark (see [isProgressMark]).
 *
 * "—" is not counted but keeps the streak going.
 * The result is attached to the first free day after the streak.
 * Habits to avoid are counted by [calculateNegativeStreak].
 */
fun calculateStreaks(
    habits: List<Habit>,
    dates: List<String>,
    marks: Map<CellKey, String>,
    today: String?,
): Map<CellKey, Int> {
    val result = HashMap<CellKey, Int>()
    for (habit in habits) {
        if (habit.isNegative) {
            if (!habit.isArchived && today != null) calculateNegativeStreak(habit, dates, marks, today, result)
            continue
        }
        var streak = 0
        for (date in dates) {
            val mark = displayedMark(habit, date, marks, today)
            when {
                isProgressMark(habit, mark) -> streak++
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

/*
 * Day Streak of a habit to avoid = sequential days without a mark till [today],
 * days before the habit creation are not counted.
 *
 * "—" is not counted but keeps the streak going.
 * The result is attached to the last day of the streak, it is free as well.
 */
private fun calculateNegativeStreak(
    habit: Habit,
    dates: List<String>,
    marks: Map<CellKey, String>,
    today: String,
    result: MutableMap<CellKey, Int>,
) {
    var streak = 0
    var lastKey: CellKey? = null
    for (date in dates) {
        if (date > today) break
        val key = CellKey(habitId = habit.id, date = date)
        val mark = marks[key]
        when {
            habit.isBeforeCreation(date, mark) || mark == INACTIVE_MARK -> Unit
            isStreakMark(habit, mark) -> {
                streak++
                lastKey = key
            }
            else -> {
                lastKey?.let { result[it] = streak }
                streak = 0
                lastKey = null
            }
        }
    }
    lastKey?.let { result[it] = streak }
}

data class StreakSummary(val max: Int, val current: Int)

/*
 * Longest and current Day Streak of [habit] over all its marks ([marks] by ISO date).
 *
 * Same rules as [calculateStreaks]; a not yet marked [today] keeps the current streak.
 * A habit to avoid is counted from its creation, a not marked [today] is a day of its streak.
 */
fun calculateStreakSummary(habit: Habit, marks: Map<String, String>, today: LocalDate): StreakSummary {
    val created = habit.createdDate().takeIf { habit.isNegative && it.isNotEmpty() }
    var date = (marks.keys + listOfNotNull(created)).minOrNull()?.let { LocalDate.parse(it) }
        ?: return StreakSummary(0, 0)
    var streak = 0
    var max = 0
    while (date <= today) {
        val mark = marks[date.toString()]
        when {
            isStreakMark(habit, mark) -> max = maxOf(max, ++streak)
            mark == INACTIVE_MARK || (date == today && !habit.isNegative) -> Unit
            else -> streak = 0
        }
        date = date.plus(1, DateTimeUnit.DAY)
    }
    return StreakSummary(max, streak)
}
