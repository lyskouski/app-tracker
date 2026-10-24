// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.progress

import kotlinx.datetime.LocalDate

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation
import com.tercad.zwyka.defines.isStatusMark
import com.tercad.zwyka.defines.statusProgress

/*
 * Progress of all habits applicable on [date], 0..1, or null if none applies.
 *
 * Each status mark contributes by its percentage ("·" = 0%, "X" = 100%),
 * values over 100% are capped. Habits marked with "—", unmarked inactive
 * days, days before the habit creation and archived habits are skipped.
 */
fun calculateDayProgress(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    date: String?,
): Float? {
    if (date == null) return null
    val dayOfWeek = LocalDate.parse(date).dayOfWeek
    val values = habits.mapNotNull { habit ->
        val mark = marks[CellKey(habitId = habit.id, date = date)]
        when {
            habit.isArchived -> null
            mark == INACTIVE_MARK -> null
            mark == null && !habit.isActiveOn(dayOfWeek) -> null
            habit.isBeforeCreation(date, mark) -> null
            else -> statusProgress(mark)
        }
    }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

/** Daily progress of [dates], see [calculateDayProgress]. */
fun calculateProgressHistory(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    dates: List<String>,
): List<Float?> = dates.map { calculateDayProgress(habits, marks, it) }
