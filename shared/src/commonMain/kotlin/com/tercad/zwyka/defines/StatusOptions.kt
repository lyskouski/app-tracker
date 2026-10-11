// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource

import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.status_100_percent
import com.tercad.zwyka.resources.status_25_percent
import com.tercad.zwyka.resources.status_50_percent
import com.tercad.zwyka.resources.status_75_percent
import com.tercad.zwyka.resources.status_over_100_percent
import com.tercad.zwyka.resources.status_start

data class StatusOption(
    val mark: String,
    val label: StringResource,
)

val statusOptions = listOf(
    StatusOption("·", Res.string.status_start),
    StatusOption("\\", Res.string.status_25_percent),
    StatusOption("|", Res.string.status_50_percent),
    StatusOption("/", Res.string.status_75_percent),
    StatusOption("X", Res.string.status_100_percent),
    StatusOption("▣", Res.string.status_over_100_percent),
)
