// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.components.habitColor
import com.tercad.zwyka.components.habitColorOptions
import com.tercad.zwyka.components.habitIcon
import com.tercad.zwyka.components.habitIconOption
import com.tercad.zwyka.components.habitIconOptions
import com.tercad.zwyka.components.normalizeHabitColor
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.add_habit_failed
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.habit_name
import com.tercad.zwyka.resources.habit_database_unavailable
import com.tercad.zwyka.resources.save
import com.tercad.zwyka.resources.habits_loading
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.habit_icon
import com.tercad.zwyka.resources.habit_color
import com.tercad.zwyka.resources.color_value
import com.tercad.zwyka.resources.invalid_color
import com.tercad.zwyka.resources.default_color
import com.tercad.zwyka.resources.no_icon
import com.tercad.zwyka.resources.select_color
import com.tercad.zwyka.resources.select_icon
import com.tercad.zwyka.resources.search_icons

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
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var newHabitTitle by remember { mutableStateOf("") }
    var newHabitIcon by remember { mutableStateOf<String?>(null) }
    var newHabitColor by remember { mutableStateOf<String?>(null) }
    var iconSearchQuery by remember { mutableStateOf("") }
    var colorInput by remember { mutableStateOf("") }
    var addHabitError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val unknownDatabaseError = stringResource(Res.string.database_error_unknown)

    LaunchedEffect(database) {
        state = HomePageState.Loading
        try {
            initializeDatabase()
            val habits = database.databaseQueries.selectAllHabits()
                .awaitAsList()
                .map { Habit(it.id, it.title, it.icon, it.color) }
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
                                    newHabitIcon = null
                                    newHabitColor = null
                                    iconSearchQuery = ""
                                    showIconPicker = false
                                    showColorPicker = false
                                    addHabitError = null
                                    showAddHabitDialog = true
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(Res.string.add_habit),
                                )
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
                val noIconDescription = stringResource(Res.string.no_icon)
                AlertDialog(
                    onDismissRequest = {
                        showAddHabitDialog = false
                        showIconPicker = false
                        showColorPicker = false
                    },
                    title = {
                        Text(
                            stringResource(
                                when {
                                    showIconPicker -> Res.string.select_icon
                                    showColorPicker -> Res.string.select_color
                                    else -> Res.string.add_habit
                                },
                            ),
                        )
                    },
                    text = {
                        if (showIconPicker) {
                            Column {
                                OutlinedTextField(
                                    value = iconSearchQuery,
                                    onValueChange = { iconSearchQuery = it },
                                    label = { Text(stringResource(Res.string.search_icons)) },
                                    singleLine = true,
                                )
                                TextButton(
                                    onClick = {
                                        newHabitIcon = null
                                        showIconPicker = false
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = noIconDescription
                                    },
                                ) {
                                    Text("?  $noIconDescription")
                                }
                                val matchingIcons = remember(iconSearchQuery) {
                                    habitIconOptions.filter { option ->
                                        option.title.contains(iconSearchQuery, ignoreCase = true)
                                    }
                                }
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(48.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(360.dp),
                                ) {
                                    items(
                                        items = matchingIcons,
                                        key = { it.id },
                                    ) { option ->
                                        IconButton(
                                            onClick = {
                                                newHabitIcon = option.id
                                                showIconPicker = false
                                            },
                                            modifier = Modifier.semantics {
                                                contentDescription = option.title
                                            },
                                        ) {
                                            Icon(
                                                imageVector = option.imageVector(),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (showColorPicker) {
                            val normalizedColor = normalizeHabitColor(colorInput)
                            Column {
                                OutlinedTextField(
                                    value = colorInput,
                                    onValueChange = { colorInput = it },
                                    label = { Text(stringResource(Res.string.color_value)) },
                                    singleLine = true,
                                    isError = colorInput.isNotBlank() && normalizedColor == null,
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(28.dp)
                                                .background(
                                                    habitColor(normalizedColor)
                                                        ?: currentColorScheme().surfaceVariant,
                                                    CircleShape,
                                                ),
                                        )
                                    },
                                )
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(6),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(192.dp),
                                ) {
                                    items(
                                        items = habitColorOptions,
                                        key = { it },
                                    ) { color ->
                                        IconButton(
                                            onClick = {
                                                newHabitColor = color
                                                colorInput = color
                                                showColorPicker = false
                                            },
                                            modifier = Modifier.semantics {
                                                contentDescription = color
                                            },
                                        ) {
                                            habitColor(color)?.let { colorValue ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(
                                                            colorValue,
                                                            CircleShape,
                                                        ),
                                                )
                                            }
                                        }
                                    }
                                }
                                if (colorInput.isNotBlank() && normalizedColor == null) {
                                    Text(stringResource(Res.string.invalid_color))
                                }
                                TextButton(onClick = {
                                    newHabitColor = null
                                    showColorPicker = false
                                }) {
                                    Text(stringResource(Res.string.default_color))
                                }
                            }
                        } else {
                            Column {
                                OutlinedTextField(
                                    value = newHabitTitle,
                                    onValueChange = { newHabitTitle = it },
                                    label = { Text(stringResource(Res.string.habit_name)) },
                                    singleLine = true,
                                    isError = addHabitError != null,
                                )
                                Text(stringResource(Res.string.habit_icon))
                                OutlinedButton(onClick = {
                                    iconSearchQuery = ""
                                    showIconPicker = true
                                }) {
                                    val selectedIcon = habitIcon(newHabitIcon)
                                    if (selectedIcon != null) {
                                        Icon(
                                            imageVector = selectedIcon,
                                            contentDescription = null,
                                        )
                                    } else {
                                        Text("?")
                                    }
                                    Text(
                                        habitIconOption(newHabitIcon)?.title
                                            ?: stringResource(Res.string.no_icon),
                                    )
                                }
                                Text(stringResource(Res.string.habit_color))
                                OutlinedButton(onClick = {
                                    colorInput = newHabitColor.orEmpty()
                                    showColorPicker = true
                                }) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(20.dp)
                                            .background(
                                                habitColor(newHabitColor)
                                                    ?: currentColorScheme().surfaceVariant,
                                                CircleShape,
                                            ),
                                    )
                                    Text(newHabitColor ?: stringResource(Res.string.default_color))
                                }
                                addHabitError?.let { error ->
                                    Text(stringResource(Res.string.add_habit_failed, error))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = showIconPicker ||
                                (showColorPicker && normalizeHabitColor(colorInput) != null) ||
                                (!showIconPicker && !showColorPicker && newHabitTitle.isNotBlank()),
                            onClick = {
                                if (showIconPicker) {
                                    showIconPicker = false
                                    return@TextButton
                                }
                                if (showColorPicker) {
                                    newHabitColor = normalizeHabitColor(colorInput)
                                    showColorPicker = false
                                    return@TextButton
                                }
                                coroutineScope.launch {
                                    try {
                                        val habits = addHabit(
                                            database = database,
                                            title = newHabitTitle.trim(),
                                            icon = newHabitIcon,
                                            color = newHabitColor,
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
                            Text(
                                stringResource(
                                    if (showIconPicker) Res.string.back else Res.string.save,
                                ),
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAddHabitDialog = false
                                showIconPicker = false
                                showColorPicker = false
                            },
                        ) {
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
    icon: String?,
    color: String?,
): List<Habit> {
    database.databaseQueries.insertHabit(title, icon, color)
    return database.databaseQueries.selectAllHabits()
        .awaitAsList()
        .map { Habit(it.id, it.title, it.icon, it.color) }
}

private sealed interface HomePageState {
    data object Loading : HomePageState
    data class Loaded(
        val habits: List<Habit>,
        val dayStates: List<DayState>,
    ) : HomePageState
    data class Failed(val message: String?) : HomePageState
}
