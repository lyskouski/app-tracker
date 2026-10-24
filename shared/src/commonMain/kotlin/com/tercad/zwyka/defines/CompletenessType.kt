// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.completeness_colors
import com.tercad.zwyka.resources.completeness_symbols

/** How status marks are shown; the stored marks are always symbols. */
enum class CompletenessType(val label: StringResource) {
    SYMBOLS(Res.string.completeness_symbols),
    COLORS(Res.string.completeness_colors),
}

val completenessTypeOptions = CompletenessType.entries
