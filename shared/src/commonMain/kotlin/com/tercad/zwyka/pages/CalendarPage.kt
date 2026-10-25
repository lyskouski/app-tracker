// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.math.floor
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.Intent
import com.tercad.zwyka.core.model.MINUTES_PER_DAY
import com.tercad.zwyka.core.model.isActiveOn
import com.tercad.zwyka.core.service.Schedule
import com.tercad.zwyka.core.time.formatTime
import com.tercad.zwyka.components.HabitTimeDialog
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit_time
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.days_short
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.months_short
import com.tercad.zwyka.resources.next_day
import com.tercad.zwyka.resources.notify
import com.tercad.zwyka.resources.previous_day
import com.tercad.zwyka.resources.timer
import com.tercad.zwyka.resources.today
import com.tercad.zwyka.state.AppSettingsStore

/** Snap of a click on an empty slot, minutes. */
private const val SLOT_MINUTES = 30

class CalendarPage(
    private val services: Services,
    private val settingsStore: AppSettingsStore,
) : Screen {
    @Composable
    override fun Content() {
        CalendarPageContent(services, settingsStore)
    }
}

/*
 * Editing target: an existing [HabitTime] or a new one starting at [start].
 */
private data class HabitTimeEditing(
    val habitTime: HabitTime?,
    val start: Int,
)

@Composable
private fun CalendarPageContent(
    services: Services,
    settingsStore: AppSettingsStore,
) {
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    val unknownDatabaseError = stringResource(Res.string.database_error_unknown)
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    var selectedDate by remember { mutableStateOf(today) }
    var schedule by remember { mutableStateOf<Schedule?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<HabitTimeEditing?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(services) {
        try {
            schedule = services.schedule.load()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            loadError = error.message ?: unknownDatabaseError
        }
    }

    Scaffold(
        containerColor = currentColorScheme().surface,
        topBar = {
            CalendarTopBar(
                selectedDate = selectedDate,
                onPrevious = { selectedDate = selectedDate.minus(1, DateTimeUnit.DAY) },
                onToday = { selectedDate = today },
                onNext = { selectedDate = selectedDate.plus(1, DateTimeUnit.DAY) },
                settingsStore = settingsStore,
            )
        },
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
                TextButton(
                    onClick = { navigator.push(TimerPage(services)) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = currentColorScheme().onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(Res.string.timer),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val start = if (selectedDate == today) ((now.hour + 1) * 60).coerceAtMost(23 * 60) else 9 * 60
                    saveError = null
                    editing = HabitTimeEditing(null, start)
                },
                modifier = Modifier.offset(y = 52.dp),
                containerColor = currentColorScheme().inversePrimary,
                contentColor = currentColorScheme().secondary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.add_habit_time),
                )
            }
        },
    ) { contentPadding ->
        val loaded = schedule
        when {
            loadError != null -> Text(
                stringResource(Res.string.habits_load_failed, loadError.orEmpty()),
                modifier = Modifier.padding(contentPadding),
            )
            loaded == null -> Text(
                stringResource(Res.string.habits_loading),
                modifier = Modifier.padding(contentPadding),
            )
            else -> DayTimeline(
                date = selectedDate,
                isToday = selectedDate == today,
                habits = loaded.habits,
                intents = loaded.intents,
                habitTimes = loaded.habitTimes.filter { it.isActiveOn(selectedDate.dayOfWeek) },
                onSlotClick = { start ->
                    saveError = null
                    editing = HabitTimeEditing(null, start)
                },
                onHabitTimeClick = { habitTime ->
                    saveError = null
                    editing = HabitTimeEditing(habitTime, habitTime.timeStart)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .swipeDays(
                        onPrevious = { selectedDate = selectedDate.minus(1, DateTimeUnit.DAY) },
                        onNext = { selectedDate = selectedDate.plus(1, DateTimeUnit.DAY) },
                    ),
            )
        }
    }

    editing?.let { current ->
        fun persist(action: suspend () -> Schedule) {
            isSaving = true
            saveError = null
            coroutineScope.launch {
                try {
                    schedule = action()
                    editing = null
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    saveError = error.message ?: unknownDatabaseError
                } finally {
                    isSaving = false
                }
            }
        }

        HabitTimeDialog(
            habitTime = current.habitTime,
            habits = schedule?.habits.orEmpty(),
            intents = schedule?.intents.orEmpty(),
            initialStart = current.start,
            date = selectedDate,
            isSaving = isSaving,
            error = saveError,
            weekStart = settingsStore.current.weekStart,
            onDismiss = { editing = null },
            onSave = { draft ->
                persist {
                    services.schedule.save(current.habitTime, draft, settingsStore.current.weekStart)
                }
            },
            onDelete = current.habitTime?.let { habitTime ->
                {
                    persist {
                        services.schedule.delete(habitTime, settingsStore.current.weekStart)
                    }
                }
            },
            onNotifyEnabled = services.reminders::requestPermission,
        )
    }
}

