// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** WCAG 1.4.3: minimal contrast of a regular text. */
const val TEXT_CONTRAST = 4.5f

/** WCAG 1.4.11: minimal contrast of graphics and borders of controls. */
const val GRAPHICS_CONTRAST = 3f

/** WCAG contrast ratio of [foreground] over [background] (taken as opaque), from 1 to 21. */
fun contrastRatio(foreground: Color, background: Color): Float {
    val opaqueBackground = background.copy(alpha = 1f)
    val first = foreground.compositeOver(opaqueBackground).luminance()
    val second = opaqueBackground.luminance()
    return (maxOf(first, second) + 0.05f) / (minOf(first, second) + 0.05f)
}

/*
 * A user color (e.g. of a habit) shown as text or as a mark over the opaque [background]:
 * kept as is when readable, otherwise darkened or lightened just enough to reach [minRatio].
 */
fun Color.readableOn(background: Color, minRatio: Float = TEXT_CONTRAST): Color {
    if (contrastRatio(this, background) >= minRatio) return this
    val opaque = compositeOver(background.copy(alpha = 1f))
    // Above this luminance black contrasts more than white.
    val target = if (background.luminance() > 0.179f) Color.Black else Color.White
    // Few steps are enough: the color is only seen, not compared.
    for (step in 1..10) {
        val candidate = lerp(opaque, target, step / 10f)
        if (contrastRatio(candidate, background) >= minRatio) return candidate
    }
    return target
}
