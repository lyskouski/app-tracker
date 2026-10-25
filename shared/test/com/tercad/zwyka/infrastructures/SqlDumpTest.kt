// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SqlDumpTest {
    @Test
    fun writesOneInsertIgnorePerLine() {
        val line = sqlInsert("Habit", listOf("id" to 1L, "title" to "It's\na \\ test", "icon" to null))
        assertEquals("INSERT IGNORE INTO Habit (id, title, icon) VALUES (1, 'It\\'s\\na \\\\ test', NULL);", line)
    }

    @Test
    fun readsWrittenValuesBack() {
        val values = listOf("id" to -5L, "title" to "It's, (quoted)\r\n\ttext \\", "icon" to null)
        val parsed = parseSqlDump(sqlInsert("Habit", values) + "\n")
        assertEquals(listOf(SqlInsert("Habit", values.toMap())), parsed)
    }

    @Test
    fun readsRealNumbersBack() {
        val values = listOf("id" to 1L, "targetValue" to 2.5)
        val parsed = parseSqlDump(sqlInsert("Habit", values) + "\n")
        assertEquals(listOf(SqlInsert("Habit", values.toMap())), parsed)
    }

    @Test
    fun acceptsSqliteNotationAndComments() {
        val parsed = parseSqlDump(
            """
            -- backup
            insert or ignore into Setting(key, value) values ('a', 'it''s');
            """.trimIndent(),
        )
        assertEquals(listOf(SqlInsert("Setting", mapOf("key" to "a", "value" to "it's"))), parsed)
    }

    @Test
    fun rejectsOtherStatements() {
        val error = assertFailsWith<SqlDumpException> {
            parseSqlDump("INSERT IGNORE INTO Setting (key, value) VALUES ('a', 'b');\nDROP TABLE Habit;")
        }
        assertEquals("Line 2: expected INSERT IGNORE statement", error.message)
    }

    @Test
    fun rejectsMismatchedValues() {
        assertFailsWith<SqlDumpException> { parseSqlDump("INSERT IGNORE INTO Setting (key, value) VALUES ('a');") }
        assertFailsWith<SqlDumpException> { parseSqlDump("INSERT IGNORE INTO Setting (key, value) VALUES ('a', b);") }
        assertFailsWith<SqlDumpException> { parseSqlDump("INSERT IGNORE INTO Setting (key, value) VALUES ('a', 'b);") }
    }
}
