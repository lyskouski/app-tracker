// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/*
 * Read-only text field opening a list of [options]; with [searchLabel] the list can be filtered.
 *
 * The field is announced once, as a drop-down list with its [label] and the selected option
 * (WCAG 1.3.1, 4.1.2), instead of an editable field plus an unnamed click area.
 */
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    searchLabel: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var fieldWidthPx by remember { mutableStateOf(0) }
    val fieldWidth = with(LocalDensity.current) { fieldWidthPx.toDp() }
    val selected = options.getOrElse(selectedIndex) { "" }
    Box(modifier.fillMaxWidth().onSizeChanged { fieldWidthPx = it.width }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                )
            },
            // Only the overlay below is focused and announced.
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false }
                .clearAndSetSemantics {},
        )
        // Transparent overlay: a read-only text field does not forward clicks.
        Box(
            Modifier
                .matchParentSize()
                .clickable(role = Role.DropdownList) {
                    query = ""
                    expanded = true
                }
                .semantics {
                    contentDescription = label
                    stateDescription = selected
                },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(fieldWidth),
        ) {
            if (searchLabel != null) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(searchLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                )
            }
            options.forEachIndexed { index, text ->
                if (searchLabel != null && !text.contains(query.trim(), ignoreCase = true)) return@forEachIndexed
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelect(index)
                    },
                    modifier = Modifier.chosen(index == selectedIndex),
                )
            }
        }
    }
}

/*
 * [label]
 * [(content)                v]
 *
 * Button opening the [menu] of options. Announced as a drop-down list
 * with its [label] and the selected [value] (WCAG 1.3.1, 4.1.2).
 */
@Composable
fun SelectButton(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
    menu: @Composable ColumnScope.() -> Unit,
) {
    var widthPx by remember { mutableStateOf(0) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Announced by the button itself.
        Text(label, modifier = Modifier.clearAndSetSemantics {})
        Box(Modifier.fillMaxWidth().onSizeChanged { widthPx = it.width }) {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.DropdownList
                        contentDescription = label
                        stateDescription = value
                    },
            ) {
                content()
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.width(with(LocalDensity.current) { widthPx.toDp() }),
                content = menu,
            )
        }
    }
}
