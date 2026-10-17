// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.time

import com.tercad.zwyka.core.model.MINUTES_PER_DAY

/** Formats minutes since midnight as "HH:MM". */
fun formatTime(minutes: Int): String =
    "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"

/** Parses "H", "HH:MM" or "H:MM" into minutes since midnight; "24:00" is the end of the day. */
fun parseTime(text: String): Int? {
    val parts = text.trim().split(':')
    if (parts.size > 2) return null
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = if (parts.size == 2) parts[1].toIntOrNull() ?: return null else 0
    if (hours !in 0..24 || minutes !in 0..59) return null
    return (hours * 60 + minutes).takeIf { it <= MINUTES_PER_DAY }
}
