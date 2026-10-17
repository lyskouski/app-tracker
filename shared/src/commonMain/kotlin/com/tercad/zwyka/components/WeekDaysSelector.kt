// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringArrayResource

import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.days_short

/*
 * Row of equally wide toggles.
 */
@Composable
fun SegmentedSelector(
    labels: List<String>,
    isSelected: (Int) -> Boolean,
    onClick: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        labels.forEachIndexed { index, label ->
            val selected = isSelected(index)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) currentColorScheme().primary
                        else Color.Transparent,
                    )
                    .border(
                        1.dp,
                        currentColorScheme().outline,
                        RoundedCornerShape(8.dp),
                    )
                    .clickable(enabled = enabled) { onClick(index) },
            ) {
                Text(
                    label,
                    color = if (selected) currentColorScheme().onPrimary
                    else currentColorScheme().onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

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
        modifier = modifier,
    )
}
