// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.GoalGroup
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.delete
import com.tercad.zwyka.resources.edit_goal_group
import com.tercad.zwyka.resources.goal_group
import com.tercad.zwyka.resources.goal_save_failed
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.widgets.ErrorMessage
import com.tercad.zwyka.widgets.TextInput
import com.tercad.zwyka.widgets.popupShortcuts

/*
 * Renames [group]: its goals are shown under the new name, [onSave] gets the trimmed one.
 * [onDelete] asks to delete the group with its goals, as the long press on the group does.
 */
@Composable
fun GoalGroupEditorDialog(
    group: GoalGroup,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var title by remember(group.id) { mutableStateOf(group.title) }
    val canSave = !isSaving && title.isNotBlank()

    fun save() {
        onSave(title.trim())
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier.popupShortcuts(
            onEnter = ::save.takeIf { canSave },
            onDelete = onDelete.takeIf { !isSaving },
        ),
        title = { Text(stringResource(Res.string.edit_goal_group)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextInput(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.goal_group)) },
                    singleLine = true,
                    isError = title.isBlank(),
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    ErrorMessage(stringResource(Res.string.goal_save_failed, it))
                }
            }
        },
        // Delete goes to the bottom left corner, away from the buttons that keep the group.
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onDelete,
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = currentColorScheme().error,
                    ),
                ) {
                    Text(stringResource(Res.string.delete))
                }
                Row {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(Res.string.cancel))
                    }
                    TextButton(onClick = ::save, enabled = canSave) {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        },
    )
}
