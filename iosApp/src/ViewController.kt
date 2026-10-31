package com.tercad.zwyka

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tercad.zwyka.infrastructures.Database

/** Database of the process, shared by the screens and the Siri Shortcuts. */
internal val database by lazy {
    Database(NativeSqliteDriver(Database.Schema.synchronous(), "zwyka.db"))
}

private val reminderScheduler by lazy { IosReminderScheduler() }

private val fileTransfer by lazy { IosFileTransfer() }

fun ViewController() = ComposeUIViewController {
    App(database = database, reminderScheduler = reminderScheduler, fileTransfer = fileTransfer)
}
