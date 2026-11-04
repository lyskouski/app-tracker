// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne

import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.createServices
import com.tercad.zwyka.core.importing.ImportSummary
import com.tercad.zwyka.core.importing.ImportedHabit
import com.tercad.zwyka.core.model.ALL_DAYS
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.Goal
import com.tercad.zwyka.core.model.GoalGroup
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.model.HabitTime
import com.tercad.zwyka.core.model.Intent
import com.tercad.zwyka.core.notifications.ReminderScheduler
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock
import com.tercad.zwyka.core.repository.BackupRepository
import com.tercad.zwyka.core.repository.DayStateRepository
import com.tercad.zwyka.core.repository.GoalRepository
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
        queries.selectAllHabits {
                id, title, icon, color, remarks, createdAt, updatedAt, activeDays, isArchived, targetValue, targetType, isNegative ->
            Habit(
                id, title, icon, color, remarks, createdAt, updatedAt, activeDays.toInt(), isArchived != 0L,
                targetValue, targetType, isNegative != 0L,
            )
        }.awaitAsList()

    override suspend fun insert(draft: HabitDraft): Long {
        queries.insertHabit(
            draft.title, draft.icon, draft.color, draft.remarks, draft.activeDays.toLong(),
            if (draft.isArchived) 1L else 0L, draft.targetValue, draft.targetType,
            if (draft.isNegative) 1L else 0L,
        )
        return queries.selectLastHabitId().awaitAsOne().max ?: error("Habit was not created")
    }

    override suspend fun update(id: Long, draft: HabitDraft) {
        queries.updateHabit(
            draft.title, draft.icon, draft.color, draft.remarks, draft.activeDays.toLong(),
            if (draft.isArchived) 1L else 0L, draft.targetValue, draft.targetType, id,
        )
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
            queries.deleteGoalsForHabit(id)
            queries.deleteGoalGroupsForHabit(id)
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

    override suspend fun getForHabit(habitId: Long): List<DayState> =
        queries.selectDayStatesForHabit(habitId) { id, date, mark ->
            DayState(id, date, mark)
        }.awaitAsList()

    override suspend fun upsert(state: DayState) {
        queries.upsertDayState(state.habitId, state.date, state.mark)
    }

    override suspend fun delete(habitId: Long, date: String) {
        queries.deleteDayState(habitId, date)
    }
}

class SqlGoalRepository(private val database: Database) : GoalRepository {
    private val queries get() = database.databaseQueries

    override suspend fun getGroups(habitId: Long): List<GoalGroup> =
        queries.selectGoalGroupsForHabit(habitId) { id, groupHabitId, title, position ->
            GoalGroup(id, groupHabitId, title, position.toInt())
        }.awaitAsList()

    override suspend fun getGoals(habitId: Long): List<Goal> =
        queries.selectGoalsForHabit(habitId) { id, groupId, title, description, isFinished, createdAt ->
            Goal(id, groupId, title, description, isFinished != 0L, createdAt)
        }.awaitAsList()

    override suspend fun insertGroup(habitId: Long, title: String, position: Int): Long {
        queries.insertGoalGroup(habitId, title, position.toLong())
        return queries.selectLastGoalGroupId().awaitAsOne().max ?: error("Goal group was not created")
    }

    override suspend fun updatePositions(groupIds: List<Long>) {
        database.transaction {
            groupIds.forEachIndexed { position, id ->
                queries.updateGoalGroupPosition(position.toLong(), id)
            }
        }
    }

    override suspend fun insert(groupId: Long, title: String, description: String?) {
        queries.insertGoal(groupId, title, description)
    }

    override suspend fun updateFinished(id: Long, isFinished: Boolean) {
        queries.updateGoalFinished(if (isFinished) 1L else 0L, id)
    }

    override suspend fun update(id: Long, groupId: Long, title: String, description: String?) {
        queries.updateGoal(groupId, title, description, id)
    }

