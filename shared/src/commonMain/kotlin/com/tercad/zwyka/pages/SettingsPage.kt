// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tercad.zwyka.defines.applicationLanguages
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.dateFormatOptions
import com.tercad.zwyka.defines.habitOrderByOptions
import com.tercad.zwyka.defines.sortDirectionOptions
import com.tercad.zwyka.defines.themeModeOptions
import com.tercad.zwyka.defines.weekStartOptions
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.date_format
import com.tercad.zwyka.resources.habit_order_by
import com.tercad.zwyka.resources.language
import com.tercad.zwyka.resources.months_short
import com.tercad.zwyka.resources.search_languages
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.resources.sort_direction
import com.tercad.zwyka.resources.start_of_week
import com.tercad.zwyka.resources.theme_mode
import com.tercad.zwyka.state.AppSettingsStore
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.OutlinedButton
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.files.LocalFileTransfer
import com.tercad.zwyka.core.service.BACKUP_EXTENSION
import com.tercad.zwyka.core.service.BackupService
import com.tercad.zwyka.resources.data_exported
import com.tercad.zwyka.resources.data_imported
import com.tercad.zwyka.resources.data_transfer_failed
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.export_data
import com.tercad.zwyka.resources.import_data
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.StringResource
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
            containerColor = currentColorScheme().surface,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(currentColorScheme().primary),
                ) {
                    TextButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(Res.string.back),
                            modifier = Modifier.padding(start = 8.dp),
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
            Text(
                text = stringResource(Res.string.settings),
                style = MaterialTheme.typography.titleLarge,
                color = currentColorScheme().onSurface,
            )

            val languageLabels = applicationLanguages.map { option ->
                option.nativeName ?: option.label?.let { stringResource(it) }.orEmpty()
            }
            DropdownField(
                label = stringResource(Res.string.language),
                options = languageLabels,
                searchable = true,
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

            services?.let { BackupButtons(it.backup, settingsStore) }
        }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    searchable: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var fieldWidthPx by remember { mutableStateOf(0) }
    val fieldWidth = with(LocalDensity.current) { fieldWidthPx.toDp() }
    Box(Modifier.fillMaxWidth().onSizeChanged { fieldWidthPx = it.width }) {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        // Transparent overlay: a read-only text field does not forward clicks.
        Box(
            Modifier
                .matchParentSize()
                .clickable {
                    query = ""
                    expanded = true
                },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(fieldWidth),
        ) {
            if (searchable) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(Res.string.search_languages)) },
                    singleLine = true,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                )
            }
            options.forEachIndexed { index, text ->
                if (searchable && !text.contains(query.trim(), ignoreCase = true)) return@forEachIndexed
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelect(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun BackupButtons(backup: BackupService, settingsStore: AppSettingsStore) {
    val fileTransfer = LocalFileTransfer.current
    val scope = rememberCoroutineScope()
    // Outcome of the last action: a message with its argument.
    var message by remember { mutableStateOf<Pair<StringResource, String>?>(null) }
    val unknownError = stringResource(Res.string.database_error_unknown)

    fun transfer(action: suspend () -> Pair<StringResource, String>?) {
        scope.launch {
            message = try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Res.string.data_transfer_failed to (error.message ?: unknownError)
            }
        }
    }

    message?.let { (text, argument) ->
        Text(
            text = stringResource(text, argument),
            style = MaterialTheme.typography.bodyMedium,
            color = currentColorScheme().onSurface,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = {
                transfer {
                    val sql = fileTransfer.open(BACKUP_EXTENSION) ?: return@transfer null
                    val added = backup.import(sql)
                    // Imported settings may change the language, which rebuilds the navigation.
                    settingsStore.reopenSettings = true
                    settingsStore.load()
                    Res.string.data_imported to added.toString()
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
            Text(text = stringResource(Res.string.import_data), modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(
            onClick = {
                transfer {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val saved = fileTransfer.save(backup.fileName(today), backup.export())
                    if (saved) Res.string.data_exported to "" else null
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            Icon(imageVector = Icons.Filled.FileUpload, contentDescription = null)
            Text(text = stringResource(Res.string.export_data), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
