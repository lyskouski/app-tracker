// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tercad.zwyka.defines.applicationLanguages
import com.tercad.zwyka.defines.completenessTypeOptions
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.dateFormatOptions
import com.tercad.zwyka.defines.habitOrderByOptions
import com.tercad.zwyka.defines.sortDirectionOptions
import com.tercad.zwyka.defines.themeModeOptions
import com.tercad.zwyka.defines.weekStartOptions
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.completeness_type
import com.tercad.zwyka.resources.date_format
import com.tercad.zwyka.resources.habit_order_by
import com.tercad.zwyka.resources.language
import com.tercad.zwyka.resources.months_short
import com.tercad.zwyka.resources.search_languages
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.resources.sort_direction
import com.tercad.zwyka.resources.sync
import com.tercad.zwyka.resources.start_of_week
import com.tercad.zwyka.resources.theme_mode
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.widgets.BottomBar
import com.tercad.zwyka.widgets.BottomBarBackButton
import com.tercad.zwyka.widgets.BottomBarButton
import com.tercad.zwyka.widgets.DropdownField
import com.tercad.zwyka.widgets.Heading
import com.tercad.zwyka.widgets.screenTitle
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

class SettingsPage(
    private val settingsStore: AppSettingsStore,
    private val services: Services? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val settings = settingsStore.current

        Scaffold(
            modifier = Modifier.screenTitle(stringResource(Res.string.settings)),
            containerColor = currentColorScheme().surface,
            bottomBar = {
                BottomBar {
                    BottomBarBackButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                    services?.let {
                        BottomBarButton(
                            icon = Icons.Filled.Sync,
                            text = stringResource(Res.string.sync),
                            onClick = { navigator.push(SyncPage(settingsStore, it)) },
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    }
                }
            },
        ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Heading(stringResource(Res.string.settings))

            val languageLabels = applicationLanguages.map { option ->
                option.nativeName ?: option.label?.let { stringResource(it) }.orEmpty()
            }
            DropdownField(
                label = stringResource(Res.string.language),
                options = languageLabels,
                searchLabel = stringResource(Res.string.search_languages),
                selectedIndex = applicationLanguages
                    .indexOfFirst { it.tag == settings.language }
                    .coerceAtLeast(0),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.reopenSettings = true
                        settingsStore.update(
                            settings.copy(language = applicationLanguages[index].tag),
                        )
                    }
                },
            )

            DropdownField(
                label = stringResource(Res.string.theme_mode),
                options = themeModeOptions.map { stringResource(it.label) },
                selectedIndex = themeModeOptions.indexOf(settings.themeMode),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(themeMode = themeModeOptions[index]))
                    }
                },
            )

            DropdownField(
                label = stringResource(Res.string.start_of_week),
                options = weekStartOptions.map { stringResource(it.label) },
                selectedIndex = weekStartOptions.indexOf(settings.weekStart),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(weekStart = weekStartOptions[index]))
                    }
                },
            )

            val today = remember {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            }
            val monthNames = stringArrayResource(Res.array.months_short)
            DropdownField(
                label = stringResource(Res.string.date_format),
                options = dateFormatOptions.map { it.format(today, monthNames) },
                selectedIndex = dateFormatOptions.indexOf(settings.dateFormat),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(dateFormat = dateFormatOptions[index]))
                    }
                },
            )

            DropdownField(
                label = stringResource(Res.string.habit_order_by),
                options = habitOrderByOptions.map { stringResource(it.label) },
                selectedIndex = habitOrderByOptions.indexOf(settings.habitOrderBy),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(habitOrderBy = habitOrderByOptions[index]))
                    }
                },
            )

            DropdownField(
                label = stringResource(Res.string.sort_direction),
                options = sortDirectionOptions.map { stringResource(it.label) },
                selectedIndex = sortDirectionOptions.indexOf(settings.sortDirection),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(sortDirection = sortDirectionOptions[index]))
                    }
                },
            )

            DropdownField(
                label = stringResource(Res.string.completeness_type),
                options = completenessTypeOptions.map { stringResource(it.label) },
                selectedIndex = completenessTypeOptions.indexOf(settings.completenessType),
                onSelect = { index ->
                    scope.launch {
                        settingsStore.update(settings.copy(completenessType = completenessTypeOptions[index]))
                    }
                },
            )

        }
        }
    }
}

