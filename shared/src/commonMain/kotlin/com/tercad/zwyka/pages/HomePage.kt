// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import com.tercad.zwyka.core.model.CellKey
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.isBeforeCreation
import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.components.DayStateDialog
import com.tercad.zwyka.components.HabitEditorDialog
import com.tercad.zwyka.components.SegmentedSelector
import com.tercad.zwyka.components.TodayHabitList
import com.tercad.zwyka.components.WeeklyHabitList
import com.tercad.zwyka.core.time.currentWeekDates
import com.tercad.zwyka.defines.HomeView
import com.tercad.zwyka.defines.homeViewOptions
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.sortedBy
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.calendar
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.delete
import com.tercad.zwyka.resources.delete_day_state_confirmation
import com.tercad.zwyka.resources.habit_database_unavailable
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.state.AppSettingsStore

class HomePage(
    private val services: Services?,
    private val initializeDatabase: suspend () -> Unit,
    private val settingsStore: AppSettingsStore,
) : Screen {
    @Composable
    override fun Content() {
        HomePageContent(services, initializeDatabase, settingsStore)
    }
}

@Composable
private fun HomePageContent(
    services: Services?,
    initializeDatabase: suspend () -> Unit,
    settingsStore: AppSettingsStore,
) {
    val navigator = LocalNavigator.currentOrThrow
    if (services == null) {
        Box(Modifier.fillMaxSize()) {
            Text(stringResource(Res.string.habit_database_unavailable))
        }
        return
    }

    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val dates = remember(today) {
        (-66..5).map { dayOffset ->
            today.plus(dayOffset, DateTimeUnit.DAY).toString()
        }
    }
    // The whole current week is shown by the "Weekly" view, whatever the week start is.
    val lastLoadedDate = remember(today) { today.plus(6, DateTimeUnit.DAY).toString() }

    var state by remember(services) {
        mutableStateOf<HomePageState>(HomePageState.Loading)
    }
    var showHabitDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var isHabitSaving by remember { mutableStateOf(false) }
    var habitError by remember { mutableStateOf<String?>(null) }
    var selectedDayState by remember { mutableStateOf<DayState?>(null) }
    var dayStateSaveError by remember { mutableStateOf<String?>(null) }
    var isDayStateSaving by remember { mutableStateOf(false) }
    var deletingDayState by remember { mutableStateOf<DayState?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val unknownDatabaseError = stringResource(Res.string.database_error_unknown)

    // Reloaded on a new day as well: its inactive days have to be stored.
    LaunchedEffect(services, today) {
        state = HomePageState.Loading
        try {
            initializeDatabase()
            services.habits.fillInactiveDays(settingsStore.current.weekStart)
            val habits = services.habits.getAll()
            val dayStates = services.dayStates.getRange(dates.first(), lastLoadedDate)
            state = HomePageState.Loaded(habits, dayStates)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            state = HomePageState.Failed(error.message)
        }
    }

    when (val current = state) {
        HomePageState.Loading -> Text(stringResource(Res.string.habits_loading))
        is HomePageState.Failed -> Text(
            stringResource(
                Res.string.habits_load_failed,
                current.message ?: stringResource(Res.string.database_error_unknown),
            ),
        )
        is HomePageState.Loaded -> {
            val settings = settingsStore.current
            val sortedHabits = current.habits.sortedBy(settings.habitOrderBy, settings.sortDirection)
            val marks = remember(current.dayStates) {
                current.dayStates.associate { CellKey(habitId = it.habitId, date = it.date) to it.mark }
            }

            // Reads the latest state on completion: other changes may be stored meanwhile.
            fun saveMark(updated: DayState) {
                coroutineScope.launch {
                    try {
                        services.dayStates.setMark(updated)
                        val latest = state as? HomePageState.Loaded ?: return@launch
                        state = latest.copy(
                            // Reloaded: `updatedAt` of the habit has changed.
                            habits = services.habits.getAll(),
                            dayStates = latest.dayStates.filterNot {
                                it.habitId == updated.habitId && it.date == updated.date
                            } + updated,
                        )
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        state = HomePageState.Failed(error.message)
                    }
                }
            }

            Scaffold(
                topBar = {
                    SegmentedSelector(
                        labels = homeViewOptions.map { stringResource(it.label) },
                        isSelected = { homeViewOptions[it] == settings.homeView },
                        onClick = { index ->
                            coroutineScope.launch {
                                settingsStore.update(settingsStore.current.copy(homeView = homeViewOptions[index]))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentColorScheme().surfaceVariant)
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                            onClick = { navigator.push(CalendarPage(services, settingsStore)) },
                            modifier = Modifier.align(Alignment.CenterStart),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentColorScheme().onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(Res.string.calendar),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        TextButton(
                            onClick = { navigator.push(SettingsPage(settingsStore, services)) },
                            modifier = Modifier.align(Alignment.CenterEnd),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentColorScheme().onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(Res.string.settings),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
                floatingActionButton = {
                    // Scaffold lifts the FAB 16.dp above the bar; shift it so it protrudes 12.dp.
                    FloatingActionButton(
                        onClick = {
                            editingHabit = null
                            habitError = null
                            showHabitDialog = true
                        },
                        modifier = Modifier.offset(y = 52.dp),
                        containerColor = currentColorScheme().inversePrimary,
                        contentColor = currentColorScheme().secondary,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(Res.string.add_habit),
                        )
                    }
                },
            ) { contentPadding ->
                when (settings.homeView) {
                    HomeView.TODAY -> TodayHabitList(
                        habits = sortedHabits,
                        marks = marks,
                        today = today.toString(),
                        completenessType = settings.completenessType,
                        onMarkChange = ::saveMark,
                        onDayStateClick = { selectedDayState = it },
                        onDayStateLongClick = { deletingDayState = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    )
                    HomeView.WEEKLY -> WeeklyHabitList(
                        habits = sortedHabits,
                        marks = marks,
                        weekDates = currentWeekDates(today, settings.weekStart),
                        today = today.toString(),
                        completenessType = settings.completenessType,
                        onDayStateClick = { selectedDayState = it },
                        onDayStateLongClick = { deletingDayState = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    )
                    HomeView.OVERALL -> CalendarTable(
                        habits = sortedHabits,
                        dates = dates,
                        today = today.toString(),
                        weekStart = settingsStore.current.weekStart,
                        dateFormat = settingsStore.current.dateFormat,
                        completenessType = settingsStore.current.completenessType,
                        dayState = current.dayStates,
                        onDayStateClick = { selectedDayState = it },
                        onDayStateLongClick = { deletingDayState = it },
                        onDateClick = { date ->
                            coroutineScope.launch {
                                try {
                                    // Days before the habit creation are not part of it.
                                    val skipped = services.dayStates.skipDay(
                                        date,
                                        current.habits
                                            .filterNot { it.isArchived || it.isBeforeCreation(date, null) }
                                            .map { it.id },
                                    )
                                    if (skipped.isNotEmpty()) {
                                        state = current.copy(
                                            // Reloaded: `updatedAt` of the habits has changed.
                                            habits = services.habits.getAll(),
                                            dayStates = current.dayStates + skipped,
                                        )
                                    }
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    state = HomePageState.Failed(error.message)
                                }
                            }
                        },
                        onProgressClick = {
                            navigator.push(
                                MetricsPage(current.habits, current.dayStates, today, settingsStore.current.weekStart),
                            )
                        },
                        onHabitClick = {
                            editingHabit = it
                            habitError = null
                            showHabitDialog = true
                        },
                        onHabitLongClick = { habit ->
                            navigator.push(DetailedPage(habit, services, today, settingsStore.current.weekStart))
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    )
                }
            }

            if (showHabitDialog) {
                val habitToEdit = editingHabit
                HabitEditorDialog(
                    habit = habitToEdit,
                    isSaving = isHabitSaving,
                    error = habitError,
                    weekStart = settingsStore.current.weekStart,
                    onDismiss = {
                        showHabitDialog = false
                        editingHabit = null
                    },
                    onSave = { title, icon, color, remarks, activeDays, isArchived, targetValue, targetType ->
                        isHabitSaving = true
                        habitError = null
                        coroutineScope.launch {
                            try {
                                val habits = services.habits.save(
                                    id = habitToEdit?.id,
                                    draft = HabitDraft(
                                        title, icon, color, remarks, activeDays, isArchived,
                                        targetValue, targetType,
                                    ),
                                    weekStart = settingsStore.current.weekStart,
                                )
                                val dayStates = services.dayStates.getRange(dates.first(), lastLoadedDate)
                                state = current.copy(habits = habits, dayStates = dayStates)
                                showHabitDialog = false
                                editingHabit = null
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                habitError = error.message ?: unknownDatabaseError
                            } finally {
                                isHabitSaving = false
                            }
                        }
                    },
                    onDelete = habitToEdit?.let { habit ->
                        {
                            isHabitSaving = true
                            habitError = null
                            coroutineScope.launch {
                                try {
                                    services.habits.delete(habit.id)
                                    state = current.copy(
                                        habits = current.habits.filterNot { it.id == habit.id },
                                        dayStates = current.dayStates.filterNot {
                                            it.habitId == habit.id
                                        },
                                    )
                                    showHabitDialog = false
                                    editingHabit = null
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    habitError = error.message ?: unknownDatabaseError
                                } finally {
                                    isHabitSaving = false
                                }
                            }
                        }
                    },
                )
            }

            selectedDayState?.let { selected ->
                current.habits.firstOrNull { it.id == selected.habitId }?.let { habit ->
                    DayStateDialog(
                        dayState = selected,
                        habit = habit,
                        dateFormat = settingsStore.current.dateFormat,
                        completenessType = settingsStore.current.completenessType,
                        saveError = dayStateSaveError,
                        isSaving = isDayStateSaving,
                        onStatusChange = { mark ->
                            val updated = selected.copy(mark = mark)
                            selectedDayState = updated
                            dayStateSaveError = null
                            isDayStateSaving = true
                            coroutineScope.launch {
                                try {
                                    services.dayStates.setMark(updated)
                                    val updatedDayStates = current.dayStates
                                        .filterNot {
                                            it.habitId == updated.habitId &&
                                                it.date == updated.date
                                        } + updated
                                    state = current.copy(
                                        // Reloaded: `updatedAt` of the habit has changed.
                                        habits = services.habits.getAll(),
                                        dayStates = updatedDayStates,
                                    )
                                    selectedDayState = null
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    dayStateSaveError =
                                        error.message ?: unknownDatabaseError
                                } finally {
                                    isDayStateSaving = false
                                }
                            }
                        },
                        onDismiss = {
                            selectedDayState = null
                            dayStateSaveError = null
                        },
                    )
                }
            }

            deletingDayState?.let { deleting ->
                AlertDialog(
                    onDismissRequest = { deletingDayState = null },
                    title = { Text(stringResource(Res.string.delete_day_state_confirmation)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deletingDayState = null
                                coroutineScope.launch {
                                    try {
                                        services.dayStates.deleteMark(deleting.habitId, deleting.date)
                                        state = current.copy(
                                            // Reloaded: `updatedAt` of the habit has changed.
                                            habits = services.habits.getAll(),
                                            dayStates = current.dayStates.filterNot {
                                                it.habitId == deleting.habitId &&
                                                    it.date == deleting.date
                                            },
                                        )
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        state = HomePageState.Failed(error.message)
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentColorScheme().error,
                            ),
                        ) {
                            Text(stringResource(Res.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deletingDayState = null }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    },
                )
            }
        }
    }
}

private sealed interface HomePageState {
    data object Loading : HomePageState
    data class Loaded(
        val habits: List<Habit>,
        val dayStates: List<DayState>,
    ) : HomePageState
    data class Failed(val message: String?) : HomePageState
}
