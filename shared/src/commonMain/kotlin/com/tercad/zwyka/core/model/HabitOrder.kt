// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

enum class HabitOrderBy {
    CREATED_AT,
    UPDATED_AT,
    TITLE,
}

enum class SortDirection {
    ASC,
    DESC,
}

fun List<Habit>.sortedBy(orderBy: HabitOrderBy, direction: SortDirection): List<Habit> {
    val comparator: Comparator<Habit> = when (orderBy) {
        HabitOrderBy.CREATED_AT -> compareBy<Habit> { it.createdAt }.thenBy { it.id }
        HabitOrderBy.UPDATED_AT -> compareBy<Habit> { it.updatedAt }.thenBy { it.id }
        HabitOrderBy.TITLE -> compareBy<Habit, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
            .thenBy { it.id }
    }
    return sortedWith(if (direction == SortDirection.DESC) comparator.reversed() else comparator)
}
