// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

import com.tercad.zwyka.defines.CompletenessType
import com.tercad.zwyka.defines.DateFormat
import com.tercad.zwyka.defines.HabitOrderBy
import com.tercad.zwyka.defines.HomeView
import com.tercad.zwyka.defines.SortDirection
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.defines.WeekStart

data class AppSettings(
    val language: String? = null,
    val weekStart: WeekStart = WeekStart.MONDAY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    val habitOrderBy: HabitOrderBy = HabitOrderBy.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC,
    val completenessType: CompletenessType = CompletenessType.SYMBOLS,
    val homeView: HomeView = HomeView.OVERALL,
)
