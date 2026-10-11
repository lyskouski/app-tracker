// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.ui.graphics.Color

val habitColorOptions = listOf(
    "#EF5350", "#F44336", "#E53935", "#C62828",
    "#FF7043", "#FF5722", "#F4511E", "#D84315",
    "#FFA726", "#FF9800", "#FB8C00", "#EF6C00",
    "#FFEE58", "#FFEB3B", "#FDD835", "#F9A825",
    "#9CCC65", "#8BC34A", "#7CB342", "#558B2F",
    "#26A69A", "#009688", "#00897B", "#00695C",
)

fun normalizeHabitColor(value: String): String? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it.digitToIntOrNull(16) == null }) return null
    return "#${hex.uppercase()}"
}

fun habitColor(hex: String?): Color? {
    val normalized = hex?.let(::normalizeHabitColor) ?: return null
    val value = normalized.substring(1).toLongOrNull(16) ?: return null
    return Color(
        red = ((value shr 16) and 0xFF).toFloat() / 255f,
        green = ((value shr 8) and 0xFF).toFloat() / 255f,
        blue = (value and 0xFF).toFloat() / 255f,
    )
}
