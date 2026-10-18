// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne

import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.createServices
import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.Intent
import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.core.repository.BackupRepository
import com.tercad.zwyka.core.repository.DayStateRepository
import com.tercad.zwyka.core.repository.HabitRepository
import com.tercad.zwyka.core.repository.HabitTimeRepository
import com.tercad.zwyka.core.repository.IntentRepository
import com.tercad.zwyka.core.repository.SettingsRepository

/*
 * SQLDelight storage of the business logic.
 */

class SqlHabitRepository(private val database: Database) : HabitRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getAll(): List<Habit> =
        queries.selectAllHabits { id, title, icon, color, remarks, createdAt, updatedAt, activeDays ->
            Habit(id, title, icon, color, remarks, createdAt, updatedAt, activeDays.toInt())
        }.awaitAsList()

    override suspend fun insert(draft: HabitDraft): Long {
        queries.insertHabit(draft.title, draft.icon, draft.color, draft.remarks, draft.activeDays.toLong())
        return queries.selectLastHabitId().awaitAsOne().max ?: error("Habit was not created")
    }

    override suspend fun update(id: Long, draft: HabitDraft) {
        queries.updateHabit(draft.title, draft.icon, draft.color, draft.remarks, draft.activeDays.toLong(), id)
    }

    override suspend fun updateActiveDays(id: Long, activeDays: Int) {
        queries.updateHabitActiveDays(activeDays.toLong(), id)
    }

    override suspend fun touch(id: Long) {
        queries.touchHabit(id)
    }

    override suspend fun delete(id: Long) {
        database.transaction {
            queries.deleteDayStatesForHabit(id)
            queries.deleteHabitTimesForHabit(id)
            queries.deleteHabit(id)
        }
    }
}

class SqlDayStateRepository(private val database: Database) : DayStateRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getRange(from: String, to: String): List<DayState> =
        queries.selectDayStatesForDateRange(from, to) { habitId, date, mark ->
            DayState(habitId, date, mark)
        }.awaitAsList()

    override suspend fun upsert(state: DayState) {
        queries.upsertDayState(state.habitId, state.date, state.mark)
    }

    override suspend fun delete(habitId: Long, date: String) {
        queries.deleteDayState(habitId, date)
    }
}

class SqlHabitTimeRepository(private val database: Database) : HabitTimeRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getAll(): List<HabitTime> =
        queries.selectAllHabitTimes { id, habitId, intentId, timeStart, timeEnd, note, activeDays, notify ->
            HabitTime(
                id = id,
                habitId = habitId,
                intentId = intentId,
                timeStart = timeStart.toInt(),
                timeEnd = timeEnd.toInt(),
                note = note,
                activeDays = activeDays.toInt(),
                notify = notify != 0L,
            )
        }.awaitAsList()

    override suspend fun insert(habitTime: HabitTime) {
        with(habitTime) {
            queries.insertHabitTime(
                habitId,
                intentId,
                timeStart.toLong(),
                timeEnd.toLong(),
                note,
                activeDays.toLong(),
                if (notify) 1L else 0L,
            )
        }
    }

    override suspend fun update(habitTime: HabitTime) {
        with(habitTime) {
            queries.updateHabitTime(
                habitId,
                intentId,
                timeStart.toLong(),
                timeEnd.toLong(),
                note,
                activeDays.toLong(),
                if (notify) 1L else 0L,
                id,
            )
        }
    }

    override suspend fun delete(id: Long) {
        queries.deleteHabitTime(id)
    }
}

class SqlIntentRepository(private val database: Database) : IntentRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getAll(): List<Intent> =
        queries.selectAllIntents { id, title, icon, color, remarks, createdAt, updatedAt, activeDays ->
            Intent(id, title, icon, color, remarks, createdAt, updatedAt, activeDays.toInt())
        }.awaitAsList()

    override suspend fun insert(title: String, activeDays: Int): Long {
        queries.insertIntent(title, activeDays.toLong())
        return queries.selectLastIntentId().awaitAsOne().max ?: error("Intent was not created")
    }

    override suspend fun updateTitle(id: Long, title: String) {
        queries.updateIntentTitle(title, id)
    }

    override suspend fun updateActiveDays(id: Long, activeDays: Int) {
        queries.updateIntentActiveDays(activeDays.toLong(), id)
    }

    override suspend fun deleteUnused() {
        queries.deleteUnusedIntents()
    }
}

class SqlSettingsRepository(private val database: Database) : SettingsRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getAll(): Map<String, String> =
        queries.selectAllSettings { key, value -> key to value }.awaitAsList().toMap()

    override suspend fun put(key: String, value: String) {
        queries.upsertSetting(key, value)
    }

    override suspend fun remove(key: String) {
        queries.deleteSetting(key)
    }
}

