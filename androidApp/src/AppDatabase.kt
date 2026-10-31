package com.tercad.zwyka

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tercad.zwyka.infrastructures.Database

/*
 * Database of the process, shared by the screens and the intents of [AutomationReceiver].
 */
object AppDatabase {
    @Volatile
    private var database: Database? = null

    fun get(context: Context): Database =
        database ?: synchronized(this) {
            database ?: Database(
                AndroidSqliteDriver(
                    Database.Schema.synchronous(),
                    context.applicationContext,
                    "zwyka.db",
                ),
            ).also { database = it }
        }
}
