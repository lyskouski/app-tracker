// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.order_created_at
import com.tercad.zwyka.resources.order_title
import com.tercad.zwyka.resources.order_updated_at
import com.tercad.zwyka.resources.sort_ascending
import com.tercad.zwyka.resources.sort_descending

enum class HabitOrderBy(val label: StringResource) {
    CREATED_AT(Res.string.order_created_at),
    UPDATED_AT(Res.string.order_updated_at),
    TITLE(Res.string.order_title),
}

enum class SortDirection(val label: StringResource) {
    ASC(Res.string.sort_ascending),
    DESC(Res.string.sort_descending),
}

val habitOrderByOptions = HabitOrderBy.entries
val sortDirectionOptions = SortDirection.entries

fun List<Habit>.sortedBy(orderBy: HabitOrderBy, direction: SortDirection): List<Habit> {
    val comparator: Comparator<Habit> = when (orderBy) {
        HabitOrderBy.CREATED_AT -> compareBy<Habit> { it.createdAt }.thenBy { it.id }
        HabitOrderBy.UPDATED_AT -> compareBy<Habit> { it.updatedAt }.thenBy { it.id }
        HabitOrderBy.TITLE -> compareBy<Habit, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
            .thenBy { it.id }
    }
    return sortedWith(if (direction == SortDirection.DESC) comparator.reversed() else comparator)
}
