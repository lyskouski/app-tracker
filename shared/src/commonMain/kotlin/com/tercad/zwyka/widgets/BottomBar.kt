// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back

/** Height of [BottomBar] at the default font size. */
private val BottomBarHeight = 46.dp

/*
 * [<- Back]                 [Settings]
 *
 * Bar of the page navigation in the brand color. It grows with the font size (WCAG 1.4.4).
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(currentColorScheme().primaryContainer)
            // Above the system navigation (Android draws the app edge-to-edge)
            .navigationBarsPadding()
            .heightIn(min = BottomBarHeight),
        content = content,
    )
}

/*
 * Button of [BottomBar]: the icon is decorative, the [text] names the action.
 */
@Composable
fun BottomBarButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = currentColorScheme().onPrimaryContainer,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

/*
 * "Back" button of [BottomBar], also pressed by Backspace (WCAG 2.1.1).
 */
@Composable
fun BottomBarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenShortcuts { event ->
        event.isBack.also { if (it) onClick() }
    }
    BottomBarButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        text = stringResource(Res.string.back),
        onClick = onClick,
        modifier = modifier,
    )
}
