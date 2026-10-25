// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.components.TimerClock
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.MINUTES_PER_DAY
import com.tercad.zwyka.core.service.Schedule
import com.tercad.zwyka.core.time.ScheduledEvent
import com.tercad.zwyka.core.time.currentAt
import com.tercad.zwyka.core.time.formatDuration
import com.tercad.zwyka.core.time.formatTime
import com.tercad.zwyka.core.time.scheduledEvents
import com.tercad.zwyka.core.time.upcomingAfter
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.timer_free
import com.tercad.zwyka.resources.timer_in
import com.tercad.zwyka.resources.timer_left
import com.tercad.zwyka.resources.timer_next_events
import com.tercad.zwyka.resources.timer_no_events
import com.tercad.zwyka.resources.timer_now
import com.tercad.zwyka.resources.timer_until_start
import com.tercad.zwyka.resources.tomorrow

/** Number of upcoming events listed under the clock. */
private const val NEXT_EVENTS = 5

/** Countdown of the current scheduled habit, the dial of the next hours and the upcoming events. */
class TimerPage(
    private val services: Services,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unknownDatabaseError = stringResource(Res.string.database_error_unknown)
        var schedule by remember { mutableStateOf<Schedule?>(null) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var dateTime by remember { mutableStateOf(currentDateTime()) }
        LaunchedEffect(services) {
            try {
                schedule = services.schedule.load()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                loadError = error.message ?: unknownDatabaseError
            }
        }
        LaunchedEffect(Unit) {
            while (true) {
                dateTime = currentDateTime()
                delay(1000)
            }
        }

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
            val loaded = schedule
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .statusBarsPadding()
                    .padding(16.dp),
            ) {
                when {
                    loadError != null -> Text(stringResource(Res.string.habits_load_failed, loadError.orEmpty()))
                    loaded == null -> Text(stringResource(Res.string.habits_loading))
                    else -> TimerContent(loaded, dateTime, isHorizontal = maxWidth > maxHeight)
                }
            }
        }
    }
}

private fun currentDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

@Composable
private fun TimerContent(schedule: Schedule, dateTime: kotlinx.datetime.LocalDateTime, isHorizontal: Boolean) {
    val events = remember(schedule, dateTime.date) { scheduledEvents(schedule.habitTimes, dateTime.date) }
    val seconds = dateTime.hour * 3600 + dateTime.minute * 60 + dateTime.second
    val now = seconds / 60
    val current = events.currentAt(now)
    val upcoming = events.upcomingAfter(now)
    val next = upcoming.firstOrNull()
    val defaultColor = currentColorScheme().primary
    val colorOf = { event: ScheduledEvent -> applicationColor(schedule.colorOf(event)) ?: defaultColor }

    val clock = @Composable { modifier: Modifier ->
        TimerClock(
            events = events,
            now = now,
            current = current,
            colorOf = colorOf,
            modifier = modifier,
        ) {
            // Rounded up: "0:01" is shown until the very end.
            val target = current?.end ?: next?.start
            if (target != null) {
                val minutesLeft = (target * 60 - seconds + 59) / 60
                Text(
                    text = formatDuration(minutesLeft),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = currentColorScheme().onSurface,
                )
                Text(
                    text = stringResource(if (current != null) Res.string.timer_left else Res.string.timer_until_start),
                    style = MaterialTheme.typography.labelMedium,
                    color = currentColorScheme().onSurfaceVariant,
                )
            } else {
                Text(
                    text = formatTime(now),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = currentColorScheme().onSurface,
                )
            }
        }
    }
    val nextEvents = @Composable {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.timer_next_events),
                style = MaterialTheme.typography.titleMedium,
                color = currentColorScheme().onSurface,
            )
            if (upcoming.isEmpty()) {
                Text(
                    text = stringResource(Res.string.timer_no_events),
                    color = currentColorScheme().onSurfaceVariant,
                )
            }
            upcoming.take(NEXT_EVENTS).forEach { event ->
                UpcomingEvent(schedule, event, colorOf(event), minutesUntil = event.start - now)
            }
        }
    }

    // Landscape: the clock on the left, the current and next events on the right.
    if (isHorizontal) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                clock(Modifier)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CurrentEvent(schedule, current, colorOf)
                nextEvents()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CurrentEvent(schedule, current, colorOf)
            clock(Modifier.widthIn(max = 420.dp))
            nextEvents()
        }
    }
}

/*
 * The scheduled habit in progress, or free time.
 */
@Composable
private fun CurrentEvent(schedule: Schedule, current: ScheduledEvent?, colorOf: (ScheduledEvent) -> Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.timer_now),
            style = MaterialTheme.typography.labelLarge,
            color = currentColorScheme().onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            if (current != null) {
                val color = colorOf(current)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    applicationIcon(schedule.iconOf(current))?.let { icon ->
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                    }
                }
                Column {
                    Text(
                        text = schedule.titleOf(current),
                        style = MaterialTheme.typography.headlineSmall,
                        color = currentColorScheme().onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${formatTime(current.habitTime.timeStart)} – ${formatTime(current.habitTime.timeEnd)}" +
                            (current.habitTime.note?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentColorScheme().onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.timer_free),
                    style = MaterialTheme.typography.headlineSmall,
                    color = currentColorScheme().onSurface,
                )
            }
        }
    }
}

@Composable
private fun UpcomingEvent(schedule: Schedule, event: ScheduledEvent, color: Color, minutesUntil: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(currentColorScheme().surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(36.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = schedule.titleOf(event),
                style = MaterialTheme.typography.bodyLarge,
                color = currentColorScheme().onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val day = if (event.start >= MINUTES_PER_DAY) "${stringResource(Res.string.tomorrow)}, " else ""
            Text(
                text = "$day${formatTime(event.habitTime.timeStart)} – ${formatTime(event.habitTime.timeEnd)}",
                style = MaterialTheme.typography.bodySmall,
                color = currentColorScheme().onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(Res.string.timer_in, formatDuration(minutesUntil)),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

private fun Schedule.titleOf(event: ScheduledEvent): String =
    habits.firstOrNull { it.id == event.habitTime.habitId }?.title
        ?: intents.firstOrNull { it.id == event.habitTime.intentId }?.title.orEmpty()

private fun Schedule.colorOf(event: ScheduledEvent): String? =
    habits.firstOrNull { it.id == event.habitTime.habitId }?.color
        ?: intents.firstOrNull { it.id == event.habitTime.intentId }?.color

private fun Schedule.iconOf(event: ScheduledEvent): String? =
    habits.firstOrNull { it.id == event.habitTime.habitId }?.icon
        ?: intents.firstOrNull { it.id == event.habitTime.intentId }?.icon
