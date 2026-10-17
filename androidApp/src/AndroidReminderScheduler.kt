package com.tercad.zwyka

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.tercad.zwyka.core.notifications.Reminder
import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.core.notifications.nextTrigger
import kotlin.time.Clock
import org.json.JSONArray
import org.json.JSONObject

private const val PREFERENCES = "zwyka.reminders"
private const val KEY_REMINDERS = "reminders"
private const val EXTRA_REMINDER_ID = "reminderId"
private const val CHANNEL_ID = "reminders"
private const val ACTION_REMIND = "com.tercad.zwyka.REMIND"

/*
 * Reminders through AlarmManager.
 *
 * The reminders are also kept in SharedPreferences, so that alarms can be
 * restored after a reboot and the next week's alarm can be set without the database.
 */
class AndroidReminderScheduler(
    private val activity: Activity,
) : ReminderScheduler {
    private val context: Context = activity.applicationContext

    override fun schedule(reminders: List<Reminder>) {
        loadReminders(context).forEach { cancelAlarm(context, it.id) }
        saveReminders(context, reminders)
        reminders.forEach { setAlarm(context, it) }
    }

    override fun requestPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }
}

/*
 * Shows a due reminder and sets its next alarm; restores all alarms after a reboot / update.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminders = loadReminders(context)
        if (intent.action == ACTION_REMIND) {
            val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
            reminders.firstOrNull { it.id == id }?.let { reminder ->
                showNotification(context, reminder)
                setAlarm(context, reminder)
            }
        } else {
            reminders.forEach { setAlarm(context, it) }
        }
    }
}

private fun alarmIntent(context: Context, id: Long): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        id.toInt(),
        Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(EXTRA_REMINDER_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

private fun setAlarm(context: Context, reminder: Reminder) {
    val trigger = reminder.nextTrigger(Clock.System.now()) ?: return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val operation = alarmIntent(context, reminder.id)
    val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    if (exact) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilliseconds(), operation)
    } else {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.toEpochMilliseconds(), operation)
    }
}

private fun cancelAlarm(context: Context, id: Long) {
    context.getSystemService(AlarmManager::class.java).cancel(alarmIntent(context, id))
}

private fun showNotification(context: Context, reminder: Reminder) {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }
    val openApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(context, CHANNEL_ID)
    } else {
        @Suppress("DEPRECATION")
        Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH)
    }
    val notification = builder
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(reminder.title)
        .setContentText(reminder.text)
        .setStyle(Notification.BigTextStyle().bigText(reminder.text))
        .setContentIntent(openApp)
        .setAutoCancel(true)
        .build()
    // Without the permission (Android 13+) the notification is silently dropped.
    manager.notify(reminder.id.toInt(), notification)
}

private fun saveReminders(context: Context, reminders: List<Reminder>) {
    val json = JSONArray()
    reminders.forEach {
        json.put(
            JSONObject()
                .put("id", it.id)
                .put("title", it.title)
                .put("text", it.text)
                .put("minuteOfDay", it.minuteOfDay)
                .put("activeDays", it.activeDays),
        )
    }
    context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_REMINDERS, json.toString())
        .apply()
}

private fun loadReminders(context: Context): List<Reminder> {
    val raw = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(KEY_REMINDERS, null) ?: return emptyList()
    val json = JSONArray(raw)
    return List(json.length()) { index ->
        val item = json.getJSONObject(index)
        Reminder(
            id = item.getLong("id"),
            title = item.getString("title"),
            text = item.getString("text"),
            minuteOfDay = item.getInt("minuteOfDay"),
            activeDays = item.getInt("activeDays"),
        )
    }
}
