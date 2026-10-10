package com.tercad.zwyka

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.infrastructures.Database
import java.io.File

private val database by lazy {
    val databaseFile = File(System.getProperty("user.home"), ".zwyka.db")
    Database(
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            schema = Database.Schema.synchronous(),
        ),
    )
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Zwyka") {
        App(database = database)
    }
}
