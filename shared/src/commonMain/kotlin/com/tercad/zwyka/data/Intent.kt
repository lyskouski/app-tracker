// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

/** Like [Habit], but used only for scheduling in the calendar (not shown in the habits table). */
data class Intent(
    val id: Long,
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val remarks: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val activeDays: Int = ALL_DAYS,
)
