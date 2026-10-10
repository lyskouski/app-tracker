package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.infrastructures.Database

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
            Text("Habit database is unavailable.")
        }
        return
    }

    var state by remember(database) {
        mutableStateOf<HomePageState>(HomePageState.Loading)
    }

    LaunchedEffect(database) {
        state = HomePageState.Loading
        try {
            initializeDatabase()
            val habits = database.databaseQueries.selectAllHabits()
                .awaitAsList()
                .map { Habit(it.id, it.title) }
            val dayStates = database.databaseQueries.selectAllDayStates()
                .awaitAsList()
                .map { DayState(it.habitId, it.date, it.mark) }
            state = HomePageState.Loaded(habits, dayStates)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            state = HomePageState.Failed(error.message ?: "Unknown database error")
        }
    }

    when (val current = state) {
        HomePageState.Loading -> Text("Loading habits...")
        is HomePageState.Failed -> Text("Unable to load habits: ${current.message}")
        is HomePageState.Loaded -> {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            val dates = remember(today) {
                (-66..5).map { dayOffset ->
                    today.plus(dayOffset, DateTimeUnit.DAY).toString()
                }
            }

            CalendarTable(
                habits = current.habits,
                dates = dates,
                dayState = current.dayStates,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private sealed interface HomePageState {
    data object Loading : HomePageState
    data class Loaded(
        val habits: List<Habit>,
        val dayStates: List<DayState>,
    ) : HomePageState
    data class Failed(val message: String) : HomePageState
}
