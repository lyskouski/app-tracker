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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.components.DayStateDialog
import com.tercad.zwyka.components.PomodoroDialog
import com.tercad.zwyka.components.TimerClock
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.HabitTime
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
import com.tercad.zwyka.defines.rememberVibration
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_pomodoro
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.pomodoro
import com.tercad.zwyka.resources.stop
import com.tercad.zwyka.resources.timer
import com.tercad.zwyka.resources.timer_free
import com.tercad.zwyka.resources.timer_in
import com.tercad.zwyka.resources.timer_left
import com.tercad.zwyka.resources.timer_next_events
import com.tercad.zwyka.resources.timer_no_events
import com.tercad.zwyka.resources.timer_now
import com.tercad.zwyka.resources.timer_until_start
import com.tercad.zwyka.resources.tomorrow
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.widgets.BottomBar
import com.tercad.zwyka.widgets.BottomBarButton
import com.tercad.zwyka.widgets.ErrorMessage
import com.tercad.zwyka.widgets.Heading
import com.tercad.zwyka.widgets.StatusMessage
import com.tercad.zwyka.widgets.readableOn
import com.tercad.zwyka.widgets.screenTitle

/** Number of upcoming events listed under the clock. */
private const val NEXT_EVENTS = 5

/** Countdown of the current scheduled habit, the dial of the next hours and the upcoming events. */
class TimerPage(
    private val services: Services,
    private val settingsStore: AppSettingsStore,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val unknownDatabaseError = stringResource(Res.string.database_error_unknown)
        var schedule by remember { mutableStateOf<Schedule?>(null) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var dateTime by remember { mutableStateOf(currentDateTime()) }
        var pomodoro by remember { mutableStateOf<Pomodoro?>(null) }
        var showPomodoroDialog by remember { mutableStateOf(false) }
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
            // The countdown stays visible while the page is open.
            modifier = Modifier
                .keepScreenOn()
                .screenTitle(stringResource(Res.string.timer)),
            containerColor = currentColorScheme().surface,
            bottomBar = {
                BottomBar {
                    BottomBarButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        text = stringResource(Res.string.back),
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                // Same as on the home page: Scaffold lifts the FAB 16.dp above the bar, it protrudes 12.dp.
                FloatingActionButton(
                    onClick = { showPomodoroDialog = true },
                    modifier = Modifier.offset(y = 52.dp),
                    containerColor = currentColorScheme().inversePrimary,
                    contentColor = currentColorScheme().secondary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.add_pomodoro),
                    )
                }
            },
        ) { contentPadding ->
            val loaded = schedule
            if (showPomodoroDialog && loaded != null) {
                PomodoroDialog(
                    habits = loaded.habits,
                    onStart = { minutes, habitId ->
                        val startedAt = Clock.System.now()
                        pomodoro = Pomodoro(habitId, startedAt, startedAt + minutes.minutes)
                        showPomodoroDialog = false
                    },
                    onDismiss = { showPomodoroDialog = false },
                )
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .statusBarsPadding()
                    .padding(16.dp),
            ) {
                when {
                    loadError != null -> ErrorMessage(stringResource(Res.string.habits_load_failed, loadError.orEmpty()))
                    loaded == null -> StatusMessage(stringResource(Res.string.habits_loading))
                    else -> TimerContent(
                        services, settingsStore, loaded, dateTime, isHorizontal = maxWidth > maxHeight,
                        pomodoro = pomodoro,
                        onPomodoroEnd = { pomodoro = null },
                    )
                }
            }
        }
    }
}

private fun currentDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** Custom timer, optionally for a habit (its completion is asked at the end). */
private data class Pomodoro(
    val habitId: Long?,
    val startedAt: Instant,
    val endsAt: Instant,
)

