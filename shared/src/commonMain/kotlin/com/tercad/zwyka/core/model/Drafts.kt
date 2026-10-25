// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

/** Editable fields of a [Habit]. */
data class HabitDraft(
    val title: String,
    val icon: String?,
    val color: String?,
    val remarks: String?,
    val activeDays: Int,
    val isArchived: Boolean = false,
    val targetValue: Double? = null,
    val targetType: String? = null,
)

/** Editable fields of a [HabitTime]. */
data class HabitTimeDraft(
    val owner: HabitTimeOwner,
    val timeStart: Int,
    val timeEnd: Int,
    val note: String?,
    val activeDays: Int,
    val notify: Boolean,
)
