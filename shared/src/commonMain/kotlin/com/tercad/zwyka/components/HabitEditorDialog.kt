// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationColorOptions
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.applicationIconOption
import com.tercad.zwyka.defines.applicationIconOptions
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.normalizeApplicationColor
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.active_days
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.add_habit_failed
import com.tercad.zwyka.resources.archive_habit
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.color_value
import com.tercad.zwyka.resources.default_color
import com.tercad.zwyka.resources.delete_habit
import com.tercad.zwyka.resources.delete_habit_confirmation
import com.tercad.zwyka.resources.delete_habit_confirmation_message
import com.tercad.zwyka.resources.edit_habit
import com.tercad.zwyka.resources.habit_name
import com.tercad.zwyka.resources.habit_remarks
import com.tercad.zwyka.resources.habit_update_failed
import com.tercad.zwyka.resources.invalid_color
import com.tercad.zwyka.resources.no_icon
import com.tercad.zwyka.resources.numeric_target
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.search_icons
import com.tercad.zwyka.resources.select_color
import com.tercad.zwyka.resources.select_icon
import com.tercad.zwyka.resources.target_type
import com.tercad.zwyka.resources.target_value
import com.tercad.zwyka.widgets.CheckboxRow
import com.tercad.zwyka.widgets.ErrorMessage
import com.tercad.zwyka.widgets.SwitchRow
import com.tercad.zwyka.widgets.chosen
import com.tercad.zwyka.widgets.popupShortcuts

