// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.importing.ImportSummary
import com.tercad.zwyka.core.importing.parseHabitExport
import com.tercad.zwyka.core.repository.BackupRepository
import kotlinx.datetime.LocalDate

const val BACKUP_EXTENSION = "sq"

/*
 * Backup of all stored data into a SQL file, and the restore from it.
 */
class BackupService(
    private val backup: BackupRepository,
    private val reminders: ReminderService,
) {
    fun fileName(date: LocalDate): String = "zwyka-$date.$BACKUP_EXTENSION"

    suspend fun export(): String = backup.export()

    /** Adds the habits exported by another app (see [parseHabitExport]). */
    suspend fun importFromApp(bytes: ByteArray): ImportSummary {
        val summary = backup.add(parseHabitExport(bytes))
        reminders.sync()
        return summary
    }

    /** Replaces the stored data with the one of [sql]. @return number of restored rows. */
    suspend fun restore(sql: String): Int {
        val restored = backup.restore(sql)
        reminders.sync()
        return restored
    }
}
