// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringArrayResource

import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.days_short
import com.tercad.zwyka.widgets.SegmentedSelector

/*
 * Week days toggles over the [activeDays] bitmask (bit 0 is Monday ... bit 6 is Sunday),
 * ordered from [weekStart].
 */
@Composable
fun WeekDaysSelector(
    activeDays: Int,
    onChange: (Int) -> Unit,
    weekStart: WeekStart,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val dayNames = stringArrayResource(Res.array.days_short)
    val first = weekStart.dayOfWeek.ordinal
    val days = List(7) { (first + it) % 7 }
    SegmentedSelector(
        labels = days.map { dayNames.getOrElse(it) { "" } },
        isSelected = { activeDays and (1 shl days[it]) != 0 },
        onClick = { onChange(activeDays xor (1 shl days[it])) },
        enabled = enabled,
        multiple = true,
        modifier = modifier,
    )
}
