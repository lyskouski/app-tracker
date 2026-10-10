package com.tercad.zwyka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tercad.zwyka.infrastructures.Database

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val database = Database(
            AndroidSqliteDriver(
                Database.Schema.synchronous(),
                applicationContext,
                "zwyka.db",
            ),
        )
        setContent {
            App(database = database)
        }
    }
}
