// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.repository.DayStateRepository
import com.tercad.zwyka.core.repository.HabitRepository

class DayStateService(
    private val dayStates: DayStateRepository,
    private val habits: HabitRepository,
) {
    /** Day states within [from]..[to] (ISO dates, inclusive). */
    suspend fun getRange(from: String, to: String): List<DayState> = dayStates.getRange(from, to)

    /** Stores the mark; the habit counts as updated. */
    suspend fun setMark(state: DayState) {
        dayStates.upsert(state)
        habits.touch(state.habitId)
    }

    /** Removes the mark of [habitId] on [date]; the habit counts as updated. */
    suspend fun deleteMark(habitId: Long, date: String) {
        dayStates.delete(habitId, date)
        habits.touch(habitId)
    }

    /**
     * Marks [date] as skipped ("—") for the habits without a mark on it;
     * existing marks are kept.
     *
     * @return the added day states.
     */
    suspend fun skipDay(date: String, habitIds: List<Long>): List<DayState> {
        val marked = dayStates.getRange(date, date).mapTo(HashSet()) { it.habitId }
        return habitIds
            .filterNot { it in marked }
            .map { DayState(habitId = it, date = date, mark = INACTIVE_MARK) }
            .onEach { setMark(it) }
    }
}
