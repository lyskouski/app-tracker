// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource

import com.tercad.zwyka.core.model.ThemeMode
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.theme_dark
import com.tercad.zwyka.resources.theme_light
import com.tercad.zwyka.resources.theme_system

val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.SYSTEM -> Res.string.theme_system
        ThemeMode.LIGHT -> Res.string.theme_light
        ThemeMode.DARK -> Res.string.theme_dark
    }

val themeModeOptions = ThemeMode.entries
