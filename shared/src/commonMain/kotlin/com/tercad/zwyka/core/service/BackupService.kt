// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.repository.BackupRepository
import kotlinx.datetime.LocalDate

const val BACKUP_EXTENSION = "sq"

/*
 * Export of all stored data into a SQL file, and its import.
 */
class BackupService(
    private val backup: BackupRepository,
    private val reminders: ReminderService,
) {
    fun fileName(date: LocalDate): String = "zwyka-$date.$BACKUP_EXTENSION"

    suspend fun export(): String = backup.export()

    /** Adds the data of [sql] to the stored one. @return number of added rows. */
    suspend fun import(sql: String): Int {
        val added = backup.import(sql)
        reminders.sync()
        return added
    }
}
