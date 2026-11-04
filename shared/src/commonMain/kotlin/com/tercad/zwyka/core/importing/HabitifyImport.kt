// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.parseNumericValue

/*
 * Habitify (https://habitify.me) CSV export: one row per check-in, delimited by "," or ";":
 * Habit, Date, Total log, Unit, Status (Completed, Skipped, Failed; Succeeded / Failed of a habit
 * to avoid), Habit ID.
 *
 * A habit is numeric when its unit is not a count of times, or when it logs more than 1 a day;
 * its target is the smallest completed log (the export has no goals). A failure of a habit to avoid
 * is a relapse. The Habitify backup (.sqlite) is not supported.
 */

/** Units of a habit that is done once a day. */
private val TIMES_UNITS = setOf("", "time", "times", "count", "rep", "reps")

internal fun parseHabitify(text: String): List<ImportedHabit> {
    val header = text.lineSequence().firstOrNull().orEmpty()
    val rows = parseCsv(text, csvDelimiter(header)).withHeader().map { row ->
        row.mapKeys { (key, _) -> key.lowercase() }
    }
    return rows.groupBy { row -> row["habit id"]?.trim()?.ifEmpty { null } ?: row["habit"].orEmpty().trim() }
        .values
        .map { logs ->
            val statuses = logs.map { it["status"].orEmpty().trim().lowercase() }
            val values = logs.map { it["total log"]?.let(::parseNumericValue) }
            val isNegative = "succeeded" in statuses
            val unit = logs.firstNotNullOfOrNull { it["unit"]?.trim()?.ifEmpty { null } }
            val isNumeric = !isNegative &&
                (unit.orEmpty().lowercase() !in TIMES_UNITS || values.any { (it ?: 0.0) > 1 })
            val completedValues = values.filterIndexed { index, value ->
                statuses[index] == "completed" && value != null && value > 0
            }.filterNotNull()

            val marks = logs.indices.mapNotNull { index ->
                val date = parseExportDate(logs[index]["date"].orEmpty()) ?: return@mapNotNull null
                val value = values[index]
                val mark = when {
                    statuses[index] == "skipped" -> INACTIVE_MARK
                    isNegative -> DONE_MARK.takeIf { statuses[index] == "failed" }
                    isNumeric -> value?.takeIf { it > 0 }?.let(::numericMark)
                    else -> DONE_MARK.takeIf { statuses[index] == "completed" }
                }
                mark?.let { date.toString() to it }
            }.toMap()

            ImportedHabit(
                draft = HabitDraft(
                    title = logs.first()["habit"].orEmpty().trim(),
                    icon = null,
                    color = null,
                    remarks = null,
                    activeDays = ALL_DAYS,
                    targetValue = if (isNumeric) {
                        completedValues.minOrNull() ?: values.filterNotNull().maxOrNull()?.takeIf { it > 0 } ?: 1.0
                    } else {
                        null
                    },
                    targetType = if (isNumeric) unit else null,
                    isNegative = isNegative,
                ),
                marks = marks,
            )
        }
}
