// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

private val systemLocale: Locale = Locale.getDefault()
private val LocalLanguageTag = staticCompositionLocalOf<String?> { null }

actual object LocalAppLocale {
    @Composable
    actual infix fun provides(tag: String?): ProvidedValue<*> {
        Locale.setDefault(tag?.let(Locale::forLanguageTag) ?: systemLocale)
        return LocalLanguageTag.provides(tag)
    }
}
