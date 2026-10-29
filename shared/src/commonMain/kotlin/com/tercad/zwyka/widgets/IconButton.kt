// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp

/*
 * [icon] on a filled square of [color], a button set apart from the rest of its row;
 * without an [icon] a "?" is shown.
 *
 * WCAG 1.4.11: [color] is expected to reach the graphics contrast over the row,
 * the content is adjusted to be readable over [color].
 * WCAG 2.5.5: the touch target is at least 48.dp.
 */
@Composable
fun IconButton(
    icon: ImageVector?,
    color: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
) {
    val contentColor = Color.White.readableOn(color, TEXT_CONTRAST)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (icon == null) {
            Text("?", color = contentColor)
        } else {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        }
    }
}
