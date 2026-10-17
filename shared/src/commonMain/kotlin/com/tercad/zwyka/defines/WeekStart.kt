// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.monday
import com.tercad.zwyka.resources.saturday
import com.tercad.zwyka.resources.sunday

enum class WeekStart(val label: StringResource, val dayOfWeek: DayOfWeek) {
    SATURDAY(Res.string.saturday, DayOfWeek.SATURDAY),
    SUNDAY(Res.string.sunday, DayOfWeek.SUNDAY),
    MONDAY(Res.string.monday, DayOfWeek.MONDAY),
}

val weekStartOptions = WeekStart.entries
