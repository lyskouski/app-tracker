// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.formatNumericValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/*
 * Import of the habits exported by other habit trackers (the format is detected by the content):
 * - Loop Habit Tracker: database backup (.db) and CSV export (.zip);
 * - HabitKit: JSON export;
 * - Habitify: CSV export;
 * - HabitBull: CSV export.
 */

/** Habit of another app, with its marks: ISO date to a mark of [com.tercad.zwyka.core.model.DayState]. */
data class ImportedHabit(
    val draft: HabitDraft,
    val marks: Map<String, String>,
    /** Creation time in epoch milliseconds; 0 when unknown. */
    val createdAt: Long = 0,
)

/** Outcome of an import: new habits and added marks. */
data class ImportSummary(val habits: Int, val marks: Int)

enum class HabitExportFormat { LOOP_DATABASE, LOOP_CSV, HABITKIT, HABITIFY, HABITBULL }

/** The file is not an export of a supported app. */
class UnsupportedExportException : IllegalArgumentException("Unsupported file format")

/** Format of the exported [bytes], or null when it is not supported. */
fun detectExportFormat(bytes: ByteArray): HabitExportFormat? = when {
    isSqlite(bytes) -> SqliteFile(bytes).takeIf { it.hasTable("Habits") && it.hasTable("Repetitions") }
        ?.let { HabitExportFormat.LOOP_DATABASE }
    isZip(bytes) -> readZip(bytes).keys.takeIf { "Habits.csv" in it }?.let { HabitExportFormat.LOOP_CSV }
    else -> detectTextFormat(bytes.decodeToString().withoutBom())
}

/** Habits of the exported [bytes]. @throws UnsupportedExportException for an unknown format. */
fun parseHabitExport(bytes: ByteArray): List<ImportedHabit> =
    when (detectExportFormat(bytes) ?: throw UnsupportedExportException()) {
        HabitExportFormat.LOOP_DATABASE -> parseLoopDatabase(SqliteFile(bytes))
        HabitExportFormat.LOOP_CSV -> parseLoopCsv(readZip(bytes))
        HabitExportFormat.HABITKIT -> parseHabitKit(bytes.decodeToString().withoutBom())
        HabitExportFormat.HABITIFY -> parseHabitify(bytes.decodeToString().withoutBom())
        HabitExportFormat.HABITBULL -> parseHabitBull(bytes.decodeToString().withoutBom())
    }

private fun detectTextFormat(text: String): HabitExportFormat? {
    val start = text.trimStart()
    if (start.startsWith("{")) {
        val root = runCatching { parseJson(start) }.getOrNull().jsonObject() ?: return null
        return HabitExportFormat.HABITKIT.takeIf { "habits" in root && "completions" in root }
    }
    val header = start.lineSequence().firstOrNull().orEmpty()
    val columns = parseCsv(header, csvDelimiter(header)).firstOrNull().orEmpty().map { it.trim().lowercase() }
    return when {
        "habitname" in columns && "calendardate" in columns -> HabitExportFormat.HABITBULL
        "habit" in columns && "date" in columns && "status" in columns -> HabitExportFormat.HABITIFY
        else -> null
    }
}

/** Delimiter of a CSV [header]: spreadsheet tools of some locales use ";" or a tab. */
internal fun csvDelimiter(header: String): Char =
    listOf(',', ';', '\t').maxBy { delimiter -> header.count { it == delimiter } }

/** Mark of a reached [value] of a numeric target. */
internal fun numericMark(value: Double): String = formatNumericValue(value)

/** "#RRGGBB" of a 24-bit [rgb]. */
internal fun hexColor(rgb: Int): String = "#" + rgb.toString(16).uppercase().padStart(6, '0')

/**
 * Date of the other apps' exports: ISO (2026-10-25, also with a time), "10/25/2026" (US),
 * "25.10.2026", "Oct 25, 2026" or "25 Oct 2026". @return null when not recognized.
 */
internal fun parseExportDate(text: String): LocalDate? {
    val value = text.trim()
    fun date(year: String, month: Int?, day: String): LocalDate? =
        runCatching { LocalDate(year.toInt(), month ?: return null, day.toInt()) }.getOrNull()
    ISO_DATE.find(value)?.destructured?.let { (year, month, day) -> return date(year, month.toInt(), day) }
    US_DATE.matchEntire(value.substringBefore(' '))?.destructured?.let { (month, day, year) ->
        return date(year, month.toInt(), day)
    }
    DOTTED_DATE.matchEntire(value.substringBefore(' '))?.destructured?.let { (day, month, year) ->
        return date(year, month.toInt(), day)
    }
    MONTH_FIRST_DATE.find(value)?.destructured?.let { (month, day, year) -> return date(year, monthOf(month), day) }
    DAY_FIRST_DATE.find(value)?.destructured?.let { (day, month, year) -> return date(year, monthOf(month), day) }
    return null
}

private val ISO_DATE = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})""")
private val US_DATE = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4})$""")
private val DOTTED_DATE = Regex("""^(\d{1,2})\.(\d{1,2})\.(\d{4})$""")
private val MONTH_FIRST_DATE = Regex("""^([A-Za-z]{3,})\.? (\d{1,2}),? (\d{4})""")
private val DAY_FIRST_DATE = Regex("""^(\d{1,2}) ([A-Za-z]{3,})\.?,? (\d{4})""")

private fun monthOf(name: String): Int? =
    Month.entries.firstOrNull { it.name.startsWith(name.uppercase().take(3)) }?.let { it.ordinal + 1 }
