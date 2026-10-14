// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import kotlinx.datetime.LocalDate

enum class DateFormat(val label: String) {
    YYYY_MM_DD("YYYY-MM-DD"),
    MM_DD_YYYY_DASH("MM-DD-YYYY"),
    DD_MM_YYYY_SLASH("DD/MM/YYYY"),
    DD_MM_YYYY_DOT("DD.MM.YYYY"),
    MM_DD_YYYY_SLASH("MM/DD/YYYY"),
    YYYY_MM_DD_SLASH("YYYY/MM/DD"),
    DD_MMM("DD MMM");

    /** [monthNames] holds the 12 localized short month names, January first. */
    fun format(isoDate: String, monthNames: List<String>): String {
        val date = LocalDate.parse(isoDate)
        val year = date.year.toString().padStart(4, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        return when (this) {
            YYYY_MM_DD -> "$year-$month-$day"
            MM_DD_YYYY_DASH -> "$month-$day-$year"
            DD_MM_YYYY_SLASH -> "$day/$month/$year"
            DD_MM_YYYY_DOT -> "$day.$month.$year"
            MM_DD_YYYY_SLASH -> "$month/$day/$year"
            YYYY_MM_DD_SLASH -> "$year/$month/$day"
            DD_MMM -> "${date.dayOfMonth} ${monthNames[date.monthNumber - 1]}"
        }
    }
}

val dateFormatOptions = DateFormat.entries
