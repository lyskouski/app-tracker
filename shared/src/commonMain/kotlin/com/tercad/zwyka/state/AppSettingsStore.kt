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

    /** As [reopenSettings], for the sync page above it (a restored backup may change the language). */
    var reopenSync = false

    suspend fun load() {
        val loaded = service?.load() ?: AppSettings()
        // A restored backup of an earlier version has no accepted terms: they stay accepted, not asked again.
        current = if (current.isTermsAccepted && !loaded.isTermsAccepted) {
            loaded.copy(isTermsAccepted = true).also { service?.save(it) }
        } else {
            loaded
        }
    }

    suspend fun update(settings: AppSettings) {
        current = settings
        service?.save(settings)
    }
}