@Composable
fun HabitEditorDialog(
    habit: Habit?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        icon: String?,
        color: String?,
        remarks: String?,
        activeDays: Int,
        isArchived: Boolean,
        targetValue: Double?,
        targetType: String?,
    ) -> Unit,
    onDelete: (() -> Unit)? = null,
    weekStart: WeekStart = WeekStart.MONDAY,
) {
    var title by remember(habit?.id) { mutableStateOf(habit?.title.orEmpty()) }
    var icon by remember(habit?.id) { mutableStateOf(habit?.icon) }
    var color by remember(habit?.id) { mutableStateOf(habit?.color) }
    var remarks by remember(habit?.id) { mutableStateOf(habit?.remarks.orEmpty()) }
    var activeDays by remember(habit?.id) { mutableStateOf(habit?.activeDays ?: ALL_DAYS) }
    var isArchived by remember(habit?.id) { mutableStateOf(habit?.isArchived ?: false) }
    var hasTarget by remember(habit?.id) { mutableStateOf(habit?.targetValue != null) }
    var targetValue by remember(habit?.id) {
        mutableStateOf(habit?.targetValue?.let(::formatNumericValue).orEmpty())
    }
    var targetType by remember(habit?.id) { mutableStateOf(habit?.targetType.orEmpty()) }
    var iconSearch by remember(habit?.id) { mutableStateOf("") }
    var colorInput by remember(habit?.id) { mutableStateOf(habit?.color.orEmpty()) }
    var selectingIcon by remember(habit?.id) { mutableStateOf(false) }
    var selectingColor by remember(habit?.id) { mutableStateOf(false) }
    var confirmingDelete by remember(habit?.id) { mutableStateOf(false) }

    val normalizedColor = normalizeApplicationColor(colorInput)
    val parsedTarget = parseNumericValue(targetValue)?.takeIf { it > 0 }
    val canSave = !isSaving && when {
        selectingIcon -> true
        selectingColor -> normalizedColor != null
        else -> title.isNotBlank() && (!hasTarget || parsedTarget != null)
    }
    val noIconDescription = stringResource(Res.string.no_icon)
    val canDelete = habit != null && onDelete != null && !selectingIcon && !selectingColor

    fun confirm() {
        when {
            selectingIcon -> selectingIcon = false
            selectingColor -> {
                color = normalizedColor
                selectingColor = false
            }
            else -> onSave(
                title.trim(),
                icon,
                color,
                remarks.trim().ifEmpty { null },
                activeDays,
                isArchived,
                parsedTarget.takeIf { hasTarget },
                targetType.trim().ifEmpty { null }.takeIf { hasTarget },
            )
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving && !confirmingDelete) onDismiss()
        },
        modifier = Modifier.popupShortcuts(
            onEnter = ::confirm.takeIf { canSave },
            onDelete = { confirmingDelete = true }.takeIf { canDelete && !isSaving },
        ),
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
                                    modifier = Modifier
                                        .semantics { contentDescription = option.title }
                                        .chosen(icon == option.id),
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
                            modifier = Modifier.fillMaxWidth(),
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
                        val rgb = (normalizedColor ?: "#000000").substring(1).toInt(16)
                        listOf(
                            Triple("R", 16, Color.Red),
                            Triple("G", 8, Color.Green),
                            Triple("B", 0, Color.Blue),
                        ).forEach { (name, shift, tint) ->
                            val channel = (rgb shr shift) and 0xFF
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, modifier = Modifier.width(16.dp))
                                Slider(
                                    value = channel.toFloat(),
                                    onValueChange = {
                                        val updated = (rgb and (0xFF shl shift).inv()) or
                                            (it.roundToInt().coerceIn(0, 255) shl shift)
                                        colorInput = "#" + updated.toString(16)
                                            .padStart(6, '0').uppercase()
                                    },
                                    valueRange = 0f..255f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = tint,
                                        activeTrackColor = tint,
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { contentDescription = name },
                                )
                                Text(
                                    channel.toString(),
                                    modifier = Modifier.width(32.dp),
                                )
                            }
                        }
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
                                    modifier = Modifier
                                        .semantics { contentDescription = colorOption }
                                        .chosen(normalizedColor == colorOption),
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
                            ErrorMessage(stringResource(Res.string.invalid_color))
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
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(Res.string.habit_name)) },
                            singleLine = true,
                            isError = error != null,
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text(stringResource(Res.string.habit_remarks)) },
                            minLines = 3,
                            maxLines = 6,
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        )
                        SwitchRow(
                            checked = hasTarget,
                            onCheckedChange = { hasTarget = it },
                            label = stringResource(Res.string.numeric_target),
                            modifier = Modifier.padding(top = 8.dp),
                            // Stored values would lose their meaning without the target.
                            enabled = !isSaving && habit?.targetValue == null,
                        )
                        if (hasTarget) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedTextField(
                                    value = targetValue,
                                    onValueChange = { targetValue = it },
                                    label = { Text(stringResource(Res.string.target_value)) },
                                    singleLine = true,
                                    isError = targetValue.isNotBlank() && parsedTarget == null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    enabled = !isSaving,
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = targetType,
                                    onValueChange = { targetType = it },
                                    label = { Text(stringResource(Res.string.target_type)) },
                                    singleLine = true,
                                    enabled = !isSaving,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        Text(
                            stringResource(Res.string.active_days),
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        WeekDaysSelector(
                            activeDays = activeDays,
                            onChange = { activeDays = it },
                            weekStart = weekStart,
                            enabled = !isSaving,
                        )
                        OutlinedButton(
                            onClick = {
                                colorInput = color.orEmpty()
                                selectingColor = true
                            },
                            enabled = !isSaving,
                            modifier = Modifier.padding(top = 14.dp),
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
                        OutlinedButton(
                            onClick = {
                                iconSearch = ""
                                selectingIcon = true
                            },
                            enabled = !isSaving,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            applicationIcon(icon)?.let { image ->
                                Icon(imageVector = image, contentDescription = null)
                            } ?: Text("?")
                            Text(
                                applicationIconOption(icon)?.title
                                    ?: stringResource(Res.string.no_icon),
                            )
                        }
                        if (habit != null) {
                            CheckboxRow(
                                checked = isArchived,
                                onCheckedChange = { isArchived = it },
                                label = stringResource(Res.string.archive_habit),
                                enabled = !isSaving,
                            )
                        }
                        error?.let {
                            ErrorMessage(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                    if (canDelete) {
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
                }
                TextButton(
                    onClick = ::confirm,
                    enabled = canSave,
                ) {
                    Text(
                        stringResource(
                            if (selectingIcon) Res.string.back else Res.string.save,
                        ),
                    )
                }
            }
        },
    )

    if (confirmingDelete && habit != null && onDelete != null) {
        val delete = {
            confirmingDelete = false
            onDelete()
        }
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            modifier = Modifier.popupShortcuts(onEnter = delete.takeIf { !isSaving }),
            title = { Text(stringResource(Res.string.delete_habit_confirmation)) },
            text = { Text(stringResource(Res.string.delete_habit_confirmation_message, habit.title)) },
            confirmButton = {
                TextButton(
                    onClick = delete,
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
