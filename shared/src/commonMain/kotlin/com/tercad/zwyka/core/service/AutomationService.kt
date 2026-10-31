// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.model.hasNumericTarget
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.core.progress.TodayStatus
import com.tercad.zwyka.core.progress.todayStatus
import com.tercad.zwyka.core.repository.HabitRepository

/** Habit of an automation request: by [id], otherwise by [title]. */
data class HabitQuery(val id: Long? = null, val title: String? = null)

sealed interface AutomationResult {
    /** [mark] is stored as today's state of [habit]. */
    data class Marked(val habit: Habit, val mark: String) : AutomationResult

    /** No single active habit matches [query]. */
    data class NotFound(val query: HabitQuery) : AutomationResult
}

/*
 * Today's states of the habits changed from outside the app: Siri Shortcuts (iOS),
 * intents (Android). Each change is published to [DataChanges], so the shown screens reload.
 *
 * Archived habits are left out: they have no marks.
 */
class AutomationService(
    private val habits: HabitRepository,
    private val dayStates: DayStateService,
    private val clock: Clock = Clock.System,
) {
    private fun today(): String =
        clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    /** Active habits, by title. */
    suspend fun habits(): List<Habit> = habits.getAll().filterNot { it.isArchived }.sortedBy { it.title.lowercase() }

    /*
     * The habit of [query]: by id, by the title ignoring the case, or by the only title
     * that starts with or contains it (as spoken to Siri); null when not found or ambiguous.
     */
    suspend fun find(query: HabitQuery): Habit? {
        val habits = habits()
        query.id?.let { id -> return habits.firstOrNull { it.id == id } }
        val title = query.title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return habits.firstOrNull { it.title.equals(title, ignoreCase = true) }
            ?: habits.singleOrNull { it.title.startsWith(title, ignoreCase = true) }
            ?: habits.singleOrNull { it.title.contains(title, ignoreCase = true) }
    }

    /*
     * Marks today as done: 100%, or the reached [value] of a numeric target (the target itself without a value).
     * [value] is ignored for habits without a numeric target.
     */
    suspend fun complete(query: HabitQuery, value: Double? = null): AutomationResult =
        mark(query) { habit, _ ->
            if (habit.hasNumericTarget) {
                (value ?: habit.targetValue)?.let(::formatNumericValue) ?: DONE_MARK
            } else {
                DONE_MARK
            }
        }

    /*
     * Adds [value] to today's reached value of a numeric target, e.g. a glass of water;
     * habits without a numeric target are marked as done.
     */
    suspend fun add(query: HabitQuery, value: Double): AutomationResult =
        mark(query) { habit, mark ->
            if (habit.hasNumericTarget) {
                formatNumericValue((mark?.let(::parseNumericValue) ?: 0.0) + value)
            } else {
                DONE_MARK
            }
        }

    /** Marks today as skipped ("—"). */
    suspend fun skip(query: HabitQuery): AutomationResult = mark(query) { _, _ -> INACTIVE_MARK }

    /** Habits not done or skipped today, by title. */
    suspend fun leftToday(): List<Habit> {
        val date = today()
        val marks = dayStates.getRange(date, date).associate { it.habitId to it.mark }
        return habits().filter { todayStatus(it, date, marks[it.id]) == TodayStatus.NOT_DONE }
    }

    /** Stores the mark [markOf] gives for the habit of [query] and its current mark of today. */
    private suspend fun mark(query: HabitQuery, markOf: (Habit, String?) -> String): AutomationResult {
        val habit = find(query) ?: return AutomationResult.NotFound(query)
        val date = today()
        val current = dayStates.getRange(date, date).firstOrNull { it.habitId == habit.id }?.mark
        val mark = markOf(habit, current)
        dayStates.setMark(DayState(habitId = habit.id, date = date, mark = mark))
        DataChanges.publish()
        return AutomationResult.Marked(habit, mark)
    }
}

/*
 * Changes of the stored data made outside the shown screens (e.g. by Siri), within the process.
 */
object DataChanges {
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val events: SharedFlow<Unit> = changes.asSharedFlow()

    fun publish() {
        changes.tryEmit(Unit)
    }
}
