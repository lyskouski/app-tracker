// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.importing

import com.tercad.zwyka.core.importing.HabitExportFormat
import com.tercad.zwyka.core.importing.detectExportFormat
import com.tercad.zwyka.core.importing.parseHabitExport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoopImportTest {
    @Test
    fun readsDatabaseBackup() {
        val bytes = sample("loop-backup.db")
        assertEquals(HabitExportFormat.LOOP_DATABASE, detectExportFormat(bytes))

        val habits = parseHabitExport(bytes)

        assertEquals(listOf("Meditate", "Run", "Old habit"), habits.map { it.draft.title })
        val meditate = habits.named("Meditate")
        assertEquals("#00897B", meditate.draft.color)
        assertEquals("Did you meditate?", meditate.draft.remarks)
        assertNull(meditate.draft.targetValue)
        // 400 days on several pages: every 10th is skipped, the 6th is "no".
        assertEquals(399, meditate.marks.size)
        assertEquals(40, meditate.marks.values.count { it == "—" })
        assertEquals("—", meditate.marks["2025-01-01"])
        assertEquals("X", meditate.marks["2025-01-02"])
        assertNull(meditate.marks["2025-01-06"])
        assertEquals("X", meditate.marks["2026-02-04"])

        val run = habits.named("Run")
        assertEquals(5.0, run.draft.targetValue)
        assertEquals("km", run.draft.targetType)
        assertEquals("#D32F2F", run.draft.color)
        // The description is on overflow pages.
        assertEquals("How far?\n" + "Long notes of the run. ".repeat(250).trim(), run.draft.remarks)
        assertEquals(mapOf("2026-01-01" to "5.5", "2026-01-02" to "3", "2026-01-03" to "—"), run.marks)

        val old = habits.named("Old habit")
        assertTrue(old.draft.isArchived)
        assertEquals("#9E9E9E", old.draft.color)
        // An automatic check (covered by the frequency) is not a mark.
        assertEquals(mapOf("2024-06-01" to "X"), old.marks)
    }

    @Test
    fun readsDatabaseBackupOfOldVersion() {
        val habits = parseHabitExport(sample("loop-backup-v12.db"))

        val floss = habits.named("Floss")
        assertEquals("#388E3C", floss.draft.color)
        assertEquals("Did you floss?", floss.draft.remarks)
        assertEquals(mapOf("2016-03-01" to "X", "2016-03-02" to "X"), floss.marks)
    }

    @Test
    fun readsCsvExport() {
        val bytes = sample("loop-export.zip")
        assertEquals(HabitExportFormat.LOOP_CSV, detectExportFormat(bytes))

        val habits = parseHabitExport(bytes)

        assertEquals(listOf("Meditate", "Read, think", "Stretch"), habits.map { it.draft.title })
        val meditate = habits.named("Meditate")
        assertEquals("#00897B", meditate.draft.color)
        assertEquals("—", meditate.marks["2026-03-02"])
        assertNull(meditate.marks["2026-03-01"])
        assertNull(meditate.marks["2026-02-28"])
        // A compressed file of 200 more days.
        assertEquals(1 + 1 + 200, meditate.marks.size)
        assertEquals("X", meditate.marks["2025-08-12"])

        val read = habits.named("Read, think")
        assertEquals(20.0, read.draft.targetValue)
        assertEquals("pages", read.draft.targetType)
        assertEquals("How many pages?\nBooks", read.draft.remarks)
        assertEquals(mapOf("2026-03-03" to "25", "2026-03-01" to "12.5"), read.marks)

        // Without a file of its own: from the combined Checkmarks.csv.
        val stretch = habits.named("Stretch")
        assertTrue(stretch.draft.isArchived)
        assertEquals(mapOf("2026-03-03" to "X"), stretch.marks)
    }
}
