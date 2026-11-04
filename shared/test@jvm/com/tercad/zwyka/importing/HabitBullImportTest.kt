// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.importing

import com.tercad.zwyka.core.importing.HabitExportFormat
import com.tercad.zwyka.core.importing.detectExportFormat
import com.tercad.zwyka.core.importing.parseHabitExport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HabitBullImportTest {
    @Test
    fun readsCsvExport() {
        val bytes = sample("habitbull-export.csv")
        assertEquals(HabitExportFormat.HABITBULL, detectExportFormat(bytes))

        val habits = parseHabitExport(bytes)

        assertEquals(listOf("Meditate", "Pushups"), habits.map { it.draft.title })

        // ISO and US dates; 0 is not done.
        val meditate = habits.named("Meditate")
        assertEquals("calm, focus", meditate.draft.remarks)
        assertNull(meditate.draft.targetValue)
        assertEquals(mapOf("2016-03-18" to "X", "2016-03-20" to "X"), meditate.marks)

        // Counts make a numeric habit with the median as the target; negative values are dropped.
        val pushups = habits.named("Pushups")
        assertEquals(40.0, pushups.draft.targetValue)
        assertEquals(mapOf("2021-09-01" to "30", "2021-09-02" to "50", "2021-09-03" to "40"), pushups.marks)
    }
}
