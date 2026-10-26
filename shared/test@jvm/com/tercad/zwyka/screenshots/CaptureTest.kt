// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.screenshots

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
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
import com.tercad.zwyka.defines.applicationLanguages
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.infrastructures.createServices
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_habit
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.calendar
import com.tercad.zwyka.resources.completeness_colors
import com.tercad.zwyka.resources.completeness_type
import com.tercad.zwyka.resources.max_streak
import com.tercad.zwyka.resources.metrics
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.resources.timer
import com.tercad.zwyka.resources.timer_next_events
import io.github.lyskouski.screenshot.ScreenshotGenerator
import io.github.lyskouski.screenshot.ScreenshotScope
import io.github.lyskouski.screenshot.ScreenshotTarget
import org.jetbrains.compose.resources.getString
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.io.File
import kotlin.test.Test
import kotlin.time.Clock

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
            "tr_TR", "ko_KR", "nl_NL", "cs_CZ", "ro_RO", "ka_GE", "ru_RU", "id_ID", "zh_TW",
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
        // Every screenshot starts from its own database: the changed setting stays in this step.
        gen.inject("6_home_colors") {
            onNodeWithText(getString(Res.string.settings)).performClick()
            onNodeWithText(getString(Res.string.completeness_type)).performScrollTo().performClick()
            onNodeWithText(getString(Res.string.completeness_colors)).performClick()
            onNodeWithText(getString(Res.string.back)).performClick()
            waitForIdle()
        }
        gen.inject("7_detailed_metrics") {
            // The header cell of a habit is described by its title.
            onNodeWithContentDescription("Mental Training").performTouchInput { longClick() }
            // The day states of the habit load after the page is shown.
            val maxStreak = getString(Res.string.max_streak, "").trim()
            waitUntil { onAllNodesWithText(maxStreak, substring = true).fetchSemanticsNodes().isNotEmpty() }
        }
        gen.inject("8_timer") {
            openCalendar()
            onNodeWithText(getString(Res.string.timer)).performClick()
            // The schedule loads after the page is shown.
            val nextEvents = getString(Res.string.timer_next_events)
            waitUntil { onAllNodesWithText(nextEvents).fetchSemanticsNodes().isNotEmpty() }
        }
        gen.inject("9_metrics") {
            // Today's progress (the corner cell of the table) is described by its percentage.
            onAllNodes(hasClickAction() and hasContentDescription("%", substring = true)).onFirst().performClick()
            val metrics = getString(Res.string.metrics)
            waitUntil { onAllNodesWithText(metrics).fetchSemanticsNodes().isNotEmpty() }
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
        listOf("Goal", "GoalGroup", "DayState", "HabitTime", "Habit", "Intent", "Setting").forEach { table ->
            driver.execute(null, "DELETE FROM $table", 0)
        }
        val database = Database(driver)
        val services = database.createServices(NoReminderScheduler)
        services.backup.import(resolveDates(SAMPLE_DATA.readText()))
        // Regional variants (zh-TW) have their own translation, other locales use the language only.
        val language = target.languageTag.takeIf { tag -> applicationLanguages.any { it.tag == tag } }
            ?: target.language
        services.settings.save(services.settings.load().copy(language = language))
        return database
    }

    /* The import reads values only: `date('now', '-N days')` is replaced by the date itself. */
    private fun resolveDates(sql: String): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return RELATIVE_DATE.replace(sql) { match ->
            val days = match.groupValues[1].ifEmpty { "0" }.toInt()
            "'${today.plus(days, DateTimeUnit.DAY)}'"
        }
    }

    private companion object {
        val RELATIVE_DATE = Regex("""date\('now'(?:, '([+-]\d+) days?')?\)""")

        // Relative to the module directory, the working directory of Gradle tests.
        val SAMPLE_DATA = File("test@jvm/com/tercad/zwyka/screenshots/zwyka-sample.sq")
    }
}
