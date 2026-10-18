// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.screenshots

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.App
import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.core.time.formatTime
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.createServices
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.calendar
import com.tercad.zwyka.resources.settings
import io.github.lyskouski.screenshot.ScreenshotGenerator
import io.github.lyskouski.screenshot.ScreenshotScope
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
        locales = listOf(
            "en_US", "de_DE", "be_BY", "pl_PL", "pt_BR", "fr_FR", "uk_UA", "zh_CN",
            "hi_IN", "uz_UZ", "fa_IR", "ar_SA", "az_AZ", "ja_JP", "es_ES", "it_IT",
            "tr_TR", "ko_KR", "nl_NL", "cs_CZ", "ro_RO", "ka_GE", "ru_RU", "id_ID",
        ),
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
        gen.inject("4_calendar") {
            openCalendar()
        }
        gen.inject("5_calendar_add") {
            openCalendar()
            // Empty slot: the sample schedules nothing before 06:00.
            val hour = onNodeWithText(formatTime(5 * 60)).performScrollTo()
            val y = hour.fetchSemanticsNode().boundsInRoot.center.y
            onRoot().performTouchInput { click(Offset(right - 32.dp.toPx(), y)) }
            waitForIdle()
        }
        gen.run()
    }

    private suspend fun ScreenshotScope.openCalendar() {
        onNodeWithText(getString(Res.string.calendar)).performClick()
        // The schedule loads after the page is shown.
        waitUntil { onAllNodesWithText(formatTime(0)).fetchSemanticsNodes().isNotEmpty() }
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
