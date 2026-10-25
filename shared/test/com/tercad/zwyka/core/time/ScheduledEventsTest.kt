// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.time

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

import com.tercad.zwyka.core.model.HabitTime

class ScheduledEventsTest {
    // Sunday: the next day is Monday.
    private val sunday = LocalDate.parse("2026-10-25")

    private val reading = HabitTime(1, habitId = 1, timeStart = 9 * 60, timeEnd = 10 * 60)
    private val walking = HabitTime(2, habitId = 2, timeStart = 9 * 60 + 30, timeEnd = 11 * 60)
    // Monday only.
    private val working = HabitTime(3, habitId = null, intentId = 1, timeStart = 8 * 60, timeEnd = 17 * 60, activeDays = 1)

    @Test
    fun repeatsActiveRangesOnTheNextDay() {
        val events = scheduledEvents(listOf(working, reading), sunday)
        assertEquals(
            listOf(
                ScheduledEvent(reading, 540, 600),
                ScheduledEvent(working, 1440 + 480, 1440 + 1020),
                ScheduledEvent(reading, 1440 + 540, 1440 + 600),
            ),
            events,
        )
    }

    @Test
    fun picksTheEventEndingFirstAndTheUpcomingOnes() {
        val events = scheduledEvents(listOf(reading, walking), sunday, days = 1)
        assertEquals(reading, events.currentAt(9 * 60 + 45)?.habitTime)
        assertEquals(walking, events.currentAt(10 * 60)?.habitTime)
        assertEquals(null, events.currentAt(11 * 60))
        assertEquals(listOf(walking), events.upcomingAfter(9 * 60).map { it.habitTime })
    }
}
