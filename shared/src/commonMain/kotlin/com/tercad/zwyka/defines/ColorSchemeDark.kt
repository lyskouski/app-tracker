// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme

public val ColorSchemeDark = darkColorScheme(
    // Light tint of the brand color: readable as text and outlines on dark surfaces (WCAG 1.4.3, 1.4.11).
    primary = Color(0xffe6a8e6),
    onPrimary = Color(0xff4a0e4a),
    // Brand color of the bars.
    primaryContainer = Color(0xff912391),
    onPrimaryContainer = Color(0xffffffff),
    secondary = Color(0xFFFFFBFE),
    background = Color(0xff000000),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF49454F),
    onSurface = Color(0xFFE6E1E5),
    outlineVariant = Color(0xFF49454F),
    inversePrimary = Color(0xff5d233c),
    inverseSurface = Color(0xFF434343),
    onSecondary = Color(0xFF434343),
    onSecondaryContainer = Color(0xFF434343),
)
