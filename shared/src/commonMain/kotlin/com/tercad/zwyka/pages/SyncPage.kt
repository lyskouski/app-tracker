// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.files.LocalFileTransfer
import com.tercad.zwyka.core.importing.UnsupportedExportException
import com.tercad.zwyka.core.service.BACKUP_EXTENSION
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.backup_created
import com.tercad.zwyka.resources.backup_restored
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.create_backup
import com.tercad.zwyka.resources.data_transfer_failed
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.import_completed
import com.tercad.zwyka.resources.import_file
import com.tercad.zwyka.resources.import_from_app
import com.tercad.zwyka.resources.import_from_app_hint
import com.tercad.zwyka.resources.import_unsupported_format
import com.tercad.zwyka.resources.restore_backup
import com.tercad.zwyka.resources.restore_backup_confirmation
import com.tercad.zwyka.resources.restore_backup_warning
import com.tercad.zwyka.resources.sync
import com.tercad.zwyka.state.AppSettingsStore
import com.tercad.zwyka.widgets.BottomBar
import com.tercad.zwyka.widgets.BottomBarBackButton
import com.tercad.zwyka.widgets.ErrorMessage
import com.tercad.zwyka.widgets.Heading
import com.tercad.zwyka.widgets.StatusMessage
import com.tercad.zwyka.widgets.popupShortcuts
import com.tercad.zwyka.widgets.screenTitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/*
 * Backup of all data into a file, the restore from it (replaces the stored data),
 * and the import of the habits exported by other apps (adds to the stored data).
 */
class SyncPage(
    private val settingsStore: AppSettingsStore,
    private val services: Services,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val fileTransfer = LocalFileTransfer.current
        val scope = rememberCoroutineScope()
        val backup = services.backup
        // Outcome of the last action.
        var message by remember { mutableStateOf<Outcome?>(null) }
        var confirmingRestore by remember { mutableStateOf(false) }
        var isBusy by remember { mutableStateOf(false) }
        val unknownError = stringResource(Res.string.database_error_unknown)

        fun transfer(action: suspend () -> Outcome?) {
            scope.launch {
                isBusy = true
                message = try {
                    action()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: UnsupportedExportException) {
                    Outcome(Res.string.import_unsupported_format, isError = true)
                } catch (error: Exception) {
                    Outcome(Res.string.data_transfer_failed, listOf(error.message ?: unknownError), isError = true)
                } finally {
                    isBusy = false
                }
            }
        }

        fun restore() {
            confirmingRestore = false
            transfer {
                val sql = fileTransfer.open(listOf(BACKUP_EXTENSION))?.decodeToString() ?: return@transfer null
                val restored = backup.restore(sql)
                val language = settingsStore.current.language
                settingsStore.load()
                // The restored language rebuilds the navigation: this page stays open.
                if (settingsStore.current.language != language) {
                    settingsStore.reopenSettings = true
                    settingsStore.reopenSync = true
                }
                Outcome(Res.string.backup_restored, listOf(restored.toString()))
            }
        }

        fun importFromApp() {
            transfer {
                val content = fileTransfer.open(IMPORT_EXTENSIONS) ?: return@transfer null
                val summary = backup.importFromApp(content)
                Outcome(Res.string.import_completed, listOf(summary.habits.toString(), summary.marks.toString()))
            }
        }

        Scaffold(
            modifier = Modifier.screenTitle(stringResource(Res.string.sync)),
            containerColor = currentColorScheme().surface,
            bottomBar = {
                BottomBar {
                    BottomBarBackButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
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
                Heading(stringResource(Res.string.sync))

                message?.let { outcome ->
                    val text = stringResource(outcome.text, *outcome.arguments.toTypedArray())
                    if (outcome.isError) ErrorMessage(text) else StatusMessage(text)
                }

                OutlinedButton(
                    onClick = {
                        transfer {
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val saved = fileTransfer.save(backup.fileName(today), backup.export())
                            if (saved) Outcome(Res.string.backup_created) else null
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                    Text(text = stringResource(Res.string.create_backup), modifier = Modifier.padding(start = 8.dp))
                }

                OutlinedButton(
                    onClick = { confirmingRestore = true },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                    Text(text = stringResource(Res.string.restore_backup), modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    text = stringResource(Res.string.restore_backup_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = currentColorScheme().onSurfaceVariant,
                )

                Heading(
                    text = stringResource(Res.string.import_from_app),
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.import_from_app_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = currentColorScheme().onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { importFromApp() },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
                    Text(text = stringResource(Res.string.import_file), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (confirmingRestore) {
            AlertDialog(
                onDismissRequest = { confirmingRestore = false },
                modifier = Modifier.popupShortcuts(onEnter = { restore() }),
                title = { Text(stringResource(Res.string.restore_backup_confirmation)) },
                text = { Text(stringResource(Res.string.restore_backup_warning)) },
                confirmButton = {
                    TextButton(
                        onClick = { restore() },
                        colors = ButtonDefaults.textButtonColors(contentColor = currentColorScheme().error),
                    ) {
                        Text(stringResource(Res.string.restore_backup))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmingRestore = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
    }
}

/** Message of the last action, with the arguments of its [text]. */
private data class Outcome(
    val text: StringResource,
    val arguments: List<String> = emptyList(),
    val isError: Boolean = false,
)

/** Extensions of the exports of other apps (see [com.tercad.zwyka.core.importing.parseHabitExport]). */
private val IMPORT_EXTENSIONS = listOf("db", "zip", "json", "csv")
