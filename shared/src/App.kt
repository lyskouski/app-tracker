package com.tercad.zwyka

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.defines.ColorSchemeLight
import com.tercad.zwyka.defines.ColorSchemeDark
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.resources.app_name
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.compose_multiplatform

@Composable
@Preview
@DevelopmentEntryPoint
fun App(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
) {
    val systemDark = isSystemInDarkTheme()
    
    MaterialTheme (
        colorScheme = when (themeMode) {
            ThemeMode.LIGHT -> ColorSchemeLight
            ThemeMode.DARK -> ColorSchemeDark
            ThemeMode.SYSTEM -> if (systemDark) ColorSchemeDark else ColorSchemeLight
        },
    ) {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text(stringResource(Res.string.app_name))
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}
