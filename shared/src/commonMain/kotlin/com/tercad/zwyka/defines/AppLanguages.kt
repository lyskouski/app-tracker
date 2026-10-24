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
    AppLanguage(tag = "be", nativeName = "Беларуская"),
    AppLanguage(tag = "pl", nativeName = "Polski"),
    AppLanguage(tag = "pt", nativeName = "Português"),
    AppLanguage(tag = "fr", nativeName = "Français"),
    AppLanguage(tag = "uk", nativeName = "Українська"),
    AppLanguage(tag = "zh", nativeName = "简体中文"),
    AppLanguage(tag = "zh-TW", nativeName = "繁體中文"),
    AppLanguage(tag = "hi", nativeName = "हिन्दी"),
    AppLanguage(tag = "uz", nativeName = "Oʻzbekcha"),
    AppLanguage(tag = "fa", nativeName = "فارسی"),
    AppLanguage(tag = "ar", nativeName = "العربية"),
    AppLanguage(tag = "az", nativeName = "Azərbaycanca"),
    AppLanguage(tag = "ja", nativeName = "日本語"),
    AppLanguage(tag = "es", nativeName = "Español"),
    AppLanguage(tag = "it", nativeName = "Italiano"),
    AppLanguage(tag = "tr", nativeName = "Türkçe"),
    AppLanguage(tag = "ko", nativeName = "한국어"),
    AppLanguage(tag = "nl", nativeName = "Nederlands"),
    AppLanguage(tag = "cs", nativeName = "Čeština"),
    AppLanguage(tag = "ro", nativeName = "Română"),
    AppLanguage(tag = "ka", nativeName = "ქართული"),
    AppLanguage(tag = "ru", nativeName = "Русский"),
    AppLanguage(tag = "id", nativeName = "Bahasa Indonesia"),
)
