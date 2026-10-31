package com.tercad.zwyka

import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.core.service.HabitQuery
import com.tercad.zwyka.defines.leftTodayMessage
import com.tercad.zwyka.defines.message
import com.tercad.zwyka.infrastructures.createServices

/*
 * Today's habit states for the Siri Shortcuts (App Intents of AppIntents.swift);
 * each action returns its outcome to be spoken, e.g. "Water: 3 / 8 glasses".
 *
 * Called from the main thread, as the suspend functions of Kotlin/Native require.
 */
object IosAutomation {
    private val automation by lazy { database.createServices(NoReminderScheduler).automation }

    /** Active habits, by title. */
    @Throws(Exception::class)
    suspend fun habits(): List<Habit> = automation.habits()

    /** Done: 100%, or the numeric target. */
    @Throws(Exception::class)
    suspend fun complete(habitId: Long): String = automation.complete(HabitQuery(id = habitId)).message()

    /** Done with the reached [value] of a numeric target. */
    @Throws(Exception::class)
    suspend fun completeWithValue(habitId: Long, value: Double): String =
        automation.complete(HabitQuery(id = habitId), value).message()

    /** Adds [value] to today's reached value of a numeric target. */
    @Throws(Exception::class)
    suspend fun add(habitId: Long, value: Double): String = automation.add(HabitQuery(id = habitId), value).message()

    @Throws(Exception::class)
    suspend fun skip(habitId: Long): String = automation.skip(HabitQuery(id = habitId)).message()

    @Throws(Exception::class)
    suspend fun leftToday(): String = leftTodayMessage(automation.leftToday())
}
