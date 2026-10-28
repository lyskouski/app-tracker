// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.Goal
import com.tercad.zwyka.core.model.GoalDraft
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_goal
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.choose_goal_group
import com.tercad.zwyka.resources.edit_goal
import com.tercad.zwyka.resources.goal_description
import com.tercad.zwyka.resources.goal_group
import com.tercad.zwyka.resources.goal_save_failed
import com.tercad.zwyka.resources.goal_title
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.widgets.ErrorMessage

/*
 * Creates a goal, or edits [goal] of [goalGroup]; its group is either typed (a new one) or chosen from [groups].
 */
@Composable
fun GoalEditorDialog(
    groups: List<String>,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (GoalDraft) -> Unit,
    goal: Goal? = null,
    goalGroup: String = "",
) {
    var title by remember(goal) { mutableStateOf(goal?.title.orEmpty()) }
    var description by remember(goal) { mutableStateOf(goal?.description.orEmpty()) }
    var groupTitle by remember(goal) { mutableStateOf(goalGroup) }
    var selectingGroup by remember { mutableStateOf(false) }
    var groupWidthPx by remember { mutableStateOf(0) }
    val groupWidth = with(LocalDensity.current) { groupWidthPx.toDp() }
    // Partly typed name narrows the list; a chosen or a new name keeps all of them.
    val typed = groupTitle.trim()
    val suggestions = if (groups.any { it.equals(typed, ignoreCase = true) }) {
        groups
    } else {
        groups.filter { it.contains(typed, ignoreCase = true) }.ifEmpty { groups }
    }
    val canSave = !isSaving && title.isNotBlank() && groupTitle.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(if (goal == null) Res.string.add_goal else Res.string.edit_goal)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.goal_title)) },
                    singleLine = true,
                    isError = title.isBlank(),
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.goal_description)) },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(Modifier.fillMaxWidth().onSizeChanged { groupWidthPx = it.width }) {
                    OutlinedTextField(
                        value = groupTitle,
                        onValueChange = { groupTitle = it },
                        label = { Text(stringResource(Res.string.goal_group)) },
                        singleLine = true,
                        isError = groupTitle.isBlank(),
                        enabled = !isSaving,
                        trailingIcon = if (groups.isEmpty()) {
                            null
                        } else {
                            {
                                IconButton(onClick = { selectingGroup = !selectingGroup }, enabled = !isSaving) {
                                    Icon(
                                        imageVector = if (selectingGroup) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        contentDescription = stringResource(Res.string.choose_goal_group),
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Not focusable: the group can still be typed while the list is open.
                    DropdownMenu(
                        expanded = selectingGroup,
                        onDismissRequest = { selectingGroup = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.width(groupWidth),
                    ) {
                        suggestions.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group, maxLines = 1) },
                                onClick = {
                                    groupTitle = group
                                    selectingGroup = false
                                },
                            )
                        }
                    }
                }
                error?.let {
                    ErrorMessage(stringResource(Res.string.goal_save_failed, it))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        GoalDraft(
                            title = title.trim(),
                            description = description.trim().ifEmpty { null },
                            groupTitle = groupTitle.trim(),
                        ),
                    )
                },
                enabled = canSave,
            ) {
                Text(stringResource(Res.string.save))
            }
        },
    )
}
