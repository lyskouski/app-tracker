// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

data class Habit(
    val id: Long,
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val remarks: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val activeDays: Int = ALL_DAYS,
)

/** Bitmask of active weekdays: bit 0 is Monday ... bit 6 is Sunday. */
const val ALL_DAYS = 0x7F
const val INACTIVE_MARK = "—"

fun Habit.isActiveOn(dayOfWeek: kotlinx.datetime.DayOfWeek): Boolean =
    activeDays and (1 shl dayOfWeek.ordinal) != 0