class SqlBackupRepository(private val database: Database) : BackupRepository {
    private val queries get() = database.databaseQueries

    // Parents go first, so the imported rows refer to existing ones.
    override suspend fun export(): String = buildList {
        queries.selectAllHabits().awaitAsList().forEach {
            add(
                sqlInsert(
                    "Habit",
                    listOf(
                        "id" to it.id, "title" to it.title, "icon" to it.icon, "color" to it.color,
                        "remarks" to it.remarks, "createdAt" to it.createdAt, "updatedAt" to it.updatedAt,
                        "activeDays" to it.activeDays,
                    ),
                ),
            )
        }
        queries.selectAllIntents().awaitAsList().forEach {
            add(
                sqlInsert(
                    "Intent",
                    listOf(
                        "id" to it.id, "title" to it.title, "icon" to it.icon, "color" to it.color,
                        "remarks" to it.remarks, "createdAt" to it.createdAt, "updatedAt" to it.updatedAt,
                        "activeDays" to it.activeDays,
                    ),
                ),
            )
        }
        queries.selectAllHabitTimesById().awaitAsList().forEach {
            add(
                sqlInsert(
                    "HabitTime",
                    listOf(
                        "id" to it.id, "habitId" to it.habitId, "intentId" to it.intentId,
                        "timeStart" to it.timeStart, "timeEnd" to it.timeEnd, "note" to it.note,
                        "activeDays" to it.activeDays, "notify" to it.notify,
                    ),
                ),
            )
        }
        queries.selectAllDayStates().awaitAsList().forEach {
            add(sqlInsert("DayState", listOf("habitId" to it.habitId, "date" to it.date, "mark" to it.mark)))
        }
        queries.selectAllSettings().awaitAsList().forEach {
            add(sqlInsert("Setting", listOf("key" to it.key, "value" to it.value_)))
        }
    }.joinToString(separator = "\n", postfix = "\n")

    override suspend fun import(sql: String): Int {
        // Parsed in full first: a broken file adds nothing.
        val inserts = parseSqlDump(sql)
        var added = 0L
        database.transaction {
            inserts.forEach { insert ->
                added += insert.execute()
            }
        }
        return added.toInt()
    }

    private suspend fun SqlInsert.execute(): Long = when (table) {
        "Habit" -> queries.importHabit(
            long("id"), string("title"), stringOrNull("icon"), stringOrNull("color"), stringOrNull("remarks"),
            long("createdAt", 0), long("updatedAt", 0), long("activeDays", ALL_DAYS.toLong()),
        )
        "Intent" -> queries.importIntent(
            long("id"), string("title"), stringOrNull("icon"), stringOrNull("color"), stringOrNull("remarks"),
            long("createdAt", 0), long("updatedAt", 0), long("activeDays", ALL_DAYS.toLong()),
        )
        "HabitTime" -> queries.importHabitTime(
            long("id"), longOrNull("habitId"), longOrNull("intentId"), long("timeStart"), long("timeEnd"),
            stringOrNull("note"), long("activeDays", ALL_DAYS.toLong()), long("notify", 0),
        )
        "DayState" -> queries.importDayState(long("habitId"), string("date"), string("mark"))
        "Setting" -> queries.importSetting(string("key"), string("value"))
        else -> throw IllegalArgumentException("Unknown table: $table")
    }

    private fun SqlInsert.longOrNull(column: String): Long? = values[column]?.let {
        it as? Long ?: throw IllegalArgumentException("$table.$column is not a number")
    }

    private fun SqlInsert.long(column: String, default: Long? = null): Long =
        longOrNull(column) ?: default ?: throw IllegalArgumentException("$table.$column is missing")

    private fun SqlInsert.stringOrNull(column: String): String? = values[column]?.let {
        it as? String ?: throw IllegalArgumentException("$table.$column is not a text")
    }

    private fun SqlInsert.string(column: String): String =
        stringOrNull(column) ?: throw IllegalArgumentException("$table.$column is missing")
}

/** Composition root: business services stored in [Database]. */
fun Database.createServices(reminderScheduler: ReminderScheduler): Services =
    createServices(
        habitRepository = SqlHabitRepository(this),
        dayStateRepository = SqlDayStateRepository(this),
        habitTimeRepository = SqlHabitTimeRepository(this),
        intentRepository = SqlIntentRepository(this),
        settingsRepository = SqlSettingsRepository(this),
        backupRepository = SqlBackupRepository(this),
        reminderScheduler = reminderScheduler,
    )
