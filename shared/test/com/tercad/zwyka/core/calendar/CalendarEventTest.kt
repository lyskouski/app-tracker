// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class CalendarEventTest {
    // Saturday
    private val saturday = LocalDate(2026, 10, 17)

    // Monday + Wednesday
    private val event = CalendarEvent(
        title = "Run & stretch",
        details = "Park, 5 km",
        from = saturday,
        timeStart = 9 * 60 + 30,
        timeEnd = 10 * 60,
        activeDays = 0b101,
    )

    @Test
    fun startsOnFirstActiveDay() {
        assertEquals(LocalDate(2026, 10, 19), event.date)
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE", event.recurrence)
        assertEquals("FREQ=DAILY", event.copy(activeDays = 0x7F).recurrence)
    }

    @Test
    fun buildsGoogleTemplate() {
        assertEquals(
            "https://calendar.google.com/calendar/render?action=TEMPLATE&text=Run%20%26%20stretch" +
                "&dates=20261019T093000/20261019T100000&details=Park%2C%205%20km" +
                "&recur=RRULE%3AFREQ%3DWEEKLY%3BBYDAY%3DMO%2CWE",
            event.googleCalendarUrl(),
        )
    }

    @Test
    fun buildsOutlookComposeEndingAtMidnight() {
        val url = event.copy(timeEnd = 24 * 60, details = null).outlookCalendarUrl()
        assertTrue(url.endsWith("&startdt=2026-10-19T09%3A30%3A00&enddt=2026-10-20T00%3A00%3A00"), url)
    }

    @Test
    fun writesIcsFile() {
        val ics = event.icsContent(Instant.parse("2026-10-18T08:24:18.123Z"))
        assertEquals(
            listOf(
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//terCAD//Zwyka//EN",
                "CALSCALE:GREGORIAN",
                "BEGIN:VEVENT",
                "UID:1792311858123@zwyka.tercad.com",
                "DTSTAMP:20261018T082418Z",
                "DTSTART:20261019T093000",
                "DTEND:20261019T100000",
                "RRULE:FREQ=WEEKLY;BYDAY=MO,WE",
                "SUMMARY:Run & stretch",
                "DESCRIPTION:Park\\, 5 km",
                "END:VEVENT",
                "END:VCALENDAR",
                "",
            ),
            ics.split("\r\n"),
        )
        assertEquals("Run--stretch.ics", event.icsFileName())
    }

    @Test
    fun foldsLongIcsLines() {
        val lines = event.copy(details = "ü".repeat(80)).icsContent(Instant.parse("2026-10-18T00:00:00Z"))
            .split("\r\n")
        assertTrue(lines.all { it.encodeToByteArray().size <= 75 })
        val description = lines.dropWhile { !it.startsWith("DESCRIPTION:") }
            .takeWhile { it.startsWith("DESCRIPTION:") || it.startsWith(" ") }
            .joinToString("") { it.removePrefix(" ") }
        assertEquals("DESCRIPTION:" + "ü".repeat(80), description)
    }
}
