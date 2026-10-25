// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.model.hasNumericTarget
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.defines.CompletenessType
import com.tercad.zwyka.defines.DateFormat
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.statusOptions
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.date
import com.tercad.zwyka.resources.day_state_save_failed
import com.tercad.zwyka.resources.edit_day_state
import com.tercad.zwyka.resources.habit_name
import com.tercad.zwyka.resources.months_short
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.skip
import com.tercad.zwyka.resources.status
import com.tercad.zwyka.resources.target_value
import org.jetbrains.compose.resources.stringArrayResource

@Composable
fun DayStateDialog(
    dayState: DayState,
    habit: Habit,
    dateFormat: DateFormat,
    completenessType: CompletenessType,
    saveError: String?,
    isSaving: Boolean,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val monthNames = stringArrayResource(Res.array.months_short)
    val habitColor = applicationColor(habit.color)
        ?.takeIf { completenessType == CompletenessType.COLORS }
    var value by remember(dayState) {
        mutableStateOf(parseNumericValue(dayState.mark)?.let(::formatNumericValue).orEmpty())
    }
    val parsedValue = parseNumericValue(value)?.takeIf { it >= 0 }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = { Text(stringResource(Res.string.edit_day_state)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = habit.title,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(Res.string.habit_name)) },
                    leadingIcon = {
                        val icon = applicationIcon(habit.icon)
                        if (icon == null) {
                            Text("?")
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dateFormat.format(dayState.date, monthNames),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(Res.string.date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (habit.hasNumericTarget) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text(stringResource(Res.string.target_value)) },
                            singleLine = true,
                            isError = value.isNotBlank() && parsedValue == null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                        )
                        habit.targetType?.let { Text(it) }
                    }
                } else {
                    Text(stringResource(Res.string.status))
                    statusOptions.chunked(3).forEach { rowOptions ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rowOptions.forEach { option ->
                                val onClick = { onStatusChange(option.mark) }
                                val buttonModifier = Modifier.weight(1f)
                                if (dayState.mark == option.mark) {
                                    Button(
                                        onClick = onClick,
                                        modifier = buttonModifier,
                                        enabled = !isSaving,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            StatusMark(
                                                mark = option.mark,
                                                color = habitColor ?: LocalContentColor.current,
                                                type = completenessType,
                                            )
                                            Text(stringResource(option.label))
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = onClick,
                                        modifier = buttonModifier,
                                        enabled = !isSaving,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            StatusMark(
                                                mark = option.mark,
                                                color = habitColor ?: LocalContentColor.current,
                                                type = completenessType,
                                            )
                                            Text(stringResource(option.label))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                saveError?.let { error ->
                    Text(stringResource(Res.string.day_state_save_failed, error))
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
                        onClick = { onStatusChange(INACTIVE_MARK) },
                        enabled = !isSaving,
                    ) {
                        Text(stringResource(Res.string.skip))
                    }
                    if (habit.hasNumericTarget) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                }
                if (habit.hasNumericTarget) {
                    TextButton(
                        onClick = { parsedValue?.let { onStatusChange(formatNumericValue(it)) } },
                        enabled = !isSaving && parsedValue != null,
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        },
    )
}
