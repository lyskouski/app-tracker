// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.defines.HomeView
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.homeViewOptions

/*
 * Browser-like tabs: the selected one merges with the page below,
 * the others stay on the strip, separated by thin lines.
 *
 * | [▣ Today] | [▥ Weekly] |  ▦ Overall  |
 */
@Composable
fun HomeViewTabs(
    selected: HomeView,
    onSelect: (HomeView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(currentColorScheme().surfaceVariant)
            .statusBarsPadding()
            .padding(start = 4.dp, end = 4.dp, top = 6.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        homeViewOptions.forEachIndexed { index, view ->
            val isSelected = view == selected
            // No separator next to the selected tab, as its background already divides.
            if (index > 0 && !isSelected && homeViewOptions[index - 1] != selected) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(currentColorScheme().outline),
                )
            }
            val contentColor = if (isSelected) {
                currentColorScheme().onBackground
            } else {
                currentColorScheme().onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    // Grows with the font size (WCAG 1.4.4).
                    .heightIn(min = 36.dp)
                    .clip(shape)
                    .background(if (isSelected) currentColorScheme().background else currentColorScheme().surfaceVariant)
                    .clickable(role = Role.Tab) { onSelect(view) }
                    .semantics { this.selected = isSelected }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = view.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(view.label),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
