// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import org.jetbrains.compose.resources.stringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.color_scheme_not_provided

val LocalColorScheme = compositionLocalOf<ColorScheme?> {
    null
}

@Composable
fun currentColorScheme(): ColorScheme =
    LocalColorScheme.current
        ?: error(stringResource(Res.string.color_scheme_not_provided))
