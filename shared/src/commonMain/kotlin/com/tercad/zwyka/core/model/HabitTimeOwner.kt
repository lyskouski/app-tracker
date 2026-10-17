// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

/*
 * Owner of a scheduled time range: a habit or an intent.
 */
sealed interface HabitTimeOwner {
    data class OfHabit(val habitId: Long) : HabitTimeOwner

    /** [intentId] is null for a new intent. */
    data class OfIntent(val intentId: Long?, val title: String) : HabitTimeOwner
}
