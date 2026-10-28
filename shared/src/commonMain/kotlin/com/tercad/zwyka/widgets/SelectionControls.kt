// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.tercad.zwyka.defines.currentColorScheme

/*
 * Row of equally wide toggles.
 *
 * Announced as radio buttons, or as checkboxes when [multiple] options may be selected.
 * The height grows with the font size (WCAG 1.4.4).
 */
@Composable
fun SegmentedSelector(
    labels: List<String>,
    isSelected: (Int) -> Boolean,
    onClick: (Int) -> Unit,
    enabled: Boolean = true,
    multiple: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = isSelected(index)
            val selection = if (multiple) {
                Modifier.toggleable(
                    value = selected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { onClick(index) },
                )
            } else {
                Modifier.selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = { onClick(index) },
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 32.dp)
                    .clip(shape)
                    .background(
                        if (selected) currentColorScheme().primary
                        else Color.Transparent,
                    )
                    .border(1.dp, currentColorScheme().outline, shape)
                    .then(selection),
            ) {
                Text(
                    label,
                    color = if (selected) currentColorScheme().onPrimary
                    else currentColorScheme().onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/*
 * One of the mutually exclusive options shown as buttons: filled when [selected].
 * Screen readers announce it as a radio button with its state (WCAG 1.3.1, 4.1.2).
 */
@Composable
fun ChoiceButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val semantics = modifier.semantics {
        role = Role.RadioButton
        this.selected = selected
    }
    if (selected) {
        Button(onClick = onClick, modifier = semantics, enabled = enabled, content = content)
    } else {
        OutlinedButton(onClick = onClick, modifier = semantics, enabled = enabled, content = content)
    }
}

/*
 * [label] ........................ [x]
 *
 * The whole row toggles the checkbox, which is announced with its label (WCAG 1.3.1, 2.5.3).
 */
@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled, modifier = Modifier.padding(12.dp))
        Text(label)
    }
}

/*
 * [label] ........................ [==o]
 *
 * The whole row toggles the switch, which is announced with its label (WCAG 1.3.1, 2.5.3).
 */
@Composable
fun SwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/*
 * Items of a group of options, e.g. a grid of icons or colors: marks the chosen one for screen readers.
 */
fun Modifier.chosen(isChosen: Boolean): Modifier = semantics { selected = isChosen }
