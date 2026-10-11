package com.tercad.zwyka

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

import com.tercad.zwyka.defines.ColorSchemeLight
import com.tercad.zwyka.defines.ColorSchemeDark
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.pages.HomePage

@Composable
fun App(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    database: Database? = null,
    initializeDatabase: suspend () -> Unit = {},
) {
    val systemDark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = when (themeMode) {
            ThemeMode.LIGHT -> ColorSchemeLight
            ThemeMode.DARK -> ColorSchemeDark
            ThemeMode.SYSTEM -> if (systemDark) ColorSchemeDark else ColorSchemeLight
        },
    ) {
        Navigator(screen = HomePage(database, initializeDatabase))
    }
}
