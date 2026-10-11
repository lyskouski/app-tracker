// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.theme_dark
import com.tercad.zwyka.resources.theme_light
import com.tercad.zwyka.resources.theme_system

enum class ThemeMode(val label: StringResource) {
    SYSTEM(Res.string.theme_system),
    LIGHT(Res.string.theme_light),
    DARK(Res.string.theme_dark),
}

val themeModeOptions = ThemeMode.entries
