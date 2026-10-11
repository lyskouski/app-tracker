// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalLanguageTag = staticCompositionLocalOf<String?> { null }

private fun overrideNavigatorLanguage(tag: String): Unit = js(
    """{
        Object.defineProperty(navigator, 'language', { get: () => tag, configurable: true });
        Object.defineProperty(navigator, 'languages', { get: () => [tag], configurable: true });
    }""",
)

private fun restoreNavigatorLanguage(): Unit = js(
    """{
        delete navigator.language;
        delete navigator.languages;
    }""",
)

actual object LocalAppLocale {
    @Composable
    actual infix fun provides(tag: String?): ProvidedValue<*> {
        if (tag == null) restoreNavigatorLanguage() else overrideNavigatorLanguage(tag)
        return LocalLanguageTag.provides(tag)
    }
}
