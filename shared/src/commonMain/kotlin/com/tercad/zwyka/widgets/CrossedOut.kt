// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

import com.tercad.zwyka.defines.currentColorScheme

/*
 * Draws a crossed circle over the content (if [crossed]): the icon of a habit to avoid.
 * The error color of the scheme contrasts with the background in both light and dark modes.
 */
@Composable
fun Modifier.crossedOut(crossed: Boolean): Modifier {
    if (!crossed) return this
    val color = currentColorScheme().error
    return drawWithContent {
        drawContent()
        val stroke = 1.dp.toPx()
        // 1.dp out of the bounds on each side, so the icon keeps some space inside.
        val radius = (size.minDimension - stroke) / 2 + 1.dp.toPx()
        drawCircle(color, radius, style = Stroke(width = stroke))
        // Diagonal from the top left to the bottom right, at 45°.
        val offset = Offset(radius, radius) * 0.7071f
        drawLine(color, center - offset, center + offset, stroke)
    }
}
