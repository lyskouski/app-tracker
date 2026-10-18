package com.tercad.zwyka

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import org.w3c.dom.Worker
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.tercad.zwyka.infrastructures.Database
import com.tercad.zwyka.core.notifications.InProcessReminderScheduler
import com.tercad.zwyka.core.notifications.Reminder
import com.tercad.zwyka.core.notifications.ReminderScheduler
import kotlinx.coroutines.MainScope

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val driver = WebWorkerDriver(createSqlJsWorker())
    val database = Database(driver)
    val reminderScheduler = WebReminderScheduler()

    ComposeViewport {
        App(
            database = database,
            initializeDatabase = { initializeDatabase(driver) },
            reminderScheduler = reminderScheduler,
            fileTransfer = WebFileTransfer(),
        )
    }
}

/*
 * Browser notifications, shown while the application is open in a tab.
 */
private class WebReminderScheduler : ReminderScheduler {
    private val scheduler = InProcessReminderScheduler(MainScope()) { reminder ->
        showNotification(reminder.title, reminder.text, "zwyka-reminder-${reminder.id}")
    }

    override fun schedule(reminders: List<Reminder>) = scheduler.schedule(reminders)

    override fun requestPermission() = requestNotificationPermission()
}

private fun requestNotificationPermission(): Unit = js(
    """{
        if (typeof Notification !== "undefined" && Notification.permission === "default") {
            Notification.requestPermission();
        }
    }""",
)

// Mobile browsers allow notifications only through the service worker.
// `renotify` alerts again even when a previous notification with the same tag is still listed.
private fun showNotification(title: String, body: String, tag: String): Unit = js(
    """{
        if (typeof Notification === "undefined" || Notification.permission !== "granted") return;
        const options = { body: body, icon: "favicon.ico", tag: tag, renotify: true };
        const fallback = () => new Notification(title, options);
        if ("serviceWorker" in navigator) {
            navigator.serviceWorker.getRegistration()
                .then((registration) => registration ? registration.showNotification(title, options) : fallback())
                .catch(fallback);
        } else {
            fallback();
        }
    }""",
)

private fun createSqlJsWorker(): Worker =
    js("""new Worker(new URL("@cashapp/zwyka-sqljs-worker/sqljs.worker.js", import.meta.url))""")

private suspend fun initializeDatabase(driver: SqlDriver) {
    val currentVersion = driver.executeQuery(
        identifier = null,
        sql = "PRAGMA user_version",
        mapper = { cursor ->
            val hasRow = cursor.next().value
            QueryResult.Value(if (hasRow) cursor.getLong(0) ?: 0L else 0L)
        },
        parameters = 0,
    ).await()

    // One transaction so a failed migration cannot leave a half-migrated schema in IndexedDB.
    Database(driver).transaction {
        when {
            currentVersion == 0L -> Database.Schema.create(driver).await()
            currentVersion < Database.Schema.version ->
                Database.Schema.migrate(driver, currentVersion, Database.Schema.version).await()
            currentVersion > Database.Schema.version ->
                error("Database schema $currentVersion is newer than this app supports.")
        }

        if (currentVersion < Database.Schema.version) {
            driver.execute(
                identifier = null,
                sql = "PRAGMA user_version = ${Database.Schema.version}",
                parameters = 0,
            ).await()
        }
    }
}
