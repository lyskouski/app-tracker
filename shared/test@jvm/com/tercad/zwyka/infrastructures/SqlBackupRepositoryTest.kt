// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.HabitTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SqlBackupRepositoryTest {
    private fun database() = Database(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
    )

    @Test
    fun exportsEveryTable() = runTest {
        val database = database()
        SqlHabitTimeRepository(database).insert(HabitTime(0, 1, null, 60, 90, "it's\nnote", 3, true))
        SqlIntentRepository(database).insert("Walk", 1)
        SqlSettingsRepository(database).put("language", "de")
        SqlGoalRepository(database).run { insert(insertGroup(1, "Morning", 0), "Run", null) }

        val lines = SqlBackupRepository(database).export().lines().filter { it.isNotEmpty() }

        assertTrue(lines.all { it.startsWith("INSERT IGNORE INTO ") && it.endsWith(");") })
        assertEquals(
            listOf("Habit", "Intent", "HabitTime", "DayState", "GoalGroup", "Goal", "Setting"),
            lines.map { it.removePrefix("INSERT IGNORE INTO ").substringBefore(' ') }.distinct(),
        )
        assertTrue("INSERT IGNORE INTO HabitTime (id, habitId, intentId, timeStart, timeEnd, note, activeDays, notify) " +
            // The schema seeds 4 time ranges.
            "VALUES (5, 1, NULL, 60, 90, 'it\\'s\\nnote', 3, 1);" in lines)
    }

    @Test
    fun importAddsMissingRowsOnly() = runTest {
        val source = database()
        val habits = SqlHabitRepository(source)
        val id = habits.insert(HabitDraft("Read", "MenuBook", "#26A69A", "20 pages", 0b11, targetValue = 20.5, targetType = "pages"))
        SqlDayStateRepository(source).upsert(DayState(id, "2026-10-18", "12"))
        SqlSettingsRepository(source).put("language", "de")
        SqlGoalRepository(source).run { insert(insertGroup(id, "Books", 0), "Finish a novel", "300 pages") }
        val sql = SqlBackupRepository(source).export()

        val target = database()
        SqlSettingsRepository(target).put("language", "en")
        val targetBackup = SqlBackupRepository(target)

        // Seeded habits and day states, and the setting, already exist.
        assertEquals(4, targetBackup.import(sql))
        assertEquals(sql, targetBackup.export().replace("'en'", "'de'"))
        assertEquals(0, targetBackup.import(sql))
    }

    @Test
    fun brokenFileAddsNothing() = runTest {
        val database = database()
        val backup = SqlBackupRepository(database)
        val before = backup.export()

        assertFailsWith<IllegalArgumentException> {
            backup.import("INSERT IGNORE INTO Setting (key, value) VALUES ('a', 'b');\nINSERT IGNORE INTO Unknown (id) VALUES (1);")
        }
        assertEquals(before, backup.export())
    }
}
