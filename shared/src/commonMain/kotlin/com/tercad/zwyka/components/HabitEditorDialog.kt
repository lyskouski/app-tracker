// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationColorOptions
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.applicationIconOption
import com.tercad.zwyka.defines.applicationIconOptions
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.normalizeApplicationColor
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.add_habit_failed
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.color_value
import com.tercad.zwyka.resources.default_color
import com.tercad.zwyka.resources.delete_habit
import com.tercad.zwyka.resources.delete_habit_confirmation
import com.tercad.zwyka.resources.delete_habit_confirmation_message
import com.tercad.zwyka.resources.edit_habit
import com.tercad.zwyka.resources.habit_color
import com.tercad.zwyka.resources.habit_icon
import com.tercad.zwyka.resources.habit_name
import com.tercad.zwyka.resources.habit_update_failed
import com.tercad.zwyka.resources.invalid_color
import com.tercad.zwyka.resources.no_icon
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.search_icons
import com.tercad.zwyka.resources.select_color
import com.tercad.zwyka.resources.select_icon

@Composable
fun HabitEditorDialog(
    habit: Habit?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (title: String, icon: String?, color: String?) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var title by remember(habit?.id) { mutableStateOf(habit?.title.orEmpty()) }
    var icon by remember(habit?.id) { mutableStateOf(habit?.icon) }
    var color by remember(habit?.id) { mutableStateOf(habit?.color) }
    var iconSearch by remember(habit?.id) { mutableStateOf("") }
    var colorInput by remember(habit?.id) { mutableStateOf(habit?.color.orEmpty()) }
    var selectingIcon by remember(habit?.id) { mutableStateOf(false) }
    var selectingColor by remember(habit?.id) { mutableStateOf(false) }
    var confirmingDelete by remember(habit?.id) { mutableStateOf(false) }

    val normalizedColor = normalizeApplicationColor(colorInput)
    val canSave = !isSaving && when {
        selectingIcon -> true
        selectingColor -> normalizedColor != null
        else -> title.isNotBlank()
    }
    val noIconDescription = stringResource(Res.string.no_icon)

    AlertDialog(
        onDismissRequest = {
            if (!isSaving && !confirmingDelete) onDismiss()
        },
        title = {
            Text(
                stringResource(
                    when {
                        selectingIcon -> Res.string.select_icon
                        selectingColor -> Res.string.select_color
                        habit == null -> Res.string.add_habit
                        else -> Res.string.edit_habit
                    },
                ),
            )
        },
        text = {
            when {
                selectingIcon -> {
                    Column {
                        OutlinedTextField(
                            value = iconSearch,
                            onValueChange = { iconSearch = it },
                            label = { Text(stringResource(Res.string.search_icons)) },
                            singleLine = true,
                        )
                        TextButton(
                            onClick = {
                                icon = null
                                selectingIcon = false
                            },
                            modifier = Modifier.semantics {
                                contentDescription = noIconDescription
                            },
                        ) {
                            Text("?  $noIconDescription")
                        }
                        val matchingIcons = remember(iconSearch) {
                            applicationIconOptions.filter { option ->
                                option.title.contains(iconSearch, ignoreCase = true)
                            }
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(48.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                        ) {
                            items(
                                items = matchingIcons,
                                key = { it.id },
                            ) { option ->
                                IconButton(
                                    onClick = {
                                        icon = option.id
                                        selectingIcon = false
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = option.title
                                    },
                                ) {
                                    Icon(
                                        imageVector = option.imageVector(),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }
                }
                selectingColor -> {
                    Column {
                        OutlinedTextField(
                            value = colorInput,
                            onValueChange = { colorInput = it },
                            label = { Text(stringResource(Res.string.color_value)) },
                            singleLine = true,
                            isError = colorInput.isNotBlank() && normalizedColor == null,
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(28.dp)
                                        .background(
                                            applicationColor(normalizedColor)
                                                ?: currentColorScheme().surfaceVariant,
                                            CircleShape,
                                        ),
                                )
                            },
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(192.dp),
                        ) {
                            items(
                                items = applicationColorOptions,
                                key = { it },
                            ) { colorOption ->
                                IconButton(
                                    onClick = {
                                        color = colorOption
                                        colorInput = colorOption
                                        selectingColor = false
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = colorOption
                                    },
                                ) {
                                    applicationColor(colorOption)?.let { colorValue ->
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(colorValue, CircleShape),
                                        )
                                    }
                                }
                            }
                        }
                        if (colorInput.isNotBlank() && normalizedColor == null) {
                            Text(stringResource(Res.string.invalid_color))
                        }
                        TextButton(
                            onClick = {
                                color = null
                                selectingColor = false
                            },
                        ) {
                            Text(stringResource(Res.string.default_color))
                        }
                    }
                }
                else -> {
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(Res.string.habit_name)) },
                            singleLine = true,
                            isError = error != null,
                            enabled = !isSaving,
                        )
                        Text(stringResource(Res.string.habit_icon))
                        OutlinedButton(
                            onClick = {
                                iconSearch = ""
                                selectingIcon = true
                            },
                            enabled = !isSaving,
                        ) {
                            applicationIcon(icon)?.let { image ->
                                Icon(imageVector = image, contentDescription = null)
                            } ?: Text("?")
                            Text(
                                applicationIconOption(icon)?.title
                                    ?: stringResource(Res.string.no_icon),
                            )
                        }
                        Text(stringResource(Res.string.habit_color))
                        OutlinedButton(
                            onClick = {
                                colorInput = color.orEmpty()
                                selectingColor = true
                            },
                            enabled = !isSaving,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(20.dp)
                                    .background(
                                        applicationColor(color)
                                            ?: currentColorScheme().surfaceVariant,
                                        CircleShape,
                                    ),
                            )
                            Text(color ?: stringResource(Res.string.default_color))
                        }
                        error?.let {
                            Text(
                                stringResource(
                                    if (habit == null) {
                                        Res.string.add_habit_failed
                                    } else {
                                        Res.string.habit_update_failed
                                    },
                                    it,
                                ),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        selectingIcon -> selectingIcon = false
                        selectingColor -> {
                            color = normalizedColor
                            selectingColor = false
                        }
                        else -> onSave(title.trim(), icon, color)
                    }
                },
                enabled = canSave,
            ) {
                Text(
                    stringResource(
                        if (selectingIcon) Res.string.back else Res.string.save,
                    ),
                )
            }
        },
        dismissButton = {
            Row {
                if (habit != null && onDelete != null && !selectingIcon && !selectingColor) {
                    TextButton(
                        onClick = { confirmingDelete = true },
                        enabled = !isSaving,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().error,
                        ),
                    ) {
                        Text(stringResource(Res.string.delete_habit))
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        },
    )

    if (confirmingDelete && habit != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(Res.string.delete_habit_confirmation)) },
            text = { Text(stringResource(Res.string.delete_habit_confirmation_message, habit.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        onDelete()
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = currentColorScheme().error,
                    ),
                ) {
                    Text(stringResource(Res.string.delete_habit))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmingDelete = false },
                    enabled = !isSaving,
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}
