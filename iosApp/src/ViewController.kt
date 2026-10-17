package com.tercad.zwyka

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tercad.zwyka.infrastructures.Database

private val database by lazy {
    Database(NativeSqliteDriver(Database.Schema.synchronous(), "zwyka.db"))
}

private val reminderScheduler by lazy { IosReminderScheduler() }

fun ViewController() = ComposeUIViewController {
    App(database = database, reminderScheduler = reminderScheduler)
}
