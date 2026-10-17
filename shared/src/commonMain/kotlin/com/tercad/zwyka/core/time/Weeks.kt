// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

import com.tercad.zwyka.core.model.WeekStart

/** Seven dates of the week (per the configured week start) that contains [today]. */
fun currentWeekDates(today: LocalDate, weekStart: WeekStart): List<LocalDate> {
    val offset = (today.dayOfWeek.ordinal - weekStart.dayOfWeek.ordinal + 7) % 7
    val first = today.minus(offset, DateTimeUnit.DAY)
    return (0..6).map { first.plus(it, DateTimeUnit.DAY) }
}
