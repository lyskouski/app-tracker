package com.tercad.zwyka

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
    val databaseFile = File(System.getProperty("user.home"), ".zwyka.db")
    Database(
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            schema = Database.Schema.synchronous(),
        ),
    )
}

/* Tray icon: notifications are shown through the system tray. */
private object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawCircle(Color(0xFF4F6BED))
    }
}

fun main() = application {
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
