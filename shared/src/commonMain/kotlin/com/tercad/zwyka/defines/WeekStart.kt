// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.monday
import com.tercad.zwyka.resources.saturday
import com.tercad.zwyka.resources.sunday

enum class WeekStart(val label: StringResource) {
    SATURDAY(Res.string.saturday),
    SUNDAY(Res.string.sunday),
    MONDAY(Res.string.monday),
}

val weekStartOptions = WeekStart.entries
