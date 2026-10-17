// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.components.DayStateDialog
import com.tercad.zwyka.components.HabitEditorDialog
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
import com.tercad.zwyka.resources.database_error_unknown
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
    val coroutineScope = rememberCoroutineScope()
    val unknownDatabaseError = stringResource(Res.string.database_error_unknown)

    LaunchedEffect(services) {
        state = HomePageState.Loading
        try {
            initializeDatabase()
            val habits = services.habits.getAll()
            val dayStates = services.dayStates.getRange(dates.first(), dates.last())
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
            Scaffold(
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
                            onClick = { navigator.push(SettingsPage(settingsStore)) },
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
                CalendarTable(
                    habits = current.habits.sortedBy(
                        settingsStore.current.habitOrderBy,
                        settingsStore.current.sortDirection,
                    ),
                    dates = dates,
                    today = today.toString(),
                    weekStart = settingsStore.current.weekStart,
                    dateFormat = settingsStore.current.dateFormat,
                    dayState = current.dayStates,
                    onDayStateClick = { selectedDayState = it },
                    onProgressClick = {
                        navigator.push(MetricsPage(current.habits, current.dayStates, today))
                    },
                    onHabitClick = {
                        editingHabit = it
                        habitError = null
                        showHabitDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                )
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
                    onSave = { title, icon, color, remarks, activeDays ->
                        isHabitSaving = true
                        habitError = null
                        coroutineScope.launch {
                            try {
                                val habits = services.habits.save(
                                    id = habitToEdit?.id,
                                    draft = HabitDraft(title, icon, color, remarks, activeDays),
                                    weekStart = settingsStore.current.weekStart,
                                )
                                val dayStates = services.dayStates.getRange(dates.first(), dates.last())
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
                                    state = current.copy(dayStates = updatedDayStates)
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
