// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.HabitDraft

/*
 * HabitBull CSV export: one row per day of a habit:
 * HabitName, HabitDescription, HabitCategory, CalendarDate (2016-03-18 or 3/18/2016), Value, CommentText.
 *
 * Value 0 is not done and 1 is done; a habit with larger values is numeric, its target is the median
 * value (the export has no goals). Categories and comments are not imported.
 */

internal fun parseHabitBull(text: String): List<ImportedHabit> {
    val rows = parseCsv(text).withHeader()
    return rows.groupBy { it["HabitName"].orEmpty().trim() }
        .filterKeys { it.isNotEmpty() }
        .map { (name, days) ->
            val values = days.map { it["Value"]?.trim()?.toDoubleOrNull() }
            val isNumeric = values.any { (it ?: 0.0) > 1 }
            val marks = days.indices.mapNotNull { index ->
                val date = parseExportDate(days[index]["CalendarDate"].orEmpty()) ?: return@mapNotNull null
                // Negative values are noise of the app.
                val value = values[index]?.takeIf { it > 0 } ?: return@mapNotNull null
                date.toString() to if (isNumeric) numericMark(value) else DONE_MARK
            }.toMap()
            val positive = values.filterNotNull().filter { it > 0 }.sorted()
            ImportedHabit(
                draft = HabitDraft(
                    title = name,
                    icon = null,
                    color = null,
                    remarks = days.firstNotNullOfOrNull { it["HabitDescription"]?.trim()?.ifEmpty { null } },
                    activeDays = ALL_DAYS,
                    targetValue = if (isNumeric) positive[positive.size / 2] else null,
                ),
                marks = marks,
            )
        }
}
