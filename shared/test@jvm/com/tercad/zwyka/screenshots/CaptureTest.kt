// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.screenshots

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.App
import com.tercad.zwyka.core.model.AppSettings
import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.model.HabitDraft
import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.defines.ThemeMode
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.statusOptions
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.createServices
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.settings
import io.github.lyskouski.screenshot.ScreenshotGenerator
import io.github.lyskouski.screenshot.ScreenshotTarget
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.time.Clock

/*
 * Store screenshots: ./gradlew :shared:jvmScreenshots
 * Filters: -PSCREENSHOT_DEVICES="iPhone 15 Pro" -PSCREENSHOT_LOCALES=de -PSCREENSHOT_STEPS=1_home -PSKIP_EXISTANT=true
 */
class CaptureTest {
    private var database: Database? = null

    private val gen = ScreenshotGenerator(
        locales = listOf("en_US", "de_DE"),
        onInit = { target -> database = sampleDatabase(target) },
    ) {
        App(database = database)
    }

    @Test
    fun capture() {
        gen.inject("1_home")
        gen.inject("2_add_habit") {
            onNodeWithContentDescription(getString(Res.string.add_habit)).performClick()
        }
        gen.inject("3_settings") {
            onNodeWithText(getString(Res.string.settings)).performClick()
        }
        gen.run()
    }

    private suspend fun sampleDatabase(target: ScreenshotTarget): Database {
        val database = Database(
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
        )
        val services = database.createServices(NoReminderScheduler)
        services.settings.save(AppSettings(language = target.language, themeMode = ThemeMode.LIGHT))
        // The schema seeds placeholder habits.
        services.habits.getAll().forEach { services.habits.delete(it.id) }

        val everyDay = 0b1111111
        val habits = listOf(
            HabitDraft("Morning run", "DirectionsRun", "#FF7043", null, 0b0010101),
            HabitDraft("Read 20 pages", "MenuBook", "#26A69A", null, everyDay),
            HabitDraft("Drink water", "WaterDrop", "#42A5F5", null, everyDay),
            HabitDraft("Meditation", "SelfImprovement", "#AB47BC", null, everyDay),
            HabitDraft("Sleep before 23:00", "Bedtime", "#7CB342", null, 0b0011111),
        ).fold(emptyList<Long>()) { _, draft ->
            services.habits.save(null, draft, WeekStart.MONDAY).map { it.id }
        }

        // Deterministic pattern of marks over the visible weeks.
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val marks = statusOptions.map { it.mark }
        habits.forEachIndexed { index, habitId ->
            for (day in 1..42) {
                val seed = (day * 7 + index * 13) % 10
                if (seed < 2) continue
                val mark = marks[(seed + index) % marks.size]
                services.dayStates.setMark(DayState(habitId, today.minus(day, DateTimeUnit.DAY).toString(), mark))
            }
        }
        return database
    }
}
