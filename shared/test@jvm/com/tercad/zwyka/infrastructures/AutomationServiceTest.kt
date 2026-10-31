// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.service.AutomationResult
import com.tercad.zwyka.core.service.AutomationService
import com.tercad.zwyka.core.service.DataChanges
import com.tercad.zwyka.core.service.DayStateService
import com.tercad.zwyka.core.service.HabitQuery
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class AutomationServiceTest {
    private val database = Database(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
    )
    private val habitRepository = SqlHabitRepository(database)
    private val dayStateRepository = SqlDayStateRepository(database)

    // Wednesday noon.
    private val clock = object : Clock {
        override fun now(): Instant = LocalDateTime(2099, 1, 7, 12, 0).toInstant(TimeZone.currentSystemDefault())
    }
    private val automation = AutomationService(
        habitRepository,
        DayStateService(dayStateRepository, habitRepository),
        clock,
    )

    // Without the sample habits of a new database.
    private suspend fun clear() = habitRepository.getAll().forEach { habitRepository.delete(it.id) }

    private suspend fun habit(title: String, targetValue: Double? = null, isArchived: Boolean = false) =
        habitRepository.insert(
            HabitDraft(title, null, null, null, ALL_DAYS, isArchived = isArchived, targetValue = targetValue),
        )

    private suspend fun markOf(habitId: Long) =
        dayStateRepository.getRange("2099-01-07", "2099-01-07").firstOrNull { it.habitId == habitId }?.mark

    @Test
    fun findsHabitByIdOrTitle() = runTest {
        clear()
        val reading = habit("Reading")
        val running = habit("Running")
        habit("Swimming", isArchived = true)

        assertEquals(reading, automation.find(HabitQuery(id = reading))?.id)
        assertEquals(reading, automation.find(HabitQuery(title = " reading "))?.id)
        assertEquals(running, automation.find(HabitQuery(title = "runn"))?.id)
        // Ambiguous, archived, or blank.
        assertNull(automation.find(HabitQuery(title = "R")))
        assertNull(automation.find(HabitQuery(title = "Swimming")))
        assertNull(automation.find(HabitQuery(title = " ")))
    }

    @Test
    fun completesSkipsAndAddsValues() = runTest {
        clear()
        val reading = habit("Reading")
        val water = habit("Water", targetValue = 8.0)

        automation.complete(HabitQuery(title = "Reading"))
        assertEquals(DONE_MARK, markOf(reading))
        // A value is ignored without a numeric target.
        automation.complete(HabitQuery(id = reading), value = 3.0)
        assertEquals(DONE_MARK, markOf(reading))
        automation.skip(HabitQuery(id = reading))
        assertEquals(INACTIVE_MARK, markOf(reading))

        automation.add(HabitQuery(id = water), 2.0)
        automation.add(HabitQuery(id = water), 1.5)
        assertEquals("3.5", markOf(water))
        automation.complete(HabitQuery(id = water), value = 5.0)
        assertEquals("5", markOf(water))
        automation.complete(HabitQuery(id = water))
        assertEquals("8", markOf(water))

        assertEquals(
            AutomationResult.NotFound(HabitQuery(title = "Chess")),
            automation.complete(HabitQuery(title = "Chess")),
        )
    }

    @Test
    fun listsHabitsLeftToday() = runTest {
        clear()
        habit("Reading")
        habit("Running")
        val water = habit("Water", targetValue = 8.0)
        habit("Swimming", isArchived = true)

        automation.complete(HabitQuery(title = "Reading"))
        automation.add(HabitQuery(id = water), 2.0)

        assertEquals(listOf("Running", "Water"), automation.leftToday().map { it.title })
    }

    @Test
    fun publishesChanges() = runTest {
        clear()
        habit("Reading")
        val change = async { DataChanges.events.first() }
        yield()

        automation.complete(HabitQuery(title = "Reading"))

        assertEquals(Unit, change.await())
    }
}
