// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

/*
 * Progress marks of a day, from "started" to "over 100%".
 */
object StatusMarks {
    const val START = "·"
    const val PERCENT_25 = "\\"
    const val PERCENT_50 = "|"
    const val PERCENT_75 = "/"
    const val PERCENT_100 = "X"
    const val OVER_100 = "▣"

    val all = listOf(START, PERCENT_25, PERCENT_50, PERCENT_75, PERCENT_100, OVER_100)

    fun isStatus(mark: String?): Boolean = mark in all

    /** Completion of [mark], 0..1: "·" = 0%, "X" = 100%; over 100% and unknown marks are capped / 0. */
    fun progress(mark: String?): Float =
        (all.indexOf(mark).coerceAtLeast(0) * 0.25f).coerceAtMost(1f)
}
