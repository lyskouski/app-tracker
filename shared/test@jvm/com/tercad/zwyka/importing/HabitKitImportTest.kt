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
import kotlin.time.Instant

class HabitKitImportTest {
    @Test
    fun readsJsonExport() {
        val bytes = sample("habitkit-export.json")
        assertEquals(HabitExportFormat.HABITKIT, detectExportFormat(bytes))

        val habits = parseHabitExport(bytes)

        // Ordered by orderIndex.
        assertEquals(listOf("Water", "Workout", "Smoking", "Pushups"), habits.map { it.draft.title })

        val workout = habits.named("Workout")
        assertEquals("FitnessCenter", workout.draft.icon)
        assertEquals("#16A34A", workout.draft.color)
        assertEquals("Gym or home é", workout.draft.remarks)
        assertNull(workout.draft.targetValue)
        assertEquals(Instant.parse("2025-09-22T03:21:07.970697Z").toEpochMilliseconds(), workout.createdAt)
        // The local day is the UTC date plus the offset; an undone tap (amount 0) is not a mark.
        assertEquals(mapOf("2026-10-26" to "X", "2026-10-28" to "X"), workout.marks)

        val water = habits.named("Water")
        assertNull(water.draft.icon)
        assertEquals("#0284C7", water.draft.color)
        assertEquals(8.0, water.draft.targetValue)
        assertEquals("glasses", water.draft.targetType)
        assertEquals(mapOf("2026-10-26" to "6", "2026-10-27" to "8"), water.marks)

        val smoking = habits.named("Smoking")
        assertTrue(smoking.draft.isNegative)
        assertEquals("SmokingRooms", smoking.draft.icon)
        assertEquals(mapOf("2026-10-20" to "X"), smoking.marks)

        // Several taps a day: the goal of the current interval is the target.
        val pushups = habits.named("Pushups")
        assertTrue(pushups.draft.isArchived)
        assertFalse(pushups.draft.isNegative)
        assertEquals("#123ABC", pushups.draft.color)
        assertEquals(3.0, pushups.draft.targetValue)
        assertEquals(mapOf("2026-10-26" to "2"), pushups.marks)
    }
}
