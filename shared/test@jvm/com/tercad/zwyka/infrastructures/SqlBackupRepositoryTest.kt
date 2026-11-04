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
    fun restoreReplacesStoredData() = runTest {
        val source = database()
        val habits = SqlHabitRepository(source)
        val id = habits.insert(HabitDraft("Read", "MenuBook", "#26A69A", "20 pages", 0b11, targetValue = 20.5, targetType = "pages"))
        SqlDayStateRepository(source).upsert(DayState(id, "2026-10-18", "12"))
        SqlSettingsRepository(source).put("language", "de")
        SqlGoalRepository(source).run { insert(insertGroup(id, "Books", 0), "Finish a novel", "300 pages") }
        val sql = SqlBackupRepository(source).export()

        val target = database()
        SqlHabitRepository(target).insert(HabitDraft("Local only", null, null, null, 0b1))
        SqlSettingsRepository(target).put("language", "en")
        SqlSettingsRepository(target).put("themeMode", "dark")
        val targetBackup = SqlBackupRepository(target)
        val rows = sql.lines().count { it.isNotEmpty() }

        assertEquals(rows, targetBackup.restore(sql))
        assertEquals(sql, targetBackup.export())
        // Restoring again gives the same data.
        assertEquals(rows, targetBackup.restore(sql))
        assertEquals(sql, targetBackup.export())
    }

    @Test
    fun brokenFileKeepsStoredData() = runTest {
        val database = database()
        val backup = SqlBackupRepository(database)
        val before = backup.export()

        assertFailsWith<IllegalArgumentException> {
            backup.restore("INSERT IGNORE INTO Setting (key, value) VALUES ('a', 'b');\nINSERT IGNORE INTO Unknown (id) VALUES (1);")
        }
        assertEquals(before, backup.export())
    }

    @Test
    fun emptyFileKeepsStoredData() = runTest {
        val database = database()
        val backup = SqlBackupRepository(database)
        val before = backup.export()

        assertFailsWith<IllegalArgumentException> { backup.restore("-- nothing\n\n") }
        assertEquals(before, backup.export())
    }
}
