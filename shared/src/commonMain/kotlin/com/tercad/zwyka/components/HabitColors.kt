// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.ui.graphics.Color

val habitColors = listOf(
    "#E53935",
    "#FB8C00",
    "#FDD835",
    "#43A047",
    "#00ACC1",
    "#1E88E5",
    "#5E35B1",
    "#D81B60",
)

fun habitColor(hex: String?): Color? {
    if (hex == null || hex.length != 7 || hex[0] != '#') return null
    val value = hex.substring(1).toLongOrNull(16) ?: return null
    return Color(
        red = ((value shr 16) and 0xFF).toFloat() / 255f,
        green = ((value shr 8) and 0xFF).toFloat() / 255f,
        blue = (value and 0xFF).toFloat() / 255f,
    )
}
