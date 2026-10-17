// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.infrastructures.Database

/** Seven dates of the week (per the configured week start) that contains [today]. */
fun currentWeekDates(today: LocalDate, weekStart: WeekStart): List<LocalDate> {
    val offset = (today.dayOfWeek.ordinal - weekStart.dayOfWeek.ordinal + 7) % 7
    val first = today.minus(offset, DateTimeUnit.DAY)
    return (0..6).map { first.plus(it, DateTimeUnit.DAY) }
}

/*
 * Propagates the habit's [activeDays] (already stored in Habit):
 *
 * - scheduled times of the habit get the same week days;
 * - inactive days of [weekDates] are marked with "—", reactivated ones are cleared.
 */
suspend fun applyHabitActiveDays(
    database: Database,
    habitId: Long,
    activeDays: Int,
    weekDates: List<LocalDate>,
) {
    val existingMarks = weekDates.associateWith { date ->
        database.databaseQueries
            .selectDayStatesForDateRange(date.toString(), date.toString())
            .awaitAsList()
            .firstOrNull { it.habitId == habitId }
            ?.mark
    }
    database.transaction {
        database.databaseQueries.updateHabitTimesActiveDays(activeDays.toLong(), habitId)
        weekDates.forEach { date ->
            val isActive = activeDays and (1 shl date.dayOfWeek.ordinal) != 0
            if (!isActive) {
                database.databaseQueries.upsertDayState(habitId, date.toString(), INACTIVE_MARK)
            } else if (existingMarks[date] == INACTIVE_MARK) {
                database.databaseQueries.deleteDayState(habitId, date.toString())
            }
        }
    }
}
