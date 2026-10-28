// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.model.isBeforeCreation
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.core.progress.displayedMark
import com.tercad.zwyka.core.progress.isProgressMark
import com.tercad.zwyka.core.progress.markProgress
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
 *  (✓)  (✓)  (|)  ( )  ( )   —   ( )
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
    modifier: Modifier = Modifier,
) {
    val dayNames = stringArrayResource(Res.array.days_short)
    LazyColumn(
        modifier = modifier,
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
) {
    val habitColor = applicationColor(habit.color) ?: currentColorScheme().primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(currentColorScheme().surfaceVariant, RoundedCornerShape(12.dp))
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
                    WeeklyDayCell(
                        habit = habit,
                        date = date,
                        marks = marks,
                        today = today,
                        habitColor = habitColor,
                        completenessType = completenessType,
                        onDayStateClick = onDayStateClick,
                        onDayStateLongClick = onDayStateLongClick,
                    )
                }
            }
        }
    }
}

/*
 * Filled circle with a check when done, outlined circle with the mark otherwise,
 * "—" for skipped days.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeeklyDayCell(
    habit: Habit,
    date: String,
    marks: Map<CellKey, String>,
    today: String,
    habitColor: Color,
    completenessType: CompletenessType,
    onDayStateClick: (DayState) -> Unit,
    onDayStateLongClick: (DayState) -> Unit,
) {
    val stored = marks[CellKey(habitId = habit.id, date = date)]
    val mark = displayedMark(habit, date, marks, today)
    val isProgress = isProgressMark(habit, mark)
    val isDone = isProgress && markProgress(habit, mark) >= 1f
    val beforeCreation = habit.isBeforeCreation(date, stored)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .then(
                when {
                    isDone -> Modifier.background(habitColor)
                    mark == INACTIVE_MARK -> Modifier
                    beforeCreation -> Modifier.border(1.dp, currentColorScheme().outlineVariant, CircleShape)
                    isProgress -> Modifier.border(2.dp, habitColor, CircleShape)
                    else -> Modifier.border(1.dp, currentColorScheme().outline, CircleShape)
                },
            )
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
            .semantics { contentDescription = "${habit.title}, $date, $mark" },
    ) {
        when {
            isDone -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            mark == INACTIVE_MARK -> Text(INACTIVE_MARK, color = currentColorScheme().onSurfaceVariant)
            isProgress -> {
                val value = parseNumericValue(mark)
                if (value != null) {
                    Text(
                        text = formatNumericValue(value),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else {
                    StatusMark(
                        mark = mark,
                        color = habitColor.takeIf { completenessType == CompletenessType.COLORS }
                            ?: currentColorScheme().onSurface,
                        type = completenessType,
                    )
                }
            }
        }
    }
}
