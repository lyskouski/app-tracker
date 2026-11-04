// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.importing

import com.tercad.zwyka.core.importing.HabitExportFormat
import com.tercad.zwyka.core.importing.detectExportFormat
import com.tercad.zwyka.core.importing.parseHabitExport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitifyImportTest {
    @Test
    fun readsCsvExport() {
        val bytes = sample("habitify-export.csv")
        assertEquals(HabitExportFormat.HABITIFY, detectExportFormat(bytes))

        val habits = parseHabitExport(bytes)

        assertEquals(listOf("Read", "Walk", "No sugar; please"), habits.map { it.draft.title })

        val read = habits.named("Read")
        assertNull(read.draft.targetValue)
        assertFalse(read.draft.isNegative)
        assertEquals(mapOf("2026-05-01" to "X", "2026-05-02" to "—"), read.marks)

        // The smallest completed log is the target.
        val walk = habits.named("Walk")
        assertEquals(5000.0, walk.draft.targetValue)
        assertEquals("steps", walk.draft.targetType)
        assertEquals(mapOf("2026-05-01" to "5000", "2026-05-02" to "3200", "2026-05-03" to "6000"), walk.marks)

        // A habit to avoid: a failure is a relapse.
        val sugar = habits.named("No sugar; please")
        assertTrue(sugar.draft.isNegative)
        assertEquals(mapOf("2026-05-02" to "X"), sugar.marks)
    }

    @Test
    fun readsCommaDelimitedExport() {
        val csv = "Habit,Date,Total log,Unit,Status,Habit ID\nYoga,\"May 3, 2026\",1,times,Completed,id-1\n"

        val habits = parseHabitExport(csv.encodeToByteArray())

        assertEquals(mapOf("2026-05-03" to "X"), habits.named("Yoga").marks)
    }
}
