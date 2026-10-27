package com.tercad.zwyka

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.core.notifications.InProcessReminderScheduler
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val database by lazy {
    // Sandboxed packages have no write access to the home folder:
    // Snap sets ZWYKA_DATA_DIR, Flatpak provides ~/.var/app/com.tercad.zwyka/data as XDG_DATA_HOME.
    val dataDir = (System.getenv("ZWYKA_DATA_DIR") ?: System.getenv("FLATPAK_ID")?.let { System.getenv("XDG_DATA_HOME") })
        ?.takeIf { it.isNotBlank() }?.let(::File)?.apply { mkdirs() }
    val databaseFile = File(dataDir ?: File(System.getProperty("user.home")), ".zwyka.db")
    Database(
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            schema = Database.Schema.synchronous(),
        ),
    )
}

/*
 * Tray icon: notifications are shown through the system tray.
 * The single-colour glyph of docs/design-flow/logo.svg, without the background.
 */
private object TrayIcon : Painter() {
    // Bounds of the glyph within the 108x108 viewport of the logo.
    private val bounds = Rect(18.05f, 10.26f, 93.76f, 93.03f)
    private val glyph = PathParser().parsePathString(
        "M25.913151,60.332054C17.459862,71.572859 16.150805,92.731071 20.40067,93.012578" +
            "C26.526461,93.627341 29.073867,76.980861 44.66384,72.878273" +
            "C54.698369,70.237631 64.857311,72.036549 73.205175,73.841161" +
            "C84.222277,74.007875 77.487949,54.814699 86.089025,49.829876" +
            "C88.620167,48.385744 91.163912,46.280984 89.426657,44.069445" +
            "C88.455257,42.799755 86.561357,42.186122 85.117508,41.884046" +
            "C60.21628,37.791999 43.105147,39.640879 25.913151,60.332054Z",
    ).toPath().apply {
        addOval(Rect(center = Offset(80.95739f, 23.058905f), radius = 12.79952f))
        addOval(Rect(center = Offset(53.40937f, 28.297575f), radius = 8.0582294f))
        addOval(Rect(center = Offset(30.353638f, 38.469524f), radius = 8.0582294f))
    }

    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        val factor = minOf(size.width / bounds.width, size.height / bounds.height)
        translate(
            left = (size.width - bounds.width * factor) / 2 - bounds.left * factor,
            top = (size.height - bounds.height * factor) / 2 - bounds.top * factor,
        ) {
            scale(factor, pivot = Offset.Zero) {
                drawPath(glyph, Color(0xFF912391))
            }
        }
    }
}

fun main() {
    // macOS menu bar: the tray icon is a template image, tinted by the system for light and dark menu bars.
    System.setProperty("apple.awt.enableTemplateImages", "true")
    runApplication()
}

private fun runApplication() = application {
    val trayState = rememberTrayState()
    val reminderScheduler = remember {
        InProcessReminderScheduler(CoroutineScope(SupervisorJob() + Dispatchers.Default)) { reminder ->
            trayState.sendNotification(Notification(reminder.title, reminder.text))
        }
    }
    Tray(state = trayState, icon = TrayIcon, tooltip = "Zwyka")
    Window(onCloseRequest = ::exitApplication, title = "Zwyka") {
        val fileTransfer = remember(window) { DesktopFileTransfer(window) }
        App(database = database, reminderScheduler = reminderScheduler, fileTransfer = fileTransfer)
    }
}
