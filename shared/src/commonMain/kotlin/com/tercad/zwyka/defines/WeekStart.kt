// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource

import com.tercad.zwyka.core.model.WeekStart
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.monday
import com.tercad.zwyka.resources.saturday
import com.tercad.zwyka.resources.sunday

val WeekStart.label: StringResource
    get() = when (this) {
        WeekStart.SATURDAY -> Res.string.saturday
        WeekStart.SUNDAY -> Res.string.sunday
        WeekStart.MONDAY -> Res.string.monday
    }

val weekStartOptions = WeekStart.entries
