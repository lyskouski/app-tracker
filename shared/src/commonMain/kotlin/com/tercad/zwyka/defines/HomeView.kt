// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.overall
import com.tercad.zwyka.resources.today
import com.tercad.zwyka.resources.weekly

/** Mode of the home page, switched by its top tabs. */
enum class HomeView(val label: StringResource, val icon: ImageVector) {
    TODAY(Res.string.today, Icons.Filled.Today),
    WEEKLY(Res.string.weekly, Icons.Filled.CalendarViewWeek),
    OVERALL(Res.string.overall, Icons.Filled.CalendarViewMonth),
}

val homeViewOptions = HomeView.entries
