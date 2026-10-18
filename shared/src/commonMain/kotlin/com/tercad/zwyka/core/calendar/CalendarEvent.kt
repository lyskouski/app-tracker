// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.calendar

import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.MINUTES_PER_DAY

const val CALENDAR_EXTENSION = "ics"

/*
 * Scheduled time range as an event of an external calendar (Google, Apple, Microsoft),
 * repeated weekly on [activeDays] (same bitmask as [com.tercad.zwyka.core.model.HabitTime.activeDays]).
 *
 * Times are floating: each calendar shows them in its own time zone.
 */
data class CalendarEvent(
    val title: String,
    val details: String?,
    /** The event starts on the first active day from this date on. */
    val from: LocalDate,
    /** Minutes since midnight. */
    val timeStart: Int,
    /** Minutes since midnight, up to [MINUTES_PER_DAY]. */
    val timeEnd: Int,
    val activeDays: Int = ALL_DAYS,
) {
    /** Date of the first occurrence. */
    val date: LocalDate
        get() = (0..6).map { from.plus(it, DateTimeUnit.DAY) }
            .firstOrNull { activeDays and (1 shl it.dayOfWeek.ordinal) != 0 }
            ?: from

    /** Recurrence rule (RFC 5545) without the "RRULE:" prefix. */
    val recurrence: String
        get() = if (activeDays and ALL_DAYS == ALL_DAYS) {
            "FREQ=DAILY"
        } else {
            val days = DayOfWeek.entries
                .filter { activeDays and (1 shl it.ordinal) != 0 }
                .joinToString(",") { it.name.take(2) }
            "FREQ=WEEKLY;BYDAY=$days"
        }
}

/** Template link that opens a prefilled new event in Google Calendar. */
fun CalendarEvent.googleCalendarUrl(): String = buildString {
    append("https://calendar.google.com/calendar/render?action=TEMPLATE")
    append("&text=").append(urlEncode(title))
    append("&dates=").append(basicDateTime(date, timeStart))
    append('/').append(basicDateTime(date, timeEnd))
    details?.let { append("&details=").append(urlEncode(it)) }
    append("&recur=").append(urlEncode("RRULE:$recurrence"))
}

/** Compose link of Outlook on the web; it does not take a recurrence, so only the first occurrence is added. */
fun CalendarEvent.outlookCalendarUrl(): String = buildString {
    append("https://outlook.live.com/calendar/0/deeplink/compose?path=%2Fcalendar%2Faction%2Fcompose&rru=addevent")
    append("&subject=").append(urlEncode(title))
    append("&startdt=").append(urlEncode(extendedDateTime(date, timeStart)))
    append("&enddt=").append(urlEncode(extendedDateTime(date, timeEnd)))
    details?.let { append("&body=").append(urlEncode(it)) }
}

/** iCalendar file (RFC 5545), opened by Apple Calendar and any other calendar app. */
fun CalendarEvent.icsContent(now: Instant): String = listOf(
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//terCAD//Zwyka//EN",
    "CALSCALE:GREGORIAN",
    "BEGIN:VEVENT",
    "UID:${now.toEpochMilliseconds()}@zwyka.tercad.com",
    // "2026-10-18T08:24:18.123Z" -> "20261018T082418Z"
    "DTSTAMP:${now.toString().filter { it.isDigit() || it == 'T' }.take(15)}Z",
    "DTSTART:${basicDateTime(date, timeStart)}",
    "DTEND:${basicDateTime(date, timeEnd)}",
    "RRULE:$recurrence",
    "SUMMARY:${icsText(title)}",
    details?.let { "DESCRIPTION:${icsText(it)}" },
    "END:VEVENT",
    "END:VCALENDAR",
).filterNotNull().joinToString("\r\n", postfix = "\r\n") { foldIcsLine(it) }

fun CalendarEvent.icsFileName(): String {
    val name = title.filter { it.isLetterOrDigit() || it == ' ' || it == '-' }.trim().replace(' ', '-')
    return "${name.ifEmpty { "zwyka" }}.$CALENDAR_EXTENSION"
}

// "24:00" is the midnight of the next day.
private fun dateTime(date: LocalDate, minutes: Int): Pair<LocalDate, Int> =
    if (minutes >= MINUTES_PER_DAY) date.plus(1, DateTimeUnit.DAY) to minutes - MINUTES_PER_DAY else date to minutes

/** "20261018T093000" */
private fun basicDateTime(date: LocalDate, minutes: Int): String {
    val (day, time) = dateTime(date, minutes)
    return "${day.year.pad(4)}${day.monthNumber.pad()}${day.dayOfMonth.pad()}" +
        "T${(time / 60).pad()}${(time % 60).pad()}00"
}

/** "2026-10-18T09:30:00" */
private fun extendedDateTime(date: LocalDate, minutes: Int): String {
    val (day, time) = dateTime(date, minutes)
    return "$day" + "T${(time / 60).pad()}:${(time % 60).pad()}:00"
}

private fun Int.pad(length: Int = 2) = toString().padStart(length, '0')

private const val UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"

/** Percent-encodes [text] as UTF-8 for a query parameter. */
internal fun urlEncode(text: String): String = buildString {
    text.encodeToByteArray().forEach { byte ->
        val char = (byte.toInt() and 0xFF).toChar()
        if (char in UNRESERVED) {
            append(char)
        } else {
            append('%').append((byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
        }
    }
}

private fun icsText(text: String): String = text
    .replace("\\", "\\\\")
    .replace(";", "\\;")
    .replace(",", "\\,")
    .replace("\r\n", "\\n")
    .replace("\n", "\\n")

// Lines longer than 75 octets continue on the next line after a space.
private fun foldIcsLine(line: String): String {
    val parts = mutableListOf<String>()
    var current = StringBuilder()
    var octets = 0
    line.forEach { char ->
        // UTF-8 octets; a surrogate pair counts on its first half, so it is never split.
        val size = when {
            char.isLowSurrogate() -> 0
            char.isHighSurrogate() -> 4
            char.code < 0x80 -> 1
            char.code < 0x800 -> 2
            else -> 3
        }
        if (octets + size > 75) {
            parts += current.toString()
            current = StringBuilder(" ")
            octets = 1
        }
        current.append(char)
        octets += size
    }
    parts += current.toString()
    return parts.joinToString("\r\n")
}
