// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

import com.tercad.zwyka.defines.CompletenessType
import com.tercad.zwyka.defines.statusOptions

private const val OVER_100_MARK = "▣"

/* Fill of the colored rectangle per status, from "0% start" to "100%". */
private val statusAlphas = listOf(0.1f, 0.25f, 0.5f, 0.75f, 1f)

/**
 * Renders a status mark; "▣" is drawn because the web build has no font glyph for it.
 *
 * [CompletenessType.COLORS] draws status marks as rectangles filled with [color].
 */
@Composable
fun StatusMark(
    mark: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    type: CompletenessType = CompletenessType.SYMBOLS,
) {
    val colored = type == CompletenessType.COLORS
    val alpha = statusAlphas.getOrNull(statusOptions.indexOfFirst { it.mark == mark })
    if (colored && alpha != null) {
        Canvas(modifier.size(18.dp)) {
            drawCircle(color = color.copy(alpha = alpha))
        }
        return
    }
    if (mark != OVER_100_MARK) {
        Text(text = mark, modifier = modifier, color = color, style = style)
        return
    }
    Canvas(modifier.size(if (colored) 18.dp else 14.dp)) {
        val stroke = 1.5.dp.toPx()
        drawRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke),
        )
        val inset = size.width * 0.3f
        drawRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - 2 * inset, size.height - 2 * inset),
        )
    }
}
