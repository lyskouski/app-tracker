// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DONE_MARK
import com.tercad.zwyka.core.model.HabitDraft
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/*
 * HabitKit (https://habitkit.app) JSON export, `formatVersion` 2:
 * - habits: id, name, description, icon, color (Tailwind name), archived, orderIndex, createdAt (ISO),
 *   isInverse (habit to quit), kind ("measure" with a unit, else taps);
 * - completions: habitId, date (local midnight in UTC), timezoneOffsetInMinutes, amountOfCompletions, note;
 * - intervals (goals): habitId, startDate, endDate, requiredNumberOfCompletionsPerDay.
 *
 * A completion of a habit to quit is a relapse. Emoji icons, reminders, categories and notes are not imported.
 */

/** Tailwind colors (shade 600) that HabitKit names. */
private val HABITKIT_COLORS = mapOf(
    "red" to 0xDC2626, "orange" to 0xEA580C, "amber" to 0xD97706, "yellow" to 0xCA8A04, "lime" to 0x65A30D,
    "green" to 0x16A34A, "emerald" to 0x059669, "teal" to 0x0D9488, "cyan" to 0x0891B2, "sky" to 0x0284C7,
    "blue" to 0x2563EB, "indigo" to 0x4F46E5, "violet" to 0x7C3AED, "purple" to 0x9333EA, "fuchsia" to 0xC026D3,
    "pink" to 0xDB2777, "rose" to 0xE11D48, "slate" to 0x475569, "gray" to 0x4B5563, "zinc" to 0x52525B,
    "neutral" to 0x525252, "stone" to 0x57534E,
)

/** HabitKit icons with a Material counterpart (see [com.tercad.zwyka.defines.applicationIcons]). */
private val HABITKIT_ICONS = mapOf(
    "bath" to "Bathtub", "bed" to "Bed", "bike" to "DirectionsBike", "book" to "MenuBook", "bookSpine" to "MenuBook",
    "briefcase" to "Work", "business" to "Work", "capsules" to "Medication", "tablets" to "Medication",
    "cigarette" to "SmokingRooms", "close" to "Block", "coffeebean" to "Coffee", "dumbbell" to "FitnessCenter",
    "film" to "Movie", "graduation" to "School", "moneybill" to "Payments", "moon" to "Bedtime",
    "newspaper" to "Newspaper", "palette" to "Palette", "personMeditation" to "SelfImprovement",
    "sickness" to "Sick", "sun" to "WbSunny",
)

internal fun parseHabitKit(text: String): List<ImportedHabit> {
    val root = parseJson(text).jsonObject() ?: throw UnsupportedExportException()
    val completions = root["completions"].jsonList().mapNotNull { it.jsonObject() }.groupBy { it.text("habitId") }
    val intervals = root["intervals"].jsonList().mapNotNull { it.jsonObject() }.groupBy { it.text("habitId") }
    return root["habits"].jsonList().mapNotNull { it.jsonObject() }
        .sortedBy { it.number("orderIndex") ?: 0.0 }
        .map { habit ->
            val id = habit.text("id")
            val isInverse = habit.flag("isInverse")
            // The goal of today: the interval without an end, else the one that started last.
            val goal = intervals[id].orEmpty().let { own ->
                own.firstOrNull { it["endDate"] == null } ?: own.maxByOrNull { it.text("startDate").orEmpty() }
            }
            val perDay = goal?.number("requiredNumberOfCompletionsPerDay")
            val isMeasure = habit.text("kind") == "measure"
            val isNumeric = !isInverse && (isMeasure || (perDay ?: 1.0) > 1)

            val amounts = mutableMapOf<String, Double>()
            completions[id].orEmpty().forEach { completion ->
                val amount = completion.number("amountOfCompletions") ?: 0.0
                // HabitKit keeps the row of an undone tap with the amount 0.
                if (amount <= 0) return@forEach
                val date = completion.text("date")?.let { runCatching { Instant.parse(it) }.getOrNull() }
                    ?: return@forEach
                val offset = (completion.number("timezoneOffsetInMinutes") ?: 0.0).toLong().minutes
                val day = (date + offset).toLocalDateTime(TimeZone.UTC).date.toString()
                amounts[day] = maxOf(amounts[day] ?: 0.0, amount)
            }
            val color = habit.text("color")?.let { name ->
                if (name.startsWith("#")) name.uppercase() else HABITKIT_COLORS[name]?.let(::hexColor)
            }
            ImportedHabit(
                draft = HabitDraft(
                    title = habit.text("name")?.trim().orEmpty(),
                    icon = habit.text("icon")?.let { HABITKIT_ICONS[it] },
                    color = color,
                    remarks = habit.text("description")?.trim()?.ifEmpty { null },
                    activeDays = ALL_DAYS,
                    isArchived = habit.flag("archived"),
                    targetValue = if (isNumeric) perDay?.takeIf { it > 0 } ?: amounts.values.maxOrNull() ?: 1.0 else null,
                    targetType = if (isNumeric) habit.text("unit")?.trim()?.ifEmpty { null } else null,
                    isNegative = isInverse,
                ),
                marks = amounts.mapValues { (_, amount) -> if (isNumeric) numericMark(amount) else DONE_MARK },
                createdAt = habit.text("createdAt")
                    ?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() } ?: 0,
            )
        }
}
