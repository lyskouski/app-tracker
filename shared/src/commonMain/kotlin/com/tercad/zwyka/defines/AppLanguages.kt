// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.language_system

/** [tag] is a BCP 47 language tag; null follows the system language. */
data class AppLanguage(
    val tag: String?,
    val nativeName: String? = null,
    val label: StringResource? = null,
)

val applicationLanguages = listOf(
    AppLanguage(tag = null, label = Res.string.language_system),
    AppLanguage(tag = "en", nativeName = "English"),
    AppLanguage(tag = "de", nativeName = "Deutsch"),
)
