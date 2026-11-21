// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.defines.applicationLanguages
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.language
import com.tercad.zwyka.resources.search_languages
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.widgets.DropdownField

/*
 * Language of the application; a change rebuilds the navigation stack,
 * [onBeforeChange] marks the pages to be reopened (see [AppSettingsStore.reopenSettings]).
 */
@Composable
fun LanguageField(
    settingsStore: AppSettingsStore,
    onBeforeChange: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val languageLabels = applicationLanguages.map { option ->
        option.nativeName ?: option.label?.let { stringResource(it) }.orEmpty()
    }
    DropdownField(
        label = stringResource(Res.string.language),
        options = languageLabels,
        searchLabel = stringResource(Res.string.search_languages),
        selectedIndex = applicationLanguages
            .indexOfFirst { it.tag == settingsStore.current.language }
            .coerceAtLeast(0),
        onSelect = { index ->
            scope.launch {
                onBeforeChange()
                settingsStore.update(
                    settingsStore.current.copy(language = applicationLanguages[index].tag),
                )
            }
        },
    )
}
