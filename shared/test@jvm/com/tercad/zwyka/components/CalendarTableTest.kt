// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertTrue

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.defines.ColorSchemeLight
import com.tercad.zwyka.defines.LocalColorScheme

@OptIn(ExperimentalTestApi::class)
class CalendarTableTest {
    @Test
    fun tabGoesThroughCurrentDayOnly() = runComposeUiTest {
        val today = "2026-10-30"
        setContent {
            CompositionLocalProvider(LocalColorScheme provides ColorSchemeLight) {
                CalendarTable(
                    habits = listOf(Habit(id = 1, title = "Run"), Habit(id = 2, title = "Read")),
                    dates = listOf("2026-10-28", "2026-10-29", today, "2026-10-31"),
                    dayState = emptyList(),
                    onDayStateClick = {},
                    onHabitClick = {},
                    today = today,
                )
            }
        }

        val visited = mutableListOf<String>()
        repeat(10) {
            onRoot().performKeyInput { pressKey(Key.Tab) }
            val focused = onAllNodes(isFocused(), useUnmergedTree = true).fetchSemanticsNodes()
            focused.firstOrNull()?.config?.getOrNull(SemanticsProperties.ContentDescription)
                ?.joinToString()?.let(visited::add)
        }

        // Cells are described as "<habit>, <date>, <status>, ...".
        assertTrue(visited.any { it.startsWith("Run, $today") }, visited.toString())
        assertTrue(visited.any { it.startsWith("Read, $today") }, visited.toString())
        assertTrue(visited.none { "2026-10-28" in it || "2026-10-29" in it || "2026-10-31" in it }, visited.toString())
    }

    @Test
    fun focusedCellIsScrolledIntoView() = runComposeUiTest {
        val today = LocalDate.parse("2026-10-30")
        val dates = (39 downTo 0).map { today.minus(it, DateTimeUnit.DAY).toString() }
        val habits = (1..20).map { Habit(id = it.toLong(), title = "H$it") }
        setContent {
            CompositionLocalProvider(LocalColorScheme provides ColorSchemeLight) {
                CalendarTable(
                    habits = habits,
                    dates = dates,
                    dayState = emptyList(),
                    onDayStateClick = {},
                    onHabitClick = {},
                    today = today.toString(),
                    modifier = Modifier.size(300.dp),
                )
            }
        }

        // The table takes the focus first; Home scrolls the current day (the last date) away.
        onRoot().performKeyInput { pressKey(Key.Tab) }
        onRoot().performKeyInput { pressKey(Key.MoveHome) }

        val visited = mutableListOf<String>()
        val minSize = with(density) { 47.dp.toPx() }
        repeat(50) {
            onRoot().performKeyInput { pressKey(Key.Tab) }
            val node = onAllNodes(isFocused(), useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()
                ?: return@repeat
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: return@repeat
            visited += description
            // Not clipped: the whole cell is shown.
            val bounds = node.boundsInRoot
            assertTrue(bounds.width >= minSize && bounds.height >= minSize, "$description: $bounds")
        }

        habits.forEach { habit ->
            assertTrue(visited.any { it.startsWith("${habit.title}, $today") }, visited.toString())
        }
    }
}
