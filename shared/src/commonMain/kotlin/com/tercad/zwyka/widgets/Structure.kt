// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle

import com.tercad.zwyka.defines.currentColorScheme

/*
 * Title of a screen, announced when the screen is opened (WCAG 2.4.2).
 */
fun Modifier.screenTitle(title: String): Modifier = semantics { paneTitle = title }

/*
 * Title of a page or of its section: screen readers jump between headings (WCAG 1.3.1, 2.4.6).
 */
@Composable
fun Heading(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = currentColorScheme().onSurface,
) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = style,
        color = color,
    )
}

/*
 * Outcome of an action or a loading state, read out without moving the focus (WCAG 4.1.3).
 */
@Composable
fun StatusMessage(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = currentColorScheme().onSurface,
) {
    Text(
        text = text,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = style,
        color = color,
    )
}

/*
 * Failure to be noticed right away: shown in the error color and read out at once (WCAG 3.3.1, 4.1.3).
 */
@Composable
fun ErrorMessage(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text = text,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        style = style,
        color = currentColorScheme().error,
    )
}
