package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen

import com.tercad.zwyka.components.CalendarTable
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit

class HomePage : Screen {
    @Composable
    override fun Content() {
        HomePageContent()
    }
}

@Composable
private fun HomePageContent() {
    val habits = remember {
        listOf(
            Habit(1, "Sample 1"),
            Habit(2, "Sample 2"),
            Habit(3, "Sample 3"),
            Habit(4, "Sample 4"),
        )
    }

    val dates = remember {
        listOf(
            "2026-10-01",
            "2026-10-02",
            "2026-10-03",
            "2026-10-04",
            "2026-10-05",
            "2026-10-06",
            "2026-10-07",
            "2026-10-08",
            "2026-10-09",
            "2026-10-10",
        )
    }

    val dayState = remember {
        listOf(
            DayState(1, "2026-10-01", "X"),
            DayState(1, "2026-10-02", " "),
            DayState(1, "2026-10-03", "/"),
            DayState(2, "2026-10-02", "X"),
            DayState(2, "2026-10-03", "X"),
            DayState(3, "2026-10-03", "X"),
        )
    }

    CalendarTable(
        habits = habits,
        dates = dates,
        dayState = dayState,
        modifier = Modifier.fillMaxSize(),
    )
}
