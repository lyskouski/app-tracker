package com.tercad.zwyka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val database = AppDatabase.get(this)
        val reminderScheduler = AndroidReminderScheduler(this)
        val fileTransfer = AndroidFileTransfer(this)
        setContent {
            App(database = database, reminderScheduler = reminderScheduler, fileTransfer = fileTransfer)
        }
    }
}
