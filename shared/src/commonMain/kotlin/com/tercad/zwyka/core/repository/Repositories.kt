// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.repository

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Goal
import com.tercad.zwyka.core.model.GoalGroup
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

    /** Sets `updatedAt` to now, for changes of data belonging to the habit (marks, scheduled times). */
    suspend fun touch(id: Long)

    /** Deletes the habit together with its day states, scheduled times and goals. */
    suspend fun delete(id: Long)
}

interface DayStateRepository {
    /** Day states within [from]..[to] (ISO dates, inclusive). */
    suspend fun getRange(from: String, to: String): List<DayState>

    /** Every day state of the habit, ordered by date. */
    suspend fun getForHabit(habitId: Long): List<DayState>

    suspend fun upsert(state: DayState)

    suspend fun delete(habitId: Long, date: String)
}

interface GoalRepository {
    /** Groups of the habit, ordered by position. */
    suspend fun getGroups(habitId: Long): List<GoalGroup>

    /** Goals of every group of the habit. */
    suspend fun getGoals(habitId: Long): List<Goal>

    /** @return id of the new group. */
    suspend fun insertGroup(habitId: Long, title: String, position: Int): Long

    /** Sets the position of each group to its index in [groupIds]. */
    suspend fun updatePositions(groupIds: List<Long>)

    suspend fun insert(groupId: Long, title: String, description: String?)

    suspend fun updateFinished(id: Long, isFinished: Boolean)
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

interface BackupRepository {
    /** All stored data as SQL: one `INSERT IGNORE` statement per line. */
    suspend fun export(): String

    /**
     * Adds the rows of [sql] (as made by [export]); rows with existing keys are skipped.
     * @return number of added rows.
     */
    suspend fun import(sql: String): Int
}
