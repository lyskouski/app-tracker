// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.overall
import com.tercad.zwyka.resources.today
import com.tercad.zwyka.resources.weekly

/** Mode of the home page, switched by its top toggle. */
enum class HomeView(val label: StringResource) {
    TODAY(Res.string.today),
    WEEKLY(Res.string.weekly),
    OVERALL(Res.string.overall),
}

val homeViewOptions = HomeView.entries
