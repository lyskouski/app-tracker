// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.data.ALL_DAYS
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.data.HabitTime
import com.tercad.zwyka.data.MINUTES_PER_DAY
import com.tercad.zwyka.data.formatTime
import com.tercad.zwyka.data.parseTime
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.active_days
import com.tercad.zwyka.resources.add_habit_time
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.delete
import com.tercad.zwyka.resources.edit_habit_time
import com.tercad.zwyka.resources.habit
import com.tercad.zwyka.resources.habit_time_note
import com.tercad.zwyka.resources.habit_time_save_failed
import com.tercad.zwyka.resources.interval
import com.tercad.zwyka.resources.interval_1_hour
import com.tercad.zwyka.resources.interval_2_hours
import com.tercad.zwyka.resources.interval_30_min
import com.tercad.zwyka.resources.interval_custom
import com.tercad.zwyka.resources.invalid_time_range
import com.tercad.zwyka.resources.no_habits_to_schedule
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.time_end
import com.tercad.zwyka.resources.time_start

private enum class HabitTimeInterval(val minutes: Int?, val label: StringResource) {
    MIN_30(30, Res.string.interval_30_min),
    HOUR_1(60, Res.string.interval_1_hour),
    HOUR_2(120, Res.string.interval_2_hours),
    CUSTOM(null, Res.string.interval_custom),
}

/*
 * Creates ([habitTime] is null) or edits a scheduled time range of a habit.
 *
 * Week days are taken from the selected habit; saving passes them back
 * so that the habit gets updated as well.
 */
@Composable
fun HabitTimeDialog(
    habitTime: HabitTime?,
    habits: List<Habit>,
    initialStart: Int,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (habitId: Long, timeStart: Int, timeEnd: Int, note: String?, activeDays: Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    weekStart: WeekStart = WeekStart.MONDAY,
) {
    val start0 = habitTime?.timeStart ?: initialStart
    val end0 = habitTime?.timeEnd ?: (start0 + 60).coerceAtMost(MINUTES_PER_DAY)
    var habitId by remember(habitTime?.id) {
        mutableStateOf(habitTime?.habitId ?: habits.firstOrNull()?.id)
    }
    var interval by remember(habitTime?.id) {
        mutableStateOf(
            HabitTimeInterval.entries.firstOrNull { it.minutes == end0 - start0 }
                ?: HabitTimeInterval.CUSTOM,
        )
    }
    var startInput by remember(habitTime?.id) { mutableStateOf(formatTime(start0)) }
    var endInput by remember(habitTime?.id) { mutableStateOf(formatTime(end0)) }
    var note by remember(habitTime?.id) { mutableStateOf(habitTime?.note.orEmpty()) }
    // Week days belong to the habit: switching the habit shows its own days.
    var activeDays by remember(habitTime?.id, habitId) {
        mutableStateOf(habits.firstOrNull { it.id == habitId }?.activeDays ?: ALL_DAYS)
    }
    var selectingHabit by remember { mutableStateOf(false) }

    val timeStart = parseTime(startInput)
    val timeEnd = interval.minutes
        ?.let { minutes -> timeStart?.let { (it + minutes).coerceAtMost(MINUTES_PER_DAY) } }
        ?: parseTime(endInput)
    val isRangeValid = timeStart != null && timeEnd != null && timeEnd > timeStart
    val canSave = !isSaving && habitId != null && isRangeValid && activeDays != 0

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                stringResource(
                    if (habitTime == null) Res.string.add_habit_time else Res.string.edit_habit_time,
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (habits.isEmpty()) {
                    Text(stringResource(Res.string.no_habits_to_schedule))
                    return@Column
                }

                Text(stringResource(Res.string.habit))
                Box {
                    val selected = habits.firstOrNull { it.id == habitId }
                    OutlinedButton(
                        onClick = { selectingHabit = true },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        HabitLabel(selected)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = selectingHabit,
                        onDismissRequest = { selectingHabit = false },
                    ) {
                        habits.forEach { habit ->
                            DropdownMenuItem(
                                text = { HabitLabel(habit) },
                                onClick = {
                                    habitId = habit.id
                                    selectingHabit = false
                                },
                            )
                        }
                    }
                }

                Text(stringResource(Res.string.interval))
                val intervals = HabitTimeInterval.entries
                SegmentedSelector(
                    labels = intervals.map { stringResource(it.label) },
                    isSelected = { intervals[it] == interval },
                    onClick = { index ->
                        val chosen = intervals[index]
                        if (chosen == HabitTimeInterval.CUSTOM && timeEnd != null) {
                            endInput = formatTime(timeEnd)
                        }
                        interval = chosen
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startInput,
                        onValueChange = { startInput = it },
                        label = { Text(stringResource(Res.string.time_start)) },
                        singleLine = true,
                        isError = timeStart == null,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = if (interval == HabitTimeInterval.CUSTOM) {
                            endInput
                        } else {
                            timeEnd?.let(::formatTime).orEmpty()
                        },
                        onValueChange = { endInput = it },
                        label = { Text(stringResource(Res.string.time_end)) },
                        singleLine = true,
                        isError = timeStart != null && !isRangeValid,
                        enabled = !isSaving && interval == HabitTimeInterval.CUSTOM,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (timeStart != null && !isRangeValid) {
                    Text(
                        stringResource(Res.string.invalid_time_range),
                        color = currentColorScheme().error,
                    )
                }

                Text(stringResource(Res.string.active_days))
                WeekDaysSelector(
                    activeDays = activeDays,
                    onChange = { activeDays = it },
                    weekStart = weekStart,
                    enabled = !isSaving,
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(Res.string.habit_time_note)) },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )

                error?.let {
                    Text(
                        stringResource(Res.string.habit_time_save_failed, it),
                        color = currentColorScheme().error,
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (habitTime != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        enabled = !isSaving,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().error,
                        ),
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                } else {
                    Spacer(Modifier)
                }
                Row {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(Res.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            val id = habitId
                            if (id != null && timeStart != null && timeEnd != null) {
                                onSave(id, timeStart, timeEnd, note.trim().ifEmpty { null }, activeDays)
                            }
                        },
                        enabled = canSave,
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        },
    )
}

@Composable
private fun HabitLabel(habit: Habit?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val tint = applicationColor(habit?.color) ?: currentColorScheme().onSurface
        applicationIcon(habit?.icon)?.let { Icon(it, contentDescription = null, tint = tint) }
        Text(habit?.title.orEmpty(), maxLines = 1, modifier = Modifier.padding(end = 4.dp))
    }
}
