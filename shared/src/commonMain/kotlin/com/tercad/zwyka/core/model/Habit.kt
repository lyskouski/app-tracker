// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class Habit(
    val id: Long,
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val remarks: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val activeDays: Int = ALL_DAYS,
    /** Deactivated: shown last in the calendar, without marks. */
    val isArchived: Boolean = false,
)

/** Bitmask of active weekdays: bit 0 is Monday ... bit 6 is Sunday. */
const val ALL_DAYS = 0x7F
const val INACTIVE_MARK = "—"

fun Habit.isActiveOn(dayOfWeek: kotlinx.datetime.DayOfWeek): Boolean =
    activeDays and (1 shl dayOfWeek.ordinal) != 0

/** ISO date of the habit creation, or "" when unknown. */
fun Habit.createdDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    if (createdAt <= 0) {
        ""
    } else {
        Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(timeZone).date.toString()
    }

/** True for a not marked [date] before the habit creation. */
fun Habit.isBeforeCreation(date: String, mark: String?): Boolean =
    mark == null && date < createdDate()
