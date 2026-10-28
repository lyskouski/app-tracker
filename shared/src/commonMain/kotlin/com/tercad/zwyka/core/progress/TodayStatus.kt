// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.LocalDate

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation

/** Groups of the "Today" view, in the shown order. */
enum class TodayStatus {
    NOT_DONE,
    DONE,
    SKIPPED,
}

/*
 * Status of [habit] on [date]: "—" and unmarked inactive days are skipped,
 * a progress of 100% (or a reached numeric target) is done.
 */
fun todayStatus(habit: Habit, date: String, mark: String?): TodayStatus = when {
    mark == INACTIVE_MARK -> TodayStatus.SKIPPED
    mark == null && !habit.isActiveOn(LocalDate.parse(date).dayOfWeek) -> TodayStatus.SKIPPED
    isProgressMark(habit, mark) && markProgress(habit, mark) >= 1f -> TodayStatus.DONE
    else -> TodayStatus.NOT_DONE
}

/*
 * Habits of [date] ordered by [TodayStatus], the order of [habits] is kept within a group.
 * Archived habits and habits created after [date] are left out.
 */
fun groupByTodayStatus(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    date: String,
): List<Pair<Habit, TodayStatus>> =
    habits
        .filterNot { habit ->
            habit.isArchived ||
                habit.isBeforeCreation(date, marks[CellKey(habitId = habit.id, date = date)])
        }
        .map { habit -> habit to todayStatus(habit, date, marks[CellKey(habitId = habit.id, date = date)]) }
        .sortedBy { it.second.ordinal }
