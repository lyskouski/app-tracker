// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tercad.zwyka.data.AppSettingsStore
import com.tercad.zwyka.defines.applicationLanguages
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.weekStartOptions
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.language
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.resources.start_of_week
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class SettingsPage(
    private val settingsStore: AppSettingsStore,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val settings = settingsStore.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentColorScheme().surface)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                        tint = currentColorScheme().onSurface,
                    )
                }
                Text(
                    text = stringResource(Res.string.settings),
                    style = MaterialTheme.typography.titleLarge,
                    color = currentColorScheme().onSurface,
                )
            }

            Text(
                text = stringResource(Res.string.language),
                style = MaterialTheme.typography.titleMedium,
                color = currentColorScheme().onSurface,
            )
            Column(Modifier.selectableGroup()) {
                applicationLanguages.forEach { option ->
                    OptionRow(
                        text = option.nativeName ?: option.label?.let { stringResource(it) }.orEmpty(),
                        selected = settings.language == option.tag,
                        onSelect = {
                            scope.launch {
                                settingsStore.reopenSettings = true
                                settingsStore.update(settings.copy(language = option.tag))
                            }
                        },
                    )
                }
            }

            Text(
                text = stringResource(Res.string.start_of_week),
                style = MaterialTheme.typography.titleMedium,
                color = currentColorScheme().onSurface,
            )
            Column(Modifier.selectableGroup()) {
                weekStartOptions.forEach { option ->
                    OptionRow(
                        text = stringResource(option.label),
                        selected = settings.weekStart == option,
                        onSelect = {
                            scope.launch {
                                settingsStore.update(settings.copy(weekStart = option))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
        Text(text = text, color = currentColorScheme().onSurface)
    }
}