/*
 * Swipe right shows the previous day, swipe left the next one.
 * Horizontal drags only, so the vertical scroll of hours keeps working.
 */
private fun Modifier.swipeDays(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
): Modifier = pointerInput(Unit) {
    val threshold = 64.dp.toPx()
    var total = 0f
    detectHorizontalDragGestures(
        onDragStart = { total = 0f },
        onDragEnd = {
            when {
                total > threshold -> onPrevious()
                total < -threshold -> onNext()
            }
        },
    ) { change, dragAmount ->
        change.consume()
        total += dragAmount
    }
}

/*
 * [<] [Today] [>]  Mon, 2026-10-17
 */
@Composable
private fun CalendarTopBar(
    selectedDate: LocalDate,
    onPrevious: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
    settingsStore: AppSettingsStore,
) {
    val dayNames = stringArrayResource(Res.array.days_short)
    val monthNames = stringArrayResource(Res.array.months_short)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(currentColorScheme().surfaceVariant)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(Res.string.previous_day),
            )
        }
        OutlinedButton(onClick = onToday) {
            Text(stringResource(Res.string.today))
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(Res.string.next_day),
            )
        }
        Text(
            text = "${dayNames.getOrElse(selectedDate.dayOfWeek.ordinal) { "" }}, " +
                settingsStore.current.dateFormat.format(selectedDate.toString(), monthNames),
            style = MaterialTheme.typography.titleMedium,
            color = currentColorScheme().onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/*
 * Single day view: hours on the left, time ranges laid out side by side when overlapping.
 */
@Composable
private fun DayTimeline(
    date: LocalDate,
    isToday: Boolean,
    habits: List<Habit>,
    intents: List<Intent>,
    habitTimes: List<HabitTime>,
    onSlotClick: (Int) -> Unit,
    onHabitTimeClick: (HabitTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hourHeight = 56.dp
    val hoursWidth = 56.dp
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val scrollState = rememberScrollState()
    val habitsById = remember(habits) { habits.associateBy { it.id } }
    val intentsById = remember(intents) { intents.associateBy { it.id } }
    val placed = remember(habitTimes) { layoutHabitTimes(habitTimes) }
    val gridColor = currentColorScheme().outlineVariant
    val nowColor = currentColorScheme().error
    val nowMinutes = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .let { it.hour * 60 + it.minute }

    // Start around the current hour, like other calendars do.
    LaunchedEffect(Unit) {
        scrollState.scrollTo((hourHeightPx * (nowMinutes / 60 - 1).coerceAtLeast(0)).toInt())
    }

    Row(
        modifier = modifier.verticalScroll(scrollState),
    ) {
        Column(modifier = Modifier.width(hoursWidth)) {
            for (hour in 0 until 24) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight),
                ) {
                    Text(
                        text = formatTime(hour * 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = currentColorScheme().onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 8.dp),
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(hourHeight * 24),
        ) {
            val columnWidth = maxWidth

            // Empty grid: click picks the slot under the pointer.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        for (hour in 0 until 24) {
                            val y = hour * hourHeightPx
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                            drawLine(
                                gridColor.copy(alpha = gridColor.alpha * 0.4f),
                                Offset(0f, y + hourHeightPx / 2),
                                Offset(size.width, y + hourHeightPx / 2),
                                1.dp.toPx(),
                            )
                        }
                        drawLine(gridColor, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
                    }
                    .pointerInput(date, hourHeightPx) {
                        detectTapGestures { offset ->
                            val slot = floor(offset.y / hourHeightPx * 60 / SLOT_MINUTES).toInt()
                            onSlotClick(
                                (slot * SLOT_MINUTES).coerceIn(0, MINUTES_PER_DAY - SLOT_MINUTES),
                            )
                        }
                    },
            )

            placed.forEach { item ->
                val habit = habitsById[item.habitTime.habitId]
                val intent = intentsById[item.habitTime.intentId]
                HabitTimeBlock(
                    habitTime = item.habitTime,
                    title = habit?.title ?: intent?.title.orEmpty(),
                    color = habit?.color ?: intent?.color,
                    modifier = Modifier
                        .offset(
                            x = columnWidth * item.column / item.columns,
                            y = hourHeight * item.habitTime.timeStart / 60,
                        )
                        .width(columnWidth / item.columns)
                        .height(hourHeight * (item.habitTime.timeEnd - item.habitTime.timeStart) / 60)
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    hourHeight = hourHeight,
                    onClick = { onHabitTimeClick(item.habitTime) },
                )
            }

            if (isToday) {
                Box(
                    modifier = Modifier
                        .offset(y = hourHeight * nowMinutes / 60 - 1.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(nowColor),
                )
            }
        }
    }
}

@Composable
private fun HabitTimeBlock(
    habitTime: HabitTime,
    title: String,
    color: String?,
    hourHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = applicationColor(color) ?: currentColorScheme().primary
    val range = "${formatTime(habitTime.timeStart)} – ${formatTime(habitTime.timeEnd)}"
    val isShort = hourHeight * (habitTime.timeEnd - habitTime.timeStart) / 60 < hourHeight * 0.75f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .drawBehind {
                drawRect(color, size = size.copy(width = 3.dp.toPx()))
            }
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$title, $range" }
            .padding(start = 6.dp, end = 4.dp, top = 2.dp),
    ) {
        if (habitTime.notify) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = stringResource(Res.string.notify),
                tint = color,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp),
            )
        }
        Column(modifier = Modifier.padding(end = if (habitTime.notify) 14.dp else 0.dp)) {
            Text(
                text = if (isShort) "$title, ${formatTime(habitTime.timeStart)}" else title,
                style = MaterialTheme.typography.labelMedium,
                color = currentColorScheme().onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isShort) {
                Text(
                    text = range,
                    style = MaterialTheme.typography.labelSmall,
                    color = currentColorScheme().onSurfaceVariant,
                    maxLines = 1,
                )
                habitTime.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = currentColorScheme().onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private data class PlacedHabitTime(
    val habitTime: HabitTime,
    val column: Int,
    val columns: Int,
)

/*
 * Splits overlapping time ranges into side-by-side columns.
 *
 * Ranges are grouped into clusters of transitively overlapping items;
 * every item in a cluster shares the width with the cluster's column count.
 */
private fun layoutHabitTimes(habitTimes: List<HabitTime>): List<PlacedHabitTime> {
    val result = mutableListOf<PlacedHabitTime>()
    val cluster = mutableListOf<Pair<HabitTime, Int>>()
    val columnEnds = mutableListOf<Int>()
    var clusterEnd = -1

    fun flush() {
        cluster.forEach { (habitTime, column) ->
            result += PlacedHabitTime(habitTime, column, columnEnds.size)
        }
        cluster.clear()
        columnEnds.clear()
    }

    habitTimes.sortedWith(compareBy({ it.timeStart }, { it.timeEnd })).forEach { habitTime ->
        if (habitTime.timeStart >= clusterEnd) flush()
        var column = columnEnds.indexOfFirst { it <= habitTime.timeStart }
        if (column < 0) {
            column = columnEnds.size
            columnEnds += habitTime.timeEnd
        } else {
            columnEnds[column] = habitTime.timeEnd
        }
        cluster += habitTime to column
        clusterEnd = if (cluster.size == 1) habitTime.timeEnd else maxOf(clusterEnd, habitTime.timeEnd)
    }
    flush()
    return result
}
