// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.repository

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.Intent

/*
 * Storage abstractions: the business logic depends on them, not on the database.
 */

interface HabitRepository {
    suspend fun getAll(): List<Habit>

    /** @return id of the new habit. */
    suspend fun insert(draft: HabitDraft): Long

    suspend fun update(id: Long, draft: HabitDraft)

    suspend fun updateActiveDays(id: Long, activeDays: Int)

    /** Deletes the habit together with its day states and scheduled times. */
    suspend fun delete(id: Long)
}

interface DayStateRepository {
    /** Day states within [from]..[to] (ISO dates, inclusive). */
    suspend fun getRange(from: String, to: String): List<DayState>

    suspend fun upsert(state: DayState)

    suspend fun delete(habitId: Long, date: String)
}

interface HabitTimeRepository {
    suspend fun getAll(): List<HabitTime>

    /** Inserts [habitTime] ignoring its id. */
    suspend fun insert(habitTime: HabitTime)

    suspend fun update(habitTime: HabitTime)

    suspend fun delete(id: Long)
}

interface IntentRepository {
    suspend fun getAll(): List<Intent>

    /** @return id of the new intent. */
    suspend fun insert(title: String, activeDays: Int): Long

    suspend fun updateTitle(id: Long, title: String)

    suspend fun updateActiveDays(id: Long, activeDays: Int)

    /** Deletes intents without scheduled times. */
    suspend fun deleteUnused()
}

interface SettingsRepository {
    suspend fun getAll(): Map<String, String>

    suspend fun put(key: String, value: String)

    suspend fun remove(key: String)
}
