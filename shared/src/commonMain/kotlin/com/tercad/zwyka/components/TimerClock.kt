// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import com.tercad.zwyka.core.time.ScheduledEvent
import com.tercad.zwyka.core.time.formatTime
import com.tercad.zwyka.defines.currentColorScheme

/** Minutes shown on the dial: half a day, like a watch. */
private const val DIAL_MINUTES = 12 * 60

/** Already passed minutes kept on the dial, the rest of it is ahead. */
private const val PAST_MINUTES = 60

/** Concentric lanes for overlapping events. */
private const val LANES = 3

/** Minimal gap between events of one lane, minutes: their round caps don't touch. */
private const val LANE_GAP_MINUTES = 10

/*
 * Watch-like dial of the next 12 hours.
 *
 *            ╭─ event ─╮
 *        11  12   1          Events are thick arcs around the hours,
 *      10   ╭───╮   2        overlapping ones are moved to the inner lanes.
 *      9    │1:25│ ──╯       The hand points at [now]; the center shows [content],
 *      8    ╰───╯   4        ringed by the progress of the current event.
 *        7   6   5
 */
@Composable
fun TimerClock(
    events: List<ScheduledEvent>,
    /** Minutes since the midnight of the first day of [events]. */
    now: Int,
    current: ScheduledEvent?,
    colorOf: (ScheduledEvent) -> Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colorScheme = currentColorScheme()
    val textMeasurer = rememberTextMeasurer()
    val numberStyle = MaterialTheme.typography.titleMedium.copy(
        color = colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
    val handLabelStyle = MaterialTheme.typography.labelMedium.copy(
        color = colorScheme.onError,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    )
    val visible = laneEvents(events, now)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        // Squeezed by the height in landscape: the dial is drawn by the smaller side.
        val discSize = minOf(maxWidth, maxHeight) * 0.38f
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val stroke = radius * 0.085f
            val laneGap = stroke * 0.3f
            fun laneRadius(lane: Int) = radius - 4.dp.toPx() - stroke / 2 - lane * (stroke + laneGap)
            val ticksRadius = laneRadius(LANES - 1) - stroke / 2 - laneGap * 2
            val numbersRadius = ticksRadius - 22.dp.toPx()
            val discRadius = discSize.toPx() / 2

            // Guides of the lanes and the face.
            for (lane in 0 until LANES) {
                drawCircle(
                    color = colorScheme.outlineVariant.copy(alpha = 0.35f),
                    radius = laneRadius(lane),
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            for (minute in 0 until 60) {
                val isHour = minute % 5 == 0
                val angle = minute / 60f * 2 * PI.toFloat() - PI.toFloat() / 2
                val length = if (isHour) 8.dp.toPx() else 3.dp.toPx()
                drawLine(
                    color = if (isHour) colorScheme.onSurfaceVariant else colorScheme.outlineVariant,
                    start = center + polar(ticksRadius - length, angle),
                    end = center + polar(ticksRadius, angle),
                    strokeWidth = if (isHour) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            for (hour in 1..12) {
                val angle = hour / 12f * 2 * PI.toFloat() - PI.toFloat() / 2
                val label = textMeasurer.measure(hour.toString(), numberStyle)
                val position = center + polar(numbersRadius, angle)
                drawText(
                    label,
                    topLeft = position - Offset(label.size.width / 2f, label.size.height / 2f),
                )
            }

            // Events: the passed part is faded, the current one stands out.
            visible.forEach { (event, lane) ->
                val color = colorOf(event)
                val laneSize = laneRadius(lane)
                val from = maxOf(event.start, now - PAST_MINUTES)
                val to = minOf(event.end, now + DIAL_MINUTES - PAST_MINUTES)
                drawEventArc(center, laneSize, stroke, from, to, color.copy(alpha = 0.3f))
                if (to > now) {
                    drawEventArc(center, laneSize, stroke, maxOf(from, now), to, color)
                }
            }

            // Progress of the current event around the center.
            val ringRadius = discRadius + 6.dp.toPx()
            drawCircle(
                color = colorScheme.outlineVariant.copy(alpha = 0.6f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 4.dp.toPx()),
            )
            if (current != null) {
                val progress = (now - current.start).toFloat() / (current.end - current.start)
                drawArc(
                    color = colorOf(current),
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = center - Offset(ringRadius, ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            drawCircle(color = colorScheme.surfaceVariant, radius = discRadius, center = center)

            // Hand of the current time with its label at the rim.
            val handAngle = minuteAngle(now)
            val handEnd = laneRadius(0) + stroke / 2
            drawLine(
                color = colorScheme.error,
                start = center + polar(ringRadius + 4.dp.toPx(), handAngle),
                end = center + polar(handEnd, handAngle),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            val label = textMeasurer.measure(formatTime(now % (24 * 60)), handLabelStyle)
            val padding = Offset(6.dp.toPx(), 2.dp.toPx())
            val pillSize = Size(label.size.width + padding.x * 2, label.size.height + padding.y * 2)
            val pillCenter = center + polar(handEnd - pillSize.width / 2, handAngle)
            drawRoundRect(
                color = colorScheme.error,
                topLeft = pillCenter - Offset(pillSize.width / 2, pillSize.height / 2),
                size = pillSize,
                cornerRadius = CornerRadius(pillSize.height / 2),
            )
            drawText(label, topLeft = pillCenter - Offset(label.size.width / 2f, label.size.height / 2f))
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                content()
            }
        }
    }
}

/** Events within the dial and their lanes (0 is the outer one). */
private fun laneEvents(events: List<ScheduledEvent>, now: Int): List<Pair<ScheduledEvent, Int>> {
    val first = now - PAST_MINUTES
    val last = first + DIAL_MINUTES
    val laneEnds = IntArray(LANES) { Int.MIN_VALUE }
    return events
        .filter { it.end > first && it.start < last }
        .map { event ->
            val start = maxOf(event.start, first)
            val lane = laneEnds.indices.firstOrNull { laneEnds[it] + LANE_GAP_MINUTES <= start } ?: (LANES - 1)
            laneEnds[lane] = maxOf(laneEnds[lane], event.end)
            event to lane
        }
}

private fun DrawScope.drawEventArc(center: Offset, radius: Float, stroke: Float, from: Int, to: Int, color: Color) {
    if (to <= from) return
    drawArc(
        color = color,
        startAngle = minuteAngle(from) * 180f / PI.toFloat(),
        sweepAngle = (to - from) / DIAL_MINUTES.toFloat() * 360f,
        useCenter = false,
        topLeft = center - Offset(radius, radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

/** Angle of [minutes] on the dial, radians; 12 o'clock is up. */
private fun minuteAngle(minutes: Int): Float =
    (minutes % DIAL_MINUTES) / DIAL_MINUTES.toFloat() * 2 * PI.toFloat() - PI.toFloat() / 2

private fun polar(radius: Float, angle: Float) = Offset(radius * cos(angle), radius * sin(angle))
