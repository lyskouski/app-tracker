// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.model.hasNumericTarget
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.core.progress.TodayStatus
import com.tercad.zwyka.core.progress.groupByTodayStatus
import com.tercad.zwyka.core.progress.isProgressMark
import com.tercad.zwyka.defines.CompletenessType
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.apply
import com.tercad.zwyka.resources.completed_skipped
import com.tercad.zwyka.resources.done
import com.tercad.zwyka.resources.skip
import com.tercad.zwyka.resources.target_value

private const val DONE_MARK = "X"

/*
 * Habits of [today] as a list: not done first, then done and skipped below a divider.
 *
 * Swipe to the right marks a habit as done (a numeric target asks for the value first),
 * swipe to the left skips it. A click opens the day state ([onDayStateClick]),
 * a long click on a marked habit deletes its mark ([onDayStateLongClick]).
 */
@Composable
fun TodayHabitList(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    today: String,
    completenessType: CompletenessType,
    onMarkChange: (DayState) -> Unit,
    onDayStateClick: (DayState) -> Unit,
    onDayStateLongClick: (DayState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(habits, marks, today) { groupByTodayStatus(habits, marks, today) }
    val firstCompleted = grouped.indexOfFirst { it.second != TodayStatus.NOT_DONE }
    // Habit with a numeric target waiting for its value after a swipe.
    var valueHabitId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        grouped.forEachIndexed { index, (habit, _) ->
            if (index == firstCompleted) {
                item(key = "completed") { CompletedDivider() }
            }
            item(key = habit.id) {
                val mark = marks[CellKey(habitId = habit.id, date = today)]
                val skip = { onMarkChange(DayState(habit.id, today, INACTIVE_MARK)) }
                val done = {
                    if (habit.hasNumericTarget) {
                        valueHabitId = habit.id
                    } else {
                        onMarkChange(DayState(habit.id, today, DONE_MARK))
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    SwipeableHabitRow(onDone = done, onSkip = skip) {
                        TodayHabitRow(
                            habit = habit,
                            mark = mark,
                            completenessType = completenessType,
                            onClick = {
                                onDayStateClick(
                                    DayState(
                                        habitId = habit.id,
                                        date = today,
                                        mark = mark?.takeIf { isProgressMark(habit, it) } ?: "·",
                                    ),
                                )
                            },
                            // Only a stored mark can be deleted.
                            onLongClick = mark?.let { { onDayStateLongClick(DayState(habit.id, today, it)) } },
                        )
                    }
                    if (valueHabitId == habit.id) {
                        NumericValueField(
                            habit = habit,
                            mark = mark,
                            onApply = { value ->
                                valueHabitId = null
                                onMarkChange(DayState(habit.id, today, value))
                            },
                        )
                    }
                }
            }
        }
    }
}

/*
 * ——— Completed / Skipped ———
 */
@Composable
private fun CompletedDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.completed_skipped),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = currentColorScheme().onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/*
 * Moves [content] horizontally with the finger; past the threshold
 * a swipe to the right calls [onDone], to the left [onSkip].
 */
@Composable
private fun SwipeableHabitRow(
    onDone: () -> Unit,
    onSkip: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val currentOnDone by rememberUpdatedState(onDone)
    val currentOnSkip by rememberUpdatedState(onSkip)
    val doneLabel = stringResource(Res.string.done)
    val skipLabel = stringResource(Res.string.skip)
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(doneLabel) { onDone(); true },
                    CustomAccessibilityAction(skipLabel) { onSkip(); true },
                )
            },
    ) {
        val isDone = offset.value > 0f
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(
                    when {
                        offset.value == 0f -> currentColorScheme().surface
                        isDone -> currentColorScheme().primaryContainer
                        else -> currentColorScheme().surfaceVariant
                    },
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isDone) Arrangement.Start else Arrangement.End,
        ) {
            if (offset.value != 0f) {
                Icon(
                    imageVector = if (isDone) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                )
                Text(
                    text = if (isDone) doneLabel else skipLabel,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    val threshold = 64.dp.toPx()
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val total = offset.value
                            scope.launch { offset.animateTo(0f) }
                            when {
                                total > threshold -> currentOnDone()
                                total < -threshold -> currentOnSkip()
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(0f) } },
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch { offset.snapTo(offset.value + dragAmount) }
                    }
                },
        ) {
            content()
        }
    }
}

/*
 * [icon] Title                         mark / value of target
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodayHabitRow(
    habit: Habit,
    mark: String?,
    completenessType: CompletenessType,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val habitColor = applicationColor(habit.color)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(currentColorScheme().surfaceVariant, RoundedCornerShape(8.dp))
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val icon = applicationIcon(habit.icon)
        if (icon == null) {
            Text("?")
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = habitColor ?: currentColorScheme().onSurface,
            )
        }
        Text(
            text = habit.title,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
        val target = habit.targetValue
        if (target != null) {
            val value = mark?.let(::parseNumericValue)?.let(::formatNumericValue) ?: "0"
            Text(
                text = "$value / ${formatNumericValue(target)}" + (habit.targetType?.let { " $it" } ?: ""),
                color = currentColorScheme().onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (mark != null) {
            StatusMark(
                mark = mark,
                color = habitColor?.takeIf { completenessType == CompletenessType.COLORS }
                    ?: currentColorScheme().onSurface,
                type = completenessType,
            )
        }
    }
}

/*
 * Value of a numeric target, shown after a swipe to the right; prefilled with the target.
 */
@Composable
private fun NumericValueField(
    habit: Habit,
    mark: String?,
    onApply: (String) -> Unit,
) {
    var value by remember(habit.id) {
        mutableStateOf(
            (mark?.let(::parseNumericValue) ?: habit.targetValue)?.let(::formatNumericValue).orEmpty(),
        )
    }
    val parsedValue = parseNumericValue(value)?.takeIf { it >= 0 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text(stringResource(Res.string.target_value)) },
            singleLine = true,
            isError = value.isNotBlank() && parsedValue == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        habit.targetType?.let { Text(it) }
        TextButton(
            onClick = { parsedValue?.let { onApply(formatNumericValue(it)) } },
            enabled = parsedValue != null,
        ) {
            Text(stringResource(Res.string.apply))
        }
    }
}
