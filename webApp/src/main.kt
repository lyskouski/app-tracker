package com.tercad.zwyka

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import org.w3c.dom.Worker
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.tercad.zwyka.infrastructures.Database

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val driver = WebWorkerDriver(createSqlJsWorker())
    val database = Database(driver)

    ComposeViewport {
        App(
            database = database,
            initializeDatabase = { initializeDatabase(driver) },
        )
    }
}

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
