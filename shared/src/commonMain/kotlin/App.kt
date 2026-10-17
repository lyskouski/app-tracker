// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.CancellationException
import com.tercad.zwyka.defines.LocalAppLocale
import com.tercad.zwyka.state.AppSettingsStore

import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.createServices
import com.tercad.zwyka.defines.ColorSchemeDark
import com.tercad.zwyka.defines.ColorSchemeLight
import com.tercad.zwyka.defines.LocalColorScheme
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.pages.HomePage
import com.tercad.zwyka.pages.SettingsPage

@Composable
fun App(
    database: Database? = null,
    initializeDatabase: suspend () -> Unit = {},
    reminderScheduler: ReminderScheduler = NoReminderScheduler,
) {
    val systemDark = isSystemInDarkTheme()

    val services = remember(database, reminderScheduler) {
        database?.createServices(reminderScheduler)
    }
    val settingsStore = remember(services) { AppSettingsStore(services?.settings) }
    val colorScheme = when (settingsStore.current.themeMode) {
        ThemeMode.LIGHT -> ColorSchemeLight
        ThemeMode.DARK -> ColorSchemeDark
        ThemeMode.SYSTEM -> if (systemDark) ColorSchemeDark else ColorSchemeLight
    }
    var settingsLoaded by remember(services) { mutableStateOf(false) }

    LaunchedEffect(settingsStore) {
        try {
            initializeDatabase()
            settingsStore.load()
            services?.reminders?.sync()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Defaults are used; the home page reports database failures.
        }
        settingsLoaded = true
    }

    if (!settingsLoaded) return

    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalAppLocale provides settingsStore.current.language,
    ) {
        key(settingsStore.current.language) {
            MaterialTheme(colorScheme = colorScheme) {
                val screens = remember {
                    val home = HomePage(services, initializeDatabase, settingsStore)
                    if (settingsStore.reopenSettings) {
                        listOf(home, SettingsPage(settingsStore))
                    } else {
                        listOf(home)
                    }
                }
                Navigator(screens = screens)
            }
        }
    }
}
