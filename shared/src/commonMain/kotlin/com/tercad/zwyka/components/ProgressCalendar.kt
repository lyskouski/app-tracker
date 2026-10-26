// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.progress.calculateProgressHistory
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.days_short
import com.tercad.zwyka.resources.months_short
import com.tercad.zwyka.resources.next_month
import com.tercad.zwyka.resources.previous_month

/*
 * Month of the daily progress, like the top-left corner of the calendar table.
 *
 *     <      Dec 2024      >
 *     Mon Tue Wed ...
 *     (25)(26)(27) ...
 *
 * Rows are weeks from [weekStart]; every day is a ring filled clockwise by its
 * completion. Days of the adjacent months are dimmed, future days have no ring.
 */
@Composable
fun ProgressCalendar(
    habits: List<Habit>,
    marks: Map<CellKey, String>,
    today: LocalDate,
    weekStart: WeekStart,
    modifier: Modifier = Modifier,
) {
    val colorScheme = currentColorScheme()
    val trackColor = colorScheme.outlineVariant
    val progressColor = colorScheme.primary
    val textColor = colorScheme.onSurface
    val dayNames = stringArrayResource(Res.array.days_short)
    val monthNames = stringArrayResource(Res.array.months_short)

    // Months from the current one: 0 is the current month, -1 the previous one.
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    val month = remember(today, monthOffset) {
        LocalDate(today.year, today.monthNumber, 1).plus(monthOffset, DateTimeUnit.MONTH)
    }
    val days = remember(month, weekStart) {
        val daysFromWeekStart = (month.dayOfWeek.ordinal - weekStart.dayOfWeek.ordinal + 7) % 7
        val first = month.minus(daysFromWeekStart, DateTimeUnit.DAY)
        val last = month.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val weeks = (daysFromWeekStart + last.dayOfMonth + 6) / 7
        (0 until weeks * 7).map { first.plus(it, DateTimeUnit.DAY) }
    }
    val progress = remember(habits, marks, days, today) {
        calculateProgressHistory(habits, marks, days.map { it.toString() })
            .mapIndexed { index, value -> value.takeIf { days[index] <= today } }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { monthOffset-- }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(Res.string.previous_month),
                )
            }
            Text(
                text = "${monthNames.getOrElse(month.monthNumber - 1) { month.monthNumber.toString() }} ${month.year}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                textAlign = TextAlign.Center,
            )
            // The progress of the future is unknown.
            IconButton(onClick = { monthOffset++ }, enabled = monthOffset < 0) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(Res.string.next_month),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            repeat(7) { column ->
                Text(
                    text = dayNames.getOrNull((weekStart.dayOfWeek.ordinal + column) % 7).orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            days.indices.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    week.forEach { index ->
                        val day = days[index]
                        ProgressDay(
                            day = day.dayOfMonth,
                            progress = progress[index],
                            trackColor = trackColor,
                            progressColor = progressColor,
                            textColor = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .alpha(if (day.month == month.month) 1f else 0.4f),
                        )
                    }
                }
            }
        }
    }
}

/* Ring filled clockwise from the top by [progress], with the day number in the middle. */
@Composable
private fun ProgressDay(
    day: Int,
    progress: Float?,
    trackColor: Color,
    progressColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val percent = progress?.let { (it * 100).roundToInt() }
    Box(
        modifier = modifier.semantics {
            contentDescription = "$day: ${percent?.let { "$it%" } ?: "-"}"
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val progressStroke = stroke + 2.dp.toPx()
            // The thicker progress line has to fit into the cell.
            val diameter = min(size.width, size.height) - progressStroke - 2.dp.toPx()
            if (diameter <= 0f) return@Canvas
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2,
            )
            val arcSize = Size(diameter, diameter)
            drawArc(
                trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            if (progress != null && progress > 0f) {
                drawArc(
                    progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = progressStroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
    }
}
