// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

import com.tercad.zwyka.defines.statusOptions

/*
 * Progress of all habits applicable on [date], 0..1, or null if none applies.
 *
 * Each status mark contributes by its percentage ("·" = 0%, "X" = 100%),
 * values over 100% are capped. Habits marked with "—", unmarked inactive
 * days and days before the habit creation are skipped.
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
            mark == INACTIVE_MARK -> null
            mark == null && !habit.isActiveOn(dayOfWeek) -> null
            mark == null && date < habit.createdDate() -> null
            else -> {
                val index = statusOptions.indexOfFirst { it.mark == mark }
                (index.coerceAtLeast(0) * 0.25f).coerceAtMost(1f)
            }
        }
    }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

private fun Habit.createdDate(): String =
    if (createdAt <= 0) {
        ""
    } else {
        Instant.fromEpochMilliseconds(createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
