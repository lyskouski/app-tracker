// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.notifications.Reminder
import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.core.service.HabitService
import com.tercad.zwyka.core.service.ReminderService
import com.tercad.zwyka.defines.WeekStart
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class HabitServiceTest {
    private val database = Database(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
    )
    private val habitRepository = SqlHabitRepository(database)
    private val dayStateRepository = SqlDayStateRepository(database)

    // Wednesday, the habits are created before its week.
    private val clock = object : Clock {
        override fun now(): Instant = LocalDate(2099, 1, 7).atStartOfDayIn(TimeZone.currentSystemDefault())
    }
    private val habits = HabitService(
        habitRepository,
        dayStateRepository,
        ReminderService(
            habitRepository,
            SqlIntentRepository(database),
            SqlHabitTimeRepository(database),
            SqlSettingsRepository(database),
            scheduler = object : ReminderScheduler {
                override fun schedule(reminders: List<Reminder>) = Unit
            },
        ),
        clock,
    )

    private suspend fun marks(habitId: Long) =
        dayStateRepository.getRange("2099-01-05", "2099-01-11")
            .filter { it.habitId == habitId }
            .associate { it.date to it.mark }

    @Test
    fun storesInactiveDaysOfCurrentWeek() = runTest {
        // Tuesday, Thursday, Saturday.
        val habit = habitRepository.insert(HabitDraft("English", null, null, null, activeDays = 42))
        dayStateRepository.upsert(DayState(habit, "2099-01-09", "X"))

        habits.fillInactiveDays(WeekStart.MONDAY)

        assertEquals(
            mapOf(
                "2099-01-05" to INACTIVE_MARK,
                "2099-01-07" to INACTIVE_MARK,
                "2099-01-09" to "X",
                "2099-01-11" to INACTIVE_MARK,
            ),
            marks(habit),
        )
    }

    @Test
    fun skipsArchivedHabits() = runTest {
        val habit = habitRepository.insert(HabitDraft("Archived", null, null, null, activeDays = 0, isArchived = true))

        habits.fillInactiveDays(WeekStart.MONDAY)

        assertEquals(emptyMap(), marks(habit))
    }
}
