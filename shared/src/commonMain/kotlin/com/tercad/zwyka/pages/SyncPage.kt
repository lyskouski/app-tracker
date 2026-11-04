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
import com.tercad.zwyka.core.service.BACKUP_EXTENSION
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.backup_created
import com.tercad.zwyka.resources.backup_restored
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.create_backup
import com.tercad.zwyka.resources.data_transfer_failed
import com.tercad.zwyka.resources.database_error_unknown
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
 * Backup of all data into a file, and the restore from it (replaces the stored data).
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
        // Outcome of the last action: a message with its argument.
        var message by remember { mutableStateOf<Pair<StringResource, String>?>(null) }
        var confirmingRestore by remember { mutableStateOf(false) }
        var isBusy by remember { mutableStateOf(false) }
        val unknownError = stringResource(Res.string.database_error_unknown)

        fun transfer(action: suspend () -> Pair<StringResource, String>?) {
            scope.launch {
                isBusy = true
                message = try {
                    action()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Res.string.data_transfer_failed to (error.message ?: unknownError)
                } finally {
                    isBusy = false
                }
            }
        }

        fun restore() {
            confirmingRestore = false
            transfer {
                val sql = fileTransfer.open(BACKUP_EXTENSION) ?: return@transfer null
                val restored = backup.restore(sql)
                val language = settingsStore.current.language
                settingsStore.load()
                // The restored language rebuilds the navigation: this page stays open.
                if (settingsStore.current.language != language) {
                    settingsStore.reopenSettings = true
                    settingsStore.reopenSync = true
                }
                Res.string.backup_restored to restored.toString()
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

                message?.let { (text, argument) ->
                    if (text == Res.string.data_transfer_failed) {
                        ErrorMessage(stringResource(text, argument))
                    } else {
                        StatusMessage(stringResource(text, argument))
                    }
                }

                OutlinedButton(
                    onClick = {
                        transfer {
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val saved = fileTransfer.save(backup.fileName(today), backup.export())
                            if (saved) Res.string.backup_created to "" else null
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
