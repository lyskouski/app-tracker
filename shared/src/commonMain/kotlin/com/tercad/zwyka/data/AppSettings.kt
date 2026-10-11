// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.async.coroutines.awaitAsList
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.infrastructures.Database

data class AppSettings(
    val language: String? = null,
    val weekStart: WeekStart = WeekStart.MONDAY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

private const val LANGUAGE_KEY = "language"
private const val WEEK_START_KEY = "week_start"
private const val THEME_MODE_KEY = "theme_mode"

/** Holds the current settings and persists every change to the database. */
class AppSettingsStore(private val database: Database?) {
    var current by mutableStateOf(AppSettings())
        private set

    /** Set when a language change rebuilds the navigation stack, so the settings page stays open. */
    var reopenSettings = false

    suspend fun load() {
        val values = database?.databaseQueries?.selectAllSettings()
            ?.awaitAsList()
            ?.associate { it.key to it.value_ }
            .orEmpty()
        current = AppSettings(
            language = values[LANGUAGE_KEY],
            weekStart = values[WEEK_START_KEY]
                ?.let { name -> WeekStart.entries.firstOrNull { it.name == name } }
                ?: WeekStart.MONDAY,
            themeMode = values[THEME_MODE_KEY]
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.SYSTEM,
        )
    }

    suspend fun update(settings: AppSettings) {
        current = settings
        val queries = database?.databaseQueries ?: return
        if (settings.language == null) {
            queries.deleteSetting(LANGUAGE_KEY)
        } else {
            queries.upsertSetting(LANGUAGE_KEY, settings.language)
        }
        queries.upsertSetting(WEEK_START_KEY, settings.weekStart.name)
        queries.upsertSetting(THEME_MODE_KEY, settings.themeMode.name)
    }
}
