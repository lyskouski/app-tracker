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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.HabitTimeDraft
import com.tercad.zwyka.core.model.HabitTimeOwner
import com.tercad.zwyka.core.model.Intent
import com.tercad.zwyka.core.model.MINUTES_PER_DAY
import com.tercad.zwyka.core.model.WeekStart
import com.tercad.zwyka.core.time.formatTime
import com.tercad.zwyka.core.time.parseTime
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.active_days
import com.tercad.zwyka.resources.add_habit_time
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.delete
import com.tercad.zwyka.resources.edit_habit_time
import com.tercad.zwyka.resources.habit_or_intent
import com.tercad.zwyka.resources.habit_time_note
import com.tercad.zwyka.resources.habit_time_save_failed
import com.tercad.zwyka.resources.intent_title
import com.tercad.zwyka.resources.interval
import com.tercad.zwyka.resources.interval_1_hour
import com.tercad.zwyka.resources.interval_2_hours
import com.tercad.zwyka.resources.interval_30_min
import com.tercad.zwyka.resources.interval_custom
import com.tercad.zwyka.resources.invalid_time_range
import com.tercad.zwyka.resources.new_intent
import com.tercad.zwyka.resources.notify
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
 * Creates ([habitTime] is null) or edits a scheduled time range of a habit,
 * or of an intent when no habit is selected.
 *
 * Week days are set per time range; a new one starts from the days of the selected
 * habit / intent (whose own days accumulate all of its time ranges).
 */
@Composable
fun HabitTimeDialog(
    habitTime: HabitTime?,
    habits: List<Habit>,
    intents: List<Intent>,
    initialStart: Int,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (HabitTimeDraft) -> Unit,
    onDelete: (() -> Unit)? = null,
    /** Called when notification gets enabled, e.g. to ask for the permission. */
    onNotifyEnabled: () -> Unit = {},
    weekStart: WeekStart = WeekStart.MONDAY,
) {
    val start0 = habitTime?.timeStart ?: initialStart
    val end0 = habitTime?.timeEnd ?: (start0 + 60).coerceAtMost(MINUTES_PER_DAY)
    // No habit selected: the time range belongs to an intent (existing or new).
    var habitId by remember(habitTime?.id) {
        mutableStateOf(if (habitTime == null) habits.firstOrNull()?.id else habitTime.habitId)
    }
    var intentId by remember(habitTime?.id) { mutableStateOf(habitTime?.intentId) }
    var intentTitle by remember(habitTime?.id, intentId) {
        mutableStateOf(intents.firstOrNull { it.id == intentId }?.title.orEmpty())
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
    var notify by remember(habitTime?.id) { mutableStateOf(habitTime?.notify ?: false) }
    // Own days of the edited range; otherwise the days of the selected habit / intent.
    var activeDays by remember(habitTime?.id, habitId, intentId) {
        mutableStateOf(
            habitTime?.takeIf { it.habitId == habitId && it.intentId == intentId }?.activeDays
                ?: habits.firstOrNull { it.id == habitId }?.activeDays
                ?: intents.firstOrNull { it.id == intentId }?.activeDays
                ?: ALL_DAYS,
        )
    }
    var selectingHabit by remember { mutableStateOf(false) }

    val timeStart = parseTime(startInput)
    val timeEnd = interval.minutes
        ?.let { minutes -> timeStart?.let { (it + minutes).coerceAtMost(MINUTES_PER_DAY) } }
        ?: parseTime(endInput)
    val isRangeValid = timeStart != null && timeEnd != null && timeEnd > timeStart
    val canSave = !isSaving && (habitId != null || intentTitle.isNotBlank()) &&
        isRangeValid && activeDays != 0

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
                Text(stringResource(Res.string.habit_or_intent))
                Box {
                    val newIntent = stringResource(Res.string.new_intent)
                    val selectedHabit = habits.firstOrNull { it.id == habitId }
                    val selectedIntent = intents.firstOrNull { it.id == intentId }
                    OutlinedButton(
                        onClick = { selectingHabit = true },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        when {
                            selectedHabit != null -> OwnerLabel(selectedHabit.title, selectedHabit.icon, selectedHabit.color)
                            selectedIntent != null -> OwnerLabel(selectedIntent.title, selectedIntent.icon, selectedIntent.color)
                            else -> OwnerLabel(newIntent, null, null, Icons.Filled.Add)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = selectingHabit,
                        onDismissRequest = { selectingHabit = false },
                    ) {
                        habits.forEach { habit ->
                            DropdownMenuItem(
                                text = { OwnerLabel(habit.title, habit.icon, habit.color) },
                                onClick = {
                                    habitId = habit.id
                                    intentId = null
                                    selectingHabit = false
                                },
                            )
                        }
                        if (habits.isNotEmpty()) HorizontalDivider()
                        intents.forEach { intent ->
                            DropdownMenuItem(
                                text = { OwnerLabel(intent.title, intent.icon, intent.color) },
                                onClick = {
                                    habitId = null
                                    intentId = intent.id
                                    selectingHabit = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { OwnerLabel(newIntent, null, null, Icons.Filled.Add) },
                            onClick = {
                                habitId = null
                                intentId = null
                                selectingHabit = false
                            },
                        )
                    }
                }
                if (habitId == null) {
                    OutlinedTextField(
                        value = intentTitle,
                        onValueChange = { intentTitle = it },
                        label = { Text(stringResource(Res.string.intent_title)) },
                        singleLine = true,
                        isError = intentTitle.isBlank(),
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(Res.string.notify), modifier = Modifier.weight(1f))
                    Switch(
                        checked = notify,
                        onCheckedChange = { checked ->
                            if (checked) onNotifyEnabled()
                            notify = checked
                        },
                        enabled = !isSaving,
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
                            val owner = if (id != null) {
                                HabitTimeOwner.OfHabit(id)
                            } else {
                                HabitTimeOwner.OfIntent(intentId, intentTitle.trim())
                            }
                            if (timeStart != null && timeEnd != null) {
                                onSave(
                                    HabitTimeDraft(
                                        owner = owner,
                                        timeStart = timeStart,
                                        timeEnd = timeEnd,
                                        note = note.trim().ifEmpty { null },
                                        activeDays = activeDays,
                                        notify = notify,
                                    ),
                                )
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
private fun OwnerLabel(
    title: String,
    icon: String?,
    color: String?,
    fallbackIcon: ImageVector? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val tint = applicationColor(color) ?: currentColorScheme().onSurface
        (applicationIcon(icon) ?: fallbackIcon)?.let { Icon(it, contentDescription = null, tint = tint) }
        Text(title, maxLines = 1, modifier = Modifier.padding(end = 4.dp))
    }
}
