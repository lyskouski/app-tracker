package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
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
import org.jetbrains.compose.resources.stringResource
import cafe.adriel.voyager.core.screen.Screen
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.add_habit_failed
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.habit_name
import com.tercad.zwyka.resources.habit_database_unavailable
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.habits_load_failed

class HomePage(
    private val database: Database?,
    private val initializeDatabase: suspend () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        HomePageContent(database, initializeDatabase)
    }
}

@Composable
private fun HomePageContent(
    database: Database?,
    initializeDatabase: suspend () -> Unit,
) {
    if (database == null) {
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

    var state by remember(database) {
        mutableStateOf<HomePageState>(HomePageState.Loading)
    }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var newHabitTitle by remember { mutableStateOf("") }
    var addHabitError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val unknownDatabaseError = stringResource(Res.string.database_error_unknown)

    LaunchedEffect(database) {
        state = HomePageState.Loading
        try {
            initializeDatabase()
            val habits = database.databaseQueries.selectAllHabits()
                .awaitAsList()
                .map { Habit(it.id, it.title) }
            val dayStates = database.databaseQueries.selectDayStatesForDateRange(
                dates.first(),
                dates.last(),
            )
                .awaitAsList()
                .map { DayState(it.habitId, it.date, it.mark) }
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
                    BottomAppBar(
                        actions = {},
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    newHabitTitle = ""
                                    addHabitError = null
                                    showAddHabitDialog = true
                                },
                            ) {
                                Text("+")
                            }
                        },
                    )
                },
            ) { contentPadding ->
                CalendarTable(
                    habits = current.habits,
                    dates = dates,
                    dayState = current.dayStates,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                )
            }

            if (showAddHabitDialog) {
                AlertDialog(
                    onDismissRequest = { showAddHabitDialog = false },
                    title = { Text(stringResource(Res.string.add_habit)) },
                    text = {
                        androidx.compose.foundation.layout.Column {
                            OutlinedTextField(
                                value = newHabitTitle,
                                onValueChange = { newHabitTitle = it },
                                label = { Text(stringResource(Res.string.habit_name)) },
                                singleLine = true,
                                isError = addHabitError != null,
                            )
                            addHabitError?.let { error ->
                                Text(stringResource(Res.string.add_habit_failed, error))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = newHabitTitle.isNotBlank(),
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val habits = addHabit(
                                            database = database,
                                            title = newHabitTitle.trim(),
                                        )
                                        state = current.copy(habits = habits)
                                        showAddHabitDialog = false
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        addHabitError = error.message ?: unknownDatabaseError
                                    }
                                }
                            },
                        ) {
                            Text(stringResource(Res.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddHabitDialog = false }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    },
                )
            }
        }
    }
}

private suspend fun addHabit(
    database: Database,
    title: String,
): List<Habit> {
    database.databaseQueries.insertHabit(title)
    return database.databaseQueries.selectAllHabits()
        .awaitAsList()
        .map { Habit(it.id, it.title) }
}

private sealed interface HomePageState {
    data object Loading : HomePageState
    data class Loaded(
        val habits: List<Habit>,
        val dayStates: List<DayState>,
    ) : HomePageState
    data class Failed(val message: String?) : HomePageState
}
