// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.screenshots

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.App
import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.createServices
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.settings
import io.github.lyskouski.screenshot.ScreenshotGenerator
import io.github.lyskouski.screenshot.ScreenshotTarget
import org.jetbrains.compose.resources.getString
import java.io.File
import kotlin.test.Test

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
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous())
        // The schema seeds placeholder data: start from empty tables.
        listOf("DayState", "HabitTime", "Habit", "Intent", "Setting").forEach { table ->
            driver.execute(null, "DELETE FROM $table", 0)
        }
        val database = Database(driver)
        val services = database.createServices(NoReminderScheduler)
        services.backup.import(SAMPLE_DATA.readText())
        services.settings.save(services.settings.load().copy(language = target.language))
        return database
    }

    private companion object {
        // Relative to the module directory, the working directory of Gradle tests.
        val SAMPLE_DATA = File("test@jvm/com/tercad/zwyka/screenshots/zwyka-sample.sq")
    }
}
