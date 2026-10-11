// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

data class DayState(
    val habitId: Long,
    val date: String,
    val mark: String
)

val dayStateMarks = listOf("·", "\\", "|", "/", "X", "▣")