@Composable
private fun TimerContent(
    services: Services,
    settingsStore: AppSettingsStore,
    schedule: Schedule,
    dateTime: kotlinx.datetime.LocalDateTime,
    isHorizontal: Boolean,
    pomodoro: Pomodoro?,
    onPomodoroEnd: () -> Unit,
) {
    val events = remember(schedule, dateTime.date) { scheduledEvents(schedule.habitTimes, dateTime.date) }
    val seconds = dateTime.hour * 3600 + dateTime.minute * 60 + dateTime.second
    val now = seconds / 60
    // A running custom timer is shown on the dial and counted down instead of the scheduled event.
    val pomodoroEvent = pomodoro?.let { timer ->
        val start = timer.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val startMinute = (start.date.toEpochDays() - dateTime.date.toEpochDays()).toInt() * MINUTES_PER_DAY +
            start.hour * 60 + start.minute
        val endMinute = startMinute + ((timer.endsAt - timer.startedAt).inWholeMinutes).toInt()
        ScheduledEvent(HabitTime(id = -1, habitId = timer.habitId, timeStart = startMinute, timeEnd = endMinute), startMinute, endMinute)
    }
    val dialEvents = if (pomodoroEvent == null) events else (events + pomodoroEvent).sortedBy { it.start }
    val current = pomodoroEvent ?: events.currentAt(now)
    val upcoming = events.upcomingAfter(now)
    val next = upcoming.firstOrNull()
    val defaultColor = currentColorScheme().primary
    val colorOf = { event: ScheduledEvent -> applicationColor(schedule.colorOf(event)) ?: defaultColor }

    /*
     * While the page is open, every minute is checked once:
     * a started event vibrates, an ended habit asks for its completion (intents are not tracked).
     */
    val vibrate = rememberVibration()
    val coroutineScope = rememberCoroutineScope()
    var checkedMinute by remember { mutableStateOf(now) }
    var endedDayState by remember { mutableStateOf<DayState?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    // Opens the completion of the habit for today, unless another one is asked already.
    val askCompletion: suspend (Long) -> Unit = { habitId ->
        if (endedDayState == null) {
            val date = dateTime.date.toString()
            val mark = services.dayStates.getRange(date, date).firstOrNull { it.habitId == habitId }?.mark
            saveError = null
            endedDayState = DayState(habitId = habitId, date = date, mark = mark ?: "·")
        }
    }
    LaunchedEffect(now) {
        // After midnight the minutes start over.
        val previous = if (now < checkedMinute) -1 else checkedMinute
        checkedMinute = now
        if (now == previous) return@LaunchedEffect
        fun Int.isJustReached() = this in (previous + 1)..now && now - this <= 1
        if (events.any { it.start.isJustReached() }) {
            vibrate()
        }
        val ended = events.firstOrNull { it.habitTime.habitId != null && it.end.isJustReached() }
        ended?.habitTime?.habitId?.let { askCompletion(it) }
    }
    LaunchedEffect(pomodoro) {
        val timer = pomodoro ?: return@LaunchedEffect
        delay(timer.endsAt - Clock.System.now())
        vibrate()
        onPomodoroEnd()
        timer.habitId?.let { askCompletion(it) }
    }
    endedDayState?.let { dayState ->
        schedule.habits.firstOrNull { it.id == dayState.habitId }?.let { habit ->
            DayStateDialog(
                dayState = dayState,
                habit = habit,
                dateFormat = settingsStore.current.dateFormat,
                completenessType = settingsStore.current.completenessType,
                saveError = saveError,
                isSaving = isSaving,
                onStatusChange = { mark ->
                    isSaving = true
                    saveError = null
                    coroutineScope.launch {
                        try {
                            services.dayStates.setMark(dayState.copy(mark = mark))
                            endedDayState = null
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            saveError = error.message.orEmpty()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                onDismiss = { endedDayState = null },
            )
        }
    }

    val clock = @Composable { modifier: Modifier ->
        TimerClock(
            events = dialEvents,
            now = now,
            current = current,
            colorOf = colorOf,
            modifier = modifier,
        ) {
            // Rounded up: "0:01" is shown until the very end.
            val secondsLeft = pomodoro?.let { (it.endsAt - Clock.System.now()).inWholeSeconds.toInt().coerceAtLeast(0) }
                ?: (current?.end ?: next?.start)?.let { it * 60 - seconds }
            if (secondsLeft != null) {
                val minutesLeft = (secondsLeft + 59) / 60
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
            Heading(
                text = stringResource(Res.string.timer_next_events),
                style = MaterialTheme.typography.titleMedium,
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
                CurrentEvent(schedule, current, colorOf, isPomodoro = pomodoro != null, onStop = onPomodoroEnd)
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
            CurrentEvent(schedule, current, colorOf, isPomodoro = pomodoro != null, onStop = onPomodoroEnd)
            clock(Modifier.widthIn(max = 420.dp))
            nextEvents()
        }
    }
}

/*
 * The scheduled habit in progress, or free time.
 */
@Composable
private fun CurrentEvent(
    schedule: Schedule,
    current: ScheduledEvent?,
    colorOf: (ScheduledEvent) -> Color,
    isPomodoro: Boolean,
    onStop: () -> Unit,
) {
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
                    (applicationIcon(schedule.iconOf(current)) ?: Icons.Filled.Timer.takeIf { isPomodoro })?.let { icon ->
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = schedule.titleOf(current).ifEmpty { stringResource(Res.string.pomodoro) },
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
                if (isPomodoro) {
                    TextButton(onClick = onStop) {
                        Text(stringResource(Res.string.stop))
                    }
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
            color = color.readableOn(currentColorScheme().surfaceVariant.copy(alpha = 0.5f).compositeOver(currentColorScheme().surface)),
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
