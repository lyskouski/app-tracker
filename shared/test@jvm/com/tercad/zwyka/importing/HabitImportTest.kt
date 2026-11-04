// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.importing

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.core.importing.UnsupportedExportException
import com.tercad.zwyka.core.importing.detectExportFormat
import com.tercad.zwyka.core.importing.parseHabitExport
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.SqlBackupRepository
import com.tercad.zwyka.infrastructures.SqlDayStateRepository
import com.tercad.zwyka.infrastructures.SqlHabitRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HabitImportTest {
    private fun database() = Database(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
    )

    @Test
    fun rejectsOtherFiles() {
        listOf(
            "INSERT IGNORE INTO Setting (key, value) VALUES ('a', 'b');",
            """{"name": "not a habit export"}""",
            "Name,Date\nRead,2026-01-01\n",
            "",
        ).forEach { text ->
            assertNull(detectExportFormat(text.encodeToByteArray()))
            assertFailsWith<UnsupportedExportException> { parseHabitExport(text.encodeToByteArray()) }
        }
    }

    @Test
    fun addsHabitsAndMergesTheSameTitle() = runTest {
        val database = database()
        val habits = SqlHabitRepository(database)
        val meditateId = habits.insert(HabitDraft("meditate", null, null, null, 0x7F))
        SqlDayStateRepository(database).upsert(DayState(meditateId, "2025-01-02", "/"))
        val before = habits.getAll().size
        val backup = SqlBackupRepository(database)
        val imported = parseHabitExport(sample("loop-backup.db"))

        val summary = backup.add(imported)

        // "Meditate" goes to the existing habit, which keeps its own mark of 2025-01-02.
        assertEquals(2, summary.habits)
        assertEquals(399 - 1 + 3 + 1, summary.marks)
        assertEquals(before + 2, habits.getAll().size)
        val states = SqlDayStateRepository(database)
        assertEquals("/", states.getForHabit(meditateId).first { it.date == "2025-01-02" }.mark)
        val run = habits.getAll().first { it.title == "Run" }
        assertEquals(5.0, run.targetValue)
        // The creation is the first marked day.
        assertEquals("2026-01-01", states.getForHabit(run.id).minOf { it.date })

        // A second import of the same file adds nothing.
        assertEquals(0 to 0, backup.add(imported).let { it.habits to it.marks })
    }
}
