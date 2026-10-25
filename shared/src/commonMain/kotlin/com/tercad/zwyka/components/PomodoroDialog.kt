// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_pomodoro
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.interval
import com.tercad.zwyka.resources.interval_15_min
import com.tercad.zwyka.resources.interval_1_hour
import com.tercad.zwyka.resources.interval_2_hours
import com.tercad.zwyka.resources.interval_30_min
import com.tercad.zwyka.resources.interval_custom
import com.tercad.zwyka.resources.pomodoro_habit
import com.tercad.zwyka.resources.pomodoro_minutes
import com.tercad.zwyka.resources.pomodoro_no_habit
import com.tercad.zwyka.resources.start

private enum class PomodoroInterval(val minutes: Int?, val label: StringResource) {
    MIN_15(15, Res.string.interval_15_min),
    MIN_30(30, Res.string.interval_30_min),
    HOUR_1(60, Res.string.interval_1_hour),
    HOUR_2(120, Res.string.interval_2_hours),
    CUSTOM(null, Res.string.interval_custom),
}

/** Longest custom timer, minutes: a day. */
private const val MAX_MINUTES = 24 * 60

/*
 * Starts a custom timer (Pomodoro) for a preset or custom number of minutes,
 * optionally for one of [habits].
 */
@Composable
fun PomodoroDialog(
    habits: List<Habit>,
    onStart: (minutes: Int, habitId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var interval by remember { mutableStateOf(PomodoroInterval.MIN_30) }
    var customMinutes by remember { mutableStateOf("") }
    var habitId by remember { mutableStateOf<Long?>(null) }
    var selectingHabit by remember { mutableStateOf(false) }
    var habitWidthPx by remember { mutableStateOf(0) }
    val minutes = interval.minutes ?: customMinutes.trim().toIntOrNull()?.takeIf { it in 1..MAX_MINUTES }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_pomodoro)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.interval))
                val intervals = PomodoroInterval.entries
                SegmentedSelector(
                    labels = intervals.map { stringResource(it.label) },
                    isSelected = { intervals[it] == interval },
                    onClick = { interval = intervals[it] },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (interval == PomodoroInterval.CUSTOM) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it },
                        label = { Text(stringResource(Res.string.pomodoro_minutes)) },
                        singleLine = true,
                        isError = customMinutes.isNotBlank() && minutes == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text(stringResource(Res.string.pomodoro_habit))
                Box(Modifier.fillMaxWidth().onSizeChanged { habitWidthPx = it.width }) {
                    val noHabit = stringResource(Res.string.pomodoro_no_habit)
                    val selected = habits.firstOrNull { it.id == habitId }
                    OutlinedButton(
                        onClick = { selectingHabit = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (selected != null) {
                            OwnerLabel(selected.title, selected.icon, selected.color)
                        } else {
                            OwnerLabel(noHabit, null, null, Icons.Filled.Timer)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = selectingHabit,
                        onDismissRequest = { selectingHabit = false },
                        modifier = Modifier.width(with(LocalDensity.current) { habitWidthPx.toDp() }),
                    ) {
                        DropdownMenuItem(
                            text = { OwnerLabel(noHabit, null, null, Icons.Filled.Timer) },
                            onClick = {
                                habitId = null
                                selectingHabit = false
                            },
                        )
                        if (habits.isNotEmpty()) HorizontalDivider()
                        habits.forEach { habit ->
                            DropdownMenuItem(
                                text = { OwnerLabel(habit.title, habit.icon, habit.color) },
                                onClick = {
                                    habitId = habit.id
                                    selectingHabit = false
                                },
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
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
                TextButton(
                    onClick = { minutes?.let { onStart(it, habitId) } },
                    enabled = minutes != null,
                ) {
                    Text(stringResource(Res.string.start))
                }
            }
        },
    )
}
