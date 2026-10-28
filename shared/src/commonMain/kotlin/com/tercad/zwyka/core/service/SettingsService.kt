// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.model.AppSettings
import com.tercad.zwyka.core.repository.SettingsRepository

private const val LANGUAGE_KEY = "language"
private const val WEEK_START_KEY = "week_start"
private const val THEME_MODE_KEY = "theme_mode"
private const val DATE_FORMAT_KEY = "date_format"
private const val HABIT_ORDER_BY_KEY = "habit_order_by"
private const val SORT_DIRECTION_KEY = "sort_direction"
private const val COMPLETENESS_TYPE_KEY = "completeness_type"
private const val HOME_VIEW_KEY = "home_view"

/*
 * Maps [AppSettings] to stored key / value pairs.
 */
class SettingsService(
    private val settings: SettingsRepository,
) {
    suspend fun load(): AppSettings {
        val values = settings.getAll()
        val defaults = AppSettings()
        return AppSettings(
            language = values[LANGUAGE_KEY],
            weekStart = values[WEEK_START_KEY].toEnum(defaults.weekStart),
            themeMode = values[THEME_MODE_KEY].toEnum(defaults.themeMode),
            dateFormat = values[DATE_FORMAT_KEY].toEnum(defaults.dateFormat),
            habitOrderBy = values[HABIT_ORDER_BY_KEY].toEnum(defaults.habitOrderBy),
            sortDirection = values[SORT_DIRECTION_KEY].toEnum(defaults.sortDirection),
            completenessType = values[COMPLETENESS_TYPE_KEY].toEnum(defaults.completenessType),
            homeView = values[HOME_VIEW_KEY].toEnum(defaults.homeView),
        )
    }

    suspend fun save(value: AppSettings) {
        val language = value.language
        if (language == null) settings.remove(LANGUAGE_KEY) else settings.put(LANGUAGE_KEY, language)
        settings.put(WEEK_START_KEY, value.weekStart.name)
        settings.put(THEME_MODE_KEY, value.themeMode.name)
        settings.put(DATE_FORMAT_KEY, value.dateFormat.name)
        settings.put(HABIT_ORDER_BY_KEY, value.habitOrderBy.name)
        settings.put(SORT_DIRECTION_KEY, value.sortDirection.name)
        settings.put(COMPLETENESS_TYPE_KEY, value.completenessType.name)
        settings.put(HOME_VIEW_KEY, value.homeView.name)
    }
}

private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

