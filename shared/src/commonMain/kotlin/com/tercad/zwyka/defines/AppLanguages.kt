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
    AppLanguage(tag = "ar", nativeName = "العربية (AR)"),
    AppLanguage(tag = "az", nativeName = "Azərbaycanca (AZ)"),
    AppLanguage(tag = "be", nativeName = "Беларуская (BE)"),
    AppLanguage(tag = "cs", nativeName = "Čeština (CS)"),
    AppLanguage(tag = "de", nativeName = "Deutsch (DE)"),
    AppLanguage(tag = "en", nativeName = "English (EN-US)"),
    AppLanguage(tag = "es", nativeName = "Español (ES)"),
    AppLanguage(tag = "fa", nativeName = "فارسی (FA)"),
    AppLanguage(tag = "fr", nativeName = "Français (FR)"),
    AppLanguage(tag = "hi", nativeName = "हिन्दी (HI)"),
    AppLanguage(tag = "id", nativeName = "Bahasa Indonesia (ID)"),
    AppLanguage(tag = "it", nativeName = "Italiano (IT)"),
    AppLanguage(tag = "ja", nativeName = "日本語 (JA)"),
    AppLanguage(tag = "ka", nativeName = "ქართული (KA)"),
    AppLanguage(tag = "ko", nativeName = "한국어 (KO)"),
    AppLanguage(tag = "nl", nativeName = "Nederlands (NL)"),
    AppLanguage(tag = "pl", nativeName = "Polski (PL)"),
    AppLanguage(tag = "pt", nativeName = "Português (PT)"),
    AppLanguage(tag = "ro", nativeName = "Română (RO)"),
    AppLanguage(tag = "ru", nativeName = "Русский (RU)"),
    AppLanguage(tag = "tr", nativeName = "Türkçe (TR)"),
    AppLanguage(tag = "uk", nativeName = "Українська (UK)"),
    AppLanguage(tag = "uz", nativeName = "Oʻzbekcha (UZ)"),
    AppLanguage(tag = "zh", nativeName = "简体中文 (ZH-CN)"),
    AppLanguage(tag = "zh-TW", nativeName = "繁體中文 (ZH-TW)"),
)
