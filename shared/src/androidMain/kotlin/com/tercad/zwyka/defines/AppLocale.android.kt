// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    @Composable
    actual infix fun provides(tag: String?): ProvidedValue<*> {
        val context = LocalContext.current
        val configuration = Configuration(LocalConfiguration.current)
        val locale = tag?.let(Locale::forLanguageTag) ?: systemLocale(context)
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        val resources = context.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }

    // Read on each call (not cached): the device or per-app language may change while the process lives,
    // and the activity resources and Locale.getDefault() hold the overridden language.
    private fun systemLocale(context: Context): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val appLocales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!appLocales.isEmpty) return appLocales[0]
        }
        return Resources.getSystem().configuration.locales[0]
    }
}
