// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.DateFormat
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.progress.calculateProgressHistory
import com.tercad.zwyka.core.progress.toMarks
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.metrics
import com.tercad.zwyka.resources.metrics_completion
import com.tercad.zwyka.resources.metrics_days
import com.tercad.zwyka.resources.months_short

/** Number of days (including today) shown on the progress graph. */
private const val METRICS_DAYS = 66

class MetricsPage(
    private val habits: List<Habit>,
    private val dayStates: List<DayState>,
    private val today: LocalDate,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            containerColor = currentColorScheme().surface,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(currentColorScheme().primary),
                ) {
                    TextButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(Res.string.back),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.metrics),
                    style = MaterialTheme.typography.titleLarge,
                    color = currentColorScheme().onSurface,
                )

                val dates = remember(today) {
                    (METRICS_DAYS - 1 downTo 0).map { offset ->
                        today.minus(offset, DateTimeUnit.DAY).toString()
                    }
                }
                val progress = remember(habits, dayStates, dates) {
                    calculateProgressHistory(habits, dayStates.toMarks(), dates)
                }

                ProgressGraph(
                    dates = dates,
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

/*
 * Line graph of the daily progress.
 *
 *     ^ Completion, %
 *     |      •
 *     |   •     •
 *     +--------------> Days
 */
@Composable
private fun ProgressGraph(
    dates: List<String>,
    progress: List<Float?>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = currentColorScheme()
    val axisColor = colorScheme.onSurface
    val gridColor = colorScheme.outlineVariant
    val labelColor = colorScheme.onSurfaceVariant
    val lineColor = colorScheme.primary
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val titleStyle = MaterialTheme.typography.labelMedium.copy(color = axisColor)
    val textMeasurer = rememberTextMeasurer()
    val monthNames = stringArrayResource(Res.array.months_short)
    val daysTitle = stringResource(Res.string.metrics_days)
    val completionTitle = stringResource(Res.string.metrics_completion)
    val dayLabels = remember(dates, monthNames) {
        dates.map { DateFormat.DD_MMM.format(it, monthNames) }
    }
    val description = dates.zip(progress)
        .joinToString { (date, value) -> "$date: ${value?.let { "${(it * 100).roundToInt()}%" } ?: "-"}" }

    Canvas(
        modifier = modifier.semantics { contentDescription = description },
    ) {
        val arrowSize = 6.dp.toPx()
        val tickSize = 4.dp.toPx()
        val gap = 4.dp.toPx()
        val percentLabelWidth = textMeasurer.measure("100%", labelStyle).size.width
        val dayLabelHeight = textMeasurer.measure(dayLabels.first(), labelStyle).size.height
        val titleHeight = textMeasurer.measure(completionTitle, titleStyle).size.height

        // Axes origin and the arrow tips.
        val originX = percentLabelWidth + gap * 2
        val originY = size.height - dayLabelHeight - tickSize - gap * 2 - titleHeight
        val topY = titleHeight + gap
        val rightX = size.width

        // Plot area keeps a margin from the arrow tips.
        val plotLeft = originX + gap * 2
        val plotRight = rightX - arrowSize * 3
        val plotTop = topY + arrowSize * 3
        val plotHeight = originY - plotTop
        if (plotHeight <= 0f || plotRight <= plotLeft) return@Canvas

        fun xOf(index: Int): Float =
            if (dates.size <= 1) plotLeft
            else plotLeft + (plotRight - plotLeft) * index / (dates.size - 1)

        fun yOf(value: Float): Float = originY - plotHeight * value.coerceIn(0f, 1f)

        // Horizontal grid with percentage labels.
        for (percent in 0..100 step 25) {
            val y = yOf(percent / 100f)
            if (percent > 0) {
                drawLine(gridColor, Offset(originX, y), Offset(plotRight, y), 1.dp.toPx())
            }
            val label = textMeasurer.measure("$percent%", labelStyle)
            drawText(
                label,
                topLeft = Offset(
                    originX - gap * 2 - label.size.width,
                    y - label.size.height / 2,
                ),
            )
        }

        // Day ticks with labels, counting back from the last day: every N-th day,
        // where N is the fewest days whose spacing fits the widest label.
        val measuredDays = dayLabels.map { textMeasurer.measure(it, labelStyle) }
        val labelSlot = measuredDays.maxOf { it.size.width } + gap * 2
        val daySpacing = if (dates.size > 1) (plotRight - plotLeft) / (dates.size - 1) else labelSlot
        val labelStep = ceil(labelSlot / daySpacing).toInt().coerceAtLeast(1)
        var freeRight = size.width
        for (index in dates.indices.reversed() step labelStep) {
            val x = xOf(index)
            drawLine(axisColor, Offset(x, originY), Offset(x, originY + tickSize), 1.dp.toPx())
            val label = measuredDays[index]
            val left = (x - label.size.width / 2).coerceIn(0f, size.width - label.size.width)
            // Edge labels are shifted inwards: skip one that would still overlap its neighbour.
            if (left + label.size.width > freeRight) continue
            drawText(label, topLeft = Offset(left, originY + tickSize + gap))
            freeRight = left - gap
        }

        // Axes with arrows.
        drawArrow(axisColor, Offset(originX, originY), Offset(rightX, originY), arrowSize)
        drawArrow(axisColor, Offset(originX, originY), Offset(originX, topY), arrowSize)

        // Axis titles next to the arrow tips.
        val daysLabel = textMeasurer.measure(daysTitle, titleStyle)
        drawText(
            daysLabel,
            topLeft = Offset(
                rightX - daysLabel.size.width,
                size.height - daysLabel.size.height,
            ),
        )
        drawText(
            textMeasurer.measure(completionTitle, titleStyle),
            topLeft = Offset(originX + gap * 2, 0f),
        )

        // Progress line, broken on days without applicable habits.
        val stroke = 2.dp.toPx()
        var line: Path? = null
        progress.forEachIndexed { index, value ->
            if (value == null) {
                line?.let { drawPath(it, lineColor, style = Stroke(stroke, cap = StrokeCap.Round)) }
                line = null
                return@forEachIndexed
            }
            val point = Offset(xOf(index), yOf(value))
            line = (line ?: Path().apply { moveTo(point.x, point.y) }).apply {
                lineTo(point.x, point.y)
            }
            drawCircle(lineColor, radius = stroke * 1.25f, center = point)
        }
        line?.let { drawPath(it, lineColor, style = Stroke(stroke, cap = StrokeCap.Round)) }
    }
}

private fun DrawScope.drawArrow(
    color: Color,
    from: Offset,
    to: Offset,
    arrowSize: Float,
) {
    val stroke = 1.5.dp.toPx()
    drawLine(color, from, to, stroke)
    val direction = (to - from).let { it / it.getDistance() }
    val normal = Offset(-direction.y, direction.x)
    val base = to - direction * arrowSize * 1.5f
    drawPath(
        Path().apply {
            moveTo(to.x, to.y)
            lineTo(base.x + normal.x * arrowSize, base.y + normal.y * arrowSize)
            lineTo(base.x - normal.x * arrowSize, base.y - normal.y * arrowSize)
            close()
        },
        color,
    )
}
