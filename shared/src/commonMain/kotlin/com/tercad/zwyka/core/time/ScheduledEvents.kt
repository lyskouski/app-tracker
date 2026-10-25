// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.MINUTES_PER_DAY
import com.tercad.zwyka.core.model.isActiveOn

/** One occurrence of [habitTime]: [start] / [end] are minutes since the midnight of the first day. */
data class ScheduledEvent(
    val habitTime: HabitTime,
    val start: Int,
    val end: Int,
)

/** Occurrences of [habitTimes] on [days] days from [from], ordered by start. */
fun scheduledEvents(habitTimes: List<HabitTime>, from: LocalDate, days: Int = 2): List<ScheduledEvent> =
    (0 until days).flatMap { day ->
        val dayOfWeek = from.plus(day, DateTimeUnit.DAY).dayOfWeek
        habitTimes
            .filter { it.isActiveOn(dayOfWeek) && it.timeEnd > it.timeStart }
            .map { ScheduledEvent(it, day * MINUTES_PER_DAY + it.timeStart, day * MINUTES_PER_DAY + it.timeEnd) }
    }.sortedWith(compareBy({ it.start }, { it.end }))

/** Duration of [minutes] as "H:MM". */
fun formatDuration(minutes: Int): String = "${minutes / 60}:${(minutes % 60).toString().padStart(2, '0')}"

/** The event in progress at [now] (minutes); the one ending first when they overlap. */
fun List<ScheduledEvent>.currentAt(now: Int): ScheduledEvent? =
    filter { now >= it.start && now < it.end }.minByOrNull { it.end }

/** Events starting after [now] (minutes). */
fun List<ScheduledEvent>.upcomingAfter(now: Int): List<ScheduledEvent> =
    filter { it.start > now }
