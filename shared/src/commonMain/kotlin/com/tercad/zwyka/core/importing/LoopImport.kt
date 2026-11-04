// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.INACTIVE_MARK
import com.tercad.zwyka.core.model.parseNumericValue
import kotlinx.datetime.LocalDate

/*
 * Loop Habit Tracker (https://github.com/iSoron/uhabits).
 *
 * Database backup (.db, SQLite):
 * - Habits: id, name, question, description, color (index of the palette, ARGB in old versions),
 *   position, archived (0/1), type (0: yes/no, 1: numerical), target_value, unit, freq_num / freq_den;
 * - Repetitions: habit, timestamp (UTC midnight in milliseconds), value (done in old versions), notes.
 *
 * CSV export (.zip): Habits.csv (Position, Name, Type, Question, Description, Color "#RRGGBB", Unit,
 * Target Value, Archived?), and "<Position> <Name>/Checkmarks.csv" per habit (Date, Value, Notes).
 *
 * Values: 2 / YES_MANUAL is done, 3 / SKIP is skipped, 0 / NO and -1 / UNKNOWN are not marked;
 * 1 / YES_AUTO (a day covered by "N times per period") is not marked either. A numerical habit
 * keeps the value multiplied by 1000. The frequency, reminders and notes have no counterpart.
 */

/** Colors of the Loop palette, by index. */
private val LOOP_COLORS = intArrayOf(
    0xD32F2F, 0xE64A19, 0xF57C00, 0xFF8F00, 0xF9A825, 0xAFB42B, 0x7CB342, 0x388E3C, 0x00897B, 0x00ACC1,
    0x039BE5, 0x1976D2, 0x303F9F, 0x5E35B1, 0x8E24AA, 0xD81B60, 0x5D4037, 0x424242, 0x757575, 0x9E9E9E,
)

/** Color of the palette [index]; old databases keep the ARGB color itself. */
private fun loopColor(index: Int): String = hexColor(LOOP_COLORS.getOrNull(index) ?: (index and 0xFFFFFF))

private const val LOOP_YES_MANUAL = 2L
private const val LOOP_SKIP = 3L
private const val DAY_MILLISECONDS = 86_400_000L

internal fun parseLoopDatabase(database: SqliteFile): List<ImportedHabit> {
    val repetitions = database.table("Repetitions").groupBy { it["habit"] as? Long }
    return database.table("Habits")
        .sortedBy { it["position"] as? Long ?: 0 }
        .map { row ->
            val isNumerical = row["type"] as? Long == 1L
            val marks = repetitions[row["id"] as? Long].orEmpty().mapNotNull { repetition ->
                val timestamp = repetition["timestamp"] as? Long ?: return@mapNotNull null
                val value = repetition["value"] as? Long ?: LOOP_YES_MANUAL
                val date = LocalDate.fromEpochDays(timestamp.floorDiv(DAY_MILLISECONDS).toInt())
                loopMark(value, isNumerical)?.let { date.toString() to it }
            }.toMap()
            ImportedHabit(
                draft = loopDraft(
                    name = row["name"] as? String,
                    question = row["question"] as? String,
                    description = row["description"] as? String,
                    color = (row["color"] as? Long)?.toInt()?.let(::loopColor),
                    isArchived = row["archived"] as? Long == 1L,
                    isNumerical = isNumerical,
                    targetValue = (row["target_value"] as? Number)?.toDouble(),
                    unit = row["unit"] as? String,
                ),
                marks = marks,
            )
        }
}

internal fun parseLoopCsv(files: Map<String, ByteArray>): List<ImportedHabit> {
    val habits = parseCsv(files.getValue("Habits.csv").decodeToString().withoutBom()).withHeader()
    // Older versions have no per habit files: the columns of Checkmarks.csv follow Habits.csv.
    val combined = files["Checkmarks.csv"]?.let { parseCsv(it.decodeToString().withoutBom()) }.orEmpty()
    return habits.mapIndexed { index, row ->
        val isNumerical = row["Type"].equals("NUMERICAL", ignoreCase = true)
        val position = row["Position"].orEmpty()
        val own = files.entries.firstOrNull { (path, _) ->
            path.startsWith("$position ") && path.endsWith("/Checkmarks.csv")
        }
        val values: List<Pair<String, String>> = if (own != null) {
            parseCsv(own.value.decodeToString().withoutBom()).drop(1)
                .mapNotNull { cells -> cells.getOrNull(1)?.let { cells[0] to it } }
        } else {
            combined.drop(1).mapNotNull { cells -> cells.getOrNull(index + 1)?.let { cells[0] to it } }
        }
        val marks = values.mapNotNull { (dateText, valueText) ->
            val date = parseExportDate(dateText) ?: return@mapNotNull null
            loopCsvMark(valueText.trim(), isNumerical)?.let { date.toString() to it }
        }.toMap()
        ImportedHabit(
            draft = loopDraft(
                name = row["Name"],
                question = row["Question"],
                description = row["Description"],
                color = row["Color"]?.trim()?.takeIf { it.startsWith("#") }?.uppercase(),
                isArchived = row["Archived?"].toBoolean(),
                isNumerical = isNumerical,
                targetValue = row["Target Value"]?.let(::parseNumericValue),
                unit = row["Unit"],
            ),
            marks = marks,
        )
    }
}

private fun loopDraft(
    name: String?,
    question: String?,
    description: String?,
    color: String?,
    isArchived: Boolean,
    isNumerical: Boolean,
    targetValue: Double?,
    unit: String?,
) = HabitDraft(
    title = name?.trim().orEmpty(),
    icon = null,
    color = color,
    remarks = listOfNotNull(question, description).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        .joinToString("\n").ifEmpty { null },
    activeDays = ALL_DAYS,
    isArchived = isArchived,
    targetValue = if (isNumerical) targetValue?.takeIf { it > 0 } ?: 1.0 else null,
    targetType = if (isNumerical) unit?.trim()?.ifEmpty { null } else null,
)

private fun loopMark(value: Long, isNumerical: Boolean): String? = when {
    value == LOOP_SKIP -> INACTIVE_MARK
    isNumerical -> numericMark(value / 1000.0).takeIf { value > 0 }
    value == LOOP_YES_MANUAL -> DONE_MARK
    else -> null
}

private fun loopCsvMark(value: String, isNumerical: Boolean): String? = when (value.uppercase()) {
    "YES_MANUAL" -> DONE_MARK
    "SKIP" -> INACTIVE_MARK
    "YES_AUTO", "NO", "UNKNOWN", "" -> null
    else -> {
        val number = parseNumericValue(value)
        when {
            number == null -> null
            // Integers are the stored values (thousandths of a numerical habit).
            '.' !in value && ',' !in value -> loopMark(number.toLong(), isNumerical)
            isNumerical && number > 0 -> numericMark(number)
            else -> null
        }
    }
}
