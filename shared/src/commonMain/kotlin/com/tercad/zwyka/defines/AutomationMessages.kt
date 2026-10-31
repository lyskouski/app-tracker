// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.getString

import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.service.AutomationResult
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.automation_all_done
import com.tercad.zwyka.resources.automation_habit_not_found
import com.tercad.zwyka.resources.automation_left_today
import com.tercad.zwyka.resources.skip
import com.tercad.zwyka.resources.status_100_percent

/*
 * Outcome of an automation request, spoken by Siri or returned to the sender of an intent:
 * "Water: 2 / 8 glasses", "Reading: 100%", "Run: Skip".
 */
suspend fun AutomationResult.message(): String = when (this) {
    is AutomationResult.Marked -> {
        val state = when (mark) {
            DONE_MARK -> getString(Res.string.status_100_percent)
            INACTIVE_MARK -> getString(Res.string.skip)
            else -> listOfNotNull(
                habit.targetValue?.let { "$mark / ${formatNumericValue(it)}" } ?: mark,
                habit.targetType?.takeIf { it.isNotBlank() },
            ).joinToString(" ")
        }
        "${habit.title}: $state"
    }
    is AutomationResult.NotFound ->
        getString(Res.string.automation_habit_not_found, query.title ?: query.id?.toString().orEmpty())
}

/** Habits left for today: "Left today: Water, Reading", or that all of them are done. */
suspend fun leftTodayMessage(habits: List<Habit>): String =
    if (habits.isEmpty()) {
        getString(Res.string.automation_all_done)
    } else {
        getString(Res.string.automation_left_today, habits.joinToString(", ") { it.title })
    }
