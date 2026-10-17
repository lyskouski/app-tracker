// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.tercad.zwyka.core.model.AppSettings
import com.tercad.zwyka.core.service.SettingsService

/** Holds the current settings for the UI and persists every change. */
class AppSettingsStore(private val service: SettingsService?) {
    var current by mutableStateOf(AppSettings())
        private set

    /** Set when a language change rebuilds the navigation stack, so the settings page stays open. */
    var reopenSettings = false

    suspend fun load() {
        current = service?.load() ?: AppSettings()
    }

    suspend fun update(settings: AppSettings) {
        current = settings
        service?.save(settings)
    }
}
