// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

/**
 * Scheduled time range of a habit or of an intent (exactly one of [habitId] / [intentId] is set),
 * repeated on [activeDays] (same bitmask as [Habit.activeDays]).
 */
data class HabitTime(
    val id: Long,
    val habitId: Long?,
    val intentId: Long? = null,
    /** Minutes since midnight. */
    val timeStart: Int,
    /** Minutes since midnight, up to [MINUTES_PER_DAY]. */
    val timeEnd: Int,
    val note: String? = null,
    val activeDays: Int = ALL_DAYS,
    /** Notify the user at [timeStart]. */
    val notify: Boolean = false,
)

const val MINUTES_PER_DAY = 24 * 60

fun HabitTime.isActiveOn(dayOfWeek: kotlinx.datetime.DayOfWeek): Boolean =
    activeDays and (1 shl dayOfWeek.ordinal) != 0
