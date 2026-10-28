// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.Canvas
import kotlin.math.roundToInt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringArrayResource

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.progress.markProgress
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.days_short
import com.tercad.zwyka.resources.months_short

/** Number of weeks (columns) shown, like a year of GitHub contributions. */
private const val GRAPH_WEEKS = 53

private val CELL_SIZE = 14.dp
private val CELL_GAP = 3.dp
private val MONTH_LABEL_HEIGHT = 16.dp

/*
 * Completion of every day, like GitHub contributions.
 *
 *          Jan       Feb
 *     Mon  ■ □ ■ ■ ■ ■ ■
 *     Tue  ■ ■ □ ■ ■ ■ ■
 *     ...
 *
 * Rows are days of the week from [weekStart], columns are weeks up to [today].
 * The cell is filled with the habit color by its completion, as the colored calendar.
 */
@Composable
fun HabitContributionGraph(
    habit: Habit,
    marks: Map<String, String>,
    today: LocalDate,
    weekStart: WeekStart,
    modifier: Modifier = Modifier,
) {
    val colorScheme = currentColorScheme()
    val baseColor = applicationColor(habit.color) ?: colorScheme.primary
    val emptyColor = colorScheme.surfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = colorScheme.onSurfaceVariant,
        fontSize = 9.sp,
        lineHeight = 14.sp,
    )
    val textMeasurer = rememberTextMeasurer()
    val dayNames = stringArrayResource(Res.array.days_short)
    val monthNames = stringArrayResource(Res.array.months_short)

    val firstDay = remember(today, weekStart) {
        val daysFromWeekStart = (today.dayOfWeek.ordinal - weekStart.dayOfWeek.ordinal + 7) % 7
        today.minus(daysFromWeekStart + (GRAPH_WEEKS - 1) * 7, DateTimeUnit.DAY)
    }
    // Text alternative of the graph (WCAG 1.1.1): completion of every marked day.
    val description = remember(habit, marks, firstDay, today) {
        generateSequence(firstDay) { it.plus(1, DateTimeUnit.DAY) }
            .takeWhile { it <= today }
            .mapNotNull { date ->
                marks[date.toString()]
                    ?.takeIf { it != INACTIVE_MARK }
                    ?.let { "$date: ${(markProgress(habit, it) * 100).roundToInt()}%" }
            }
            .joinToString()
    }
    val scrollState = rememberScrollState()
    // The latest weeks are the most relevant.
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(modifier = modifier) {
        Column(
            modifier = Modifier.padding(top = MONTH_LABEL_HEIGHT, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(CELL_GAP),
        ) {
            repeat(7) { row ->
                Box(Modifier.height(CELL_SIZE), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = dayNames.getOrNull((weekStart.dayOfWeek.ordinal + row) % 7).orEmpty(),
                        style = labelStyle,
                    )
                }
            }
        }
        // Time goes left to right in every language: RTL only moves the day labels to the right.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                Canvas(
                    modifier = Modifier
                        .semantics { contentDescription = description }
                        .width((CELL_SIZE + CELL_GAP) * GRAPH_WEEKS)
                        .height(MONTH_LABEL_HEIGHT + (CELL_SIZE + CELL_GAP) * 7),
                ) {
                    val cell = CELL_SIZE.toPx()
                    val step = cell + CELL_GAP.toPx()
                    val top = MONTH_LABEL_HEIGHT.toPx()
                    val corner = CornerRadius(2.dp.toPx())
                    var labelEnd = 0f
                    for (week in 0 until GRAPH_WEEKS) {
                        val weekFirstDay = firstDay.plus(week * 7, DateTimeUnit.DAY)
                        val x = week * step
                        // Month name above the first week of the month, if it fits.
                        val previousMonth = weekFirstDay.minus(7, DateTimeUnit.DAY).month
                        if (week == 0 || weekFirstDay.month != previousMonth) {
                            val label = textMeasurer.measure(
                                monthNames.getOrNull(weekFirstDay.month.ordinal).orEmpty(),
                                labelStyle,
                            )
                            if (x >= labelEnd) {
                                drawText(label, topLeft = Offset(x, 0f))
                                labelEnd = x + label.size.width + step / 2
                            }
                        }
                        for (row in 0 until 7) {
                            val date = weekFirstDay.plus(row, DateTimeUnit.DAY)
                            if (date > today) break
                            val mark = marks[date.toString()]
                            val color = when (mark) {
                                null, INACTIVE_MARK -> emptyColor
                                else -> baseColor.copy(alpha = markProgress(habit, mark).coerceAtLeast(0.1f))
                            }
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(x, top + row * step),
                                size = Size(cell, cell),
                                cornerRadius = corner,
                            )
                        }
                    }
                }
            }
        }
    }
}
