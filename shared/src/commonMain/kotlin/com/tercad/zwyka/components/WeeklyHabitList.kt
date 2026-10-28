// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.hasNumericTarget
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.core.progress.displayedMark
import com.tercad.zwyka.core.progress.isProgressMark
import com.tercad.zwyka.defines.CompletenessType
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.days_short
import com.tercad.zwyka.resources.every_day

/*
 * A card per not archived habit with its marks of [weekDates]:
 *
 * [icon] Title                    Every day
 *  Mon  Tue  Wed  Thu  Fri  Sat  Sun
 *  [X]  [X]  [|]  [ ]  [ ]  [—]  [ ]
 */
@Composable
fun WeeklyHabitList(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    weekDates: List<LocalDate>,
    today: String,
    completenessType: CompletenessType,
    onDayStateClick: (DayState) -> Unit,
    onDayStateLongClick: (DayState) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayNames = stringArrayResource(Res.array.days_short)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(habits.filterNot { it.isArchived }, key = { it.id }) { habit ->
            WeeklyHabitCard(
                habit = habit,
                marks = marks,
                weekDates = weekDates,
                today = today,
                dayNames = dayNames,
                completenessType = completenessType,
                onDayStateClick = onDayStateClick,
                onDayStateLongClick = onDayStateLongClick,
                onHabitLongClick = onHabitLongClick,
            )
        }
    }
}

@Composable
private fun WeeklyHabitCard(
    habit: Habit,
    marks: Map<CellKey, String>,
    weekDates: List<LocalDate>,
    today: String,
    dayNames: List<String>,
    completenessType: CompletenessType,
    onDayStateClick: (DayState) -> Unit,
    onDayStateLongClick: (DayState) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
) {
    val habitColor = applicationColor(habit.color) ?: currentColorScheme().primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(currentColorScheme().surfaceVariant, RoundedCornerShape(12.dp))
            // Day cells handle their own clicks, the rest of the card opens the habit editor.
            .pointerInput(habit) { detectTapGestures(onLongPress = { onHabitLongClick(habit) }) }
            .semantics {
                onLongClick { onHabitLongClick(habit); true }
            }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val icon = applicationIcon(habit.icon)
            if (icon == null) {
                Text("?")
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = habitColor)
            }
            Text(
                text = habit.title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (habit.activeDays == ALL_DAYS) {
                    stringResource(Res.string.every_day)
                } else {
                    weekDates
                        .filter { habit.isActiveOn(it.dayOfWeek) }
                        .joinToString(", ") { dayNames.getOrElse(it.dayOfWeek.ordinal) { "" } }
                },
                color = currentColorScheme().onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        habit.remarks?.takeIf { it.isNotBlank() }?.let { remarks ->
            Text(
                text = remarks,
                color = currentColorScheme().onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDates.forEach { localDate ->
                val date = localDate.toString()
                val isToday = date == today
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = dayNames.getOrElse(localDate.dayOfWeek.ordinal) { "" },
                        color = if (isToday) currentColorScheme().primary else currentColorScheme().onSurfaceVariant,
                        fontWeight = if (isToday) FontWeight.Bold else null,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        WeeklyDayCell(
                            habit = habit,
                            date = date,
                            marks = marks,
                            today = today,
                            width = maxWidth,
                            completenessType = completenessType,
                            onDayStateClick = onDayStateClick,
                            onDayStateLongClick = onDayStateLongClick,
                        )
                    }
                }
            }
        }
    }
}

/*
 * Same cell as the "Overall" table: a rectangle with the mark shown
 * as a symbol or a colored circle (see [CompletenessType]), today is outlined.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeeklyDayCell(
    habit: Habit,
    date: String,
    marks: Map<CellKey, String>,
    today: String,
    width: Dp,
    completenessType: CompletenessType,
    onDayStateClick: (DayState) -> Unit,
    onDayStateLongClick: (DayState) -> Unit,
) {
    val stored = marks[CellKey(habitId = habit.id, date = date)]
    val value = displayedMark(habit, date, marks, today)
    // A reached value of a numeric target is shown above its unit.
    val unit = habit.targetType
        ?.takeIf { habit.hasNumericTarget && parseNumericValue(value) != null }
    CalendarTableCell(
        text = value,
        width = width,
        height = 48.dp,
        background = if (habit.isBeforeCreation(date, stored)) {
            currentColorScheme().background
        } else {
            applicationColor(habit.color)?.copy(alpha = 0.05f) ?: currentColorScheme().surface
        },
        textColor = applicationColor(habit.color)
            ?.takeIf { completenessType == CompletenessType.COLORS }
            ?: currentColorScheme().onSurface,
        completenessType = completenessType,
        bottomLabel = unit,
        highlight = todayHighlight(isToday = date == today, left = true, right = true),
        modifier = Modifier
            .combinedClickable(
                // Only a stored mark can be deleted.
                onLongClick = stored?.let { { onDayStateLongClick(DayState(habit.id, date, it)) } },
            ) {
                onDayStateClick(
                    DayState(
                        habitId = habit.id,
                        date = date,
                        mark = stored?.takeIf { isProgressMark(habit, it) } ?: "·",
                    ),
                )
            }
            .semantics {
                contentDescription = "${habit.title}, $date, $value" + (unit?.let { " $it" } ?: "")
            },
    )
}