    override suspend fun deleteGroup(groupId: Long) {
        database.transaction {
            queries.deleteGoalsForGroup(groupId)
            queries.deleteGoalGroup(groupId)
        }
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
                        "activeDays" to it.activeDays, "isArchived" to it.isArchived,
                        "targetValue" to it.targetValue, "targetType" to it.targetType,
                        "isNegative" to it.isNegative,
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
        queries.selectAllGoalGroups().awaitAsList().forEach {
            add(
                sqlInsert(
                    "GoalGroup",
                    listOf("id" to it.id, "habitId" to it.habitId, "title" to it.title, "position" to it.position),
                ),
            )
        }
        queries.selectAllGoals().awaitAsList().forEach {
            add(
                sqlInsert(
                    "Goal",
                    listOf(
                        "id" to it.id, "groupId" to it.groupId, "title" to it.title,
                        "description" to it.description, "isFinished" to it.isFinished, "createdAt" to it.createdAt,
                    ),
                ),
            )
        }
        queries.selectAllSettings().awaitAsList().forEach {
            add(sqlInsert("Setting", listOf("key" to it.key, "value" to it.value_)))
        }
    }.joinToString(separator = "\n", postfix = "\n")

    override suspend fun restore(sql: String): Int {
        // Parsed in full first: a broken or empty file keeps the stored data.
        val inserts = parseSqlDump(sql)
        require(inserts.isNotEmpty()) { "The backup has no data" }
        var restored = 0L
        database.transaction {
            clear()
            inserts.forEach { insert ->
                restored += insert.execute()
            }
        }
        return restored.toInt()
    }

    override suspend fun add(habits: List<ImportedHabit>): ImportSummary {
        var created = 0
        var marks = 0L
        database.transaction {
            val existing = queries.selectAllHabits().awaitAsList()
            habits.filter { it.draft.title.isNotBlank() }.forEach { habit ->
                val draft = habit.draft
                val id = existing.firstOrNull {
                    it.title.trim().equals(draft.title.trim(), ignoreCase = true) &&
                        (it.targetValue != null) == (draft.targetValue != null) &&
                        (it.isNegative != 0L) == draft.isNegative
                }?.id ?: run {
                    val createdAt = habit.createdAt.takeIf { it > 0 }
                        ?: habit.marks.keys.minOrNull()
                            ?.let { LocalDate.parse(it).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() }
                        ?: Clock.System.now().toEpochMilliseconds()
                    queries.insertImportedHabit(
                        draft.title.trim(), draft.icon, draft.color, draft.remarks, draft.activeDays.toLong(),
                        if (draft.isArchived) 1L else 0L, draft.targetValue, draft.targetType,
                        if (draft.isNegative) 1L else 0L, createdAt,
                    )
                    created++
                    queries.selectLastHabitId().awaitAsOne().max ?: error("Habit was not created")
                }
                habit.marks.forEach { (date, mark) -> marks += queries.importDayState(id, date, mark) }
            }
        }
        return ImportSummary(created, marks.toInt())
    }

    private suspend fun clear() {
        queries.deleteAllGoals()
        queries.deleteAllGoalGroups()
        queries.deleteAllDayStates()
        queries.deleteAllHabitTimes()
        queries.deleteAllIntents()
        queries.deleteAllHabits()
        queries.deleteAllSettings()
    }

    private suspend fun SqlInsert.execute(): Long = when (table) {
        "Habit" -> queries.importHabit(
            long("id"), string("title"), stringOrNull("icon"), stringOrNull("color"), stringOrNull("remarks"),
            long("createdAt", 0), long("updatedAt", 0), long("activeDays", ALL_DAYS.toLong()),
            long("isArchived", 0), doubleOrNull("targetValue"), stringOrNull("targetType"),
            long("isNegative", 0),
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
        "GoalGroup" -> queries.importGoalGroup(long("id"), long("habitId"), string("title"), long("position", 0))
        "Goal" -> queries.importGoal(
            long("id"), long("groupId"), string("title"), stringOrNull("description"),
            long("isFinished", 0), long("createdAt", 0),
        )
        "Setting" -> queries.importSetting(string("key"), string("value"))
        else -> throw IllegalArgumentException("Unknown table: $table")
    }

    private fun SqlInsert.longOrNull(column: String): Long? = values[column]?.let {
        it as? Long ?: throw IllegalArgumentException("$table.$column is not a number")
    }

    private fun SqlInsert.doubleOrNull(column: String): Double? = values[column]?.let {
        (it as? Double ?: (it as? Long)?.toDouble())
            ?: throw IllegalArgumentException("$table.$column is not a number")
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
        goalRepository = SqlGoalRepository(this),
        habitTimeRepository = SqlHabitTimeRepository(this),
        intentRepository = SqlIntentRepository(this),
        settingsRepository = SqlSettingsRepository(this),
        backupRepository = SqlBackupRepository(this),
        reminderScheduler = reminderScheduler,
    )
