package com.tercad.zwyka

import com.tercad.zwyka.core.notifications.Reminder
import com.tercad.zwyka.core.notifications.ReminderScheduler
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

private const val REQUEST_PREFIX = "zwyka.reminder."

/*
 * Reminders through UNUserNotificationCenter: one repeating calendar trigger
 * per reminder and week day, so they are shown by iOS while the app is closed.
 *
 * iOS keeps up to 64 pending requests per app.
 */
class IosReminderScheduler : ReminderScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // Shows reminders also while the app is in the foreground.
    private val delegate = ForegroundPresenter()

    init {
        center.delegate = delegate
    }

    override fun schedule(reminders: List<Reminder>) {
        center.getPendingNotificationRequestsWithCompletionHandler { pending ->
            val previous = pending.orEmpty()
                .mapNotNull { (it as? UNNotificationRequest)?.identifier }
                .filter { it.startsWith(REQUEST_PREFIX) }
            center.removePendingNotificationRequestsWithIdentifiers(previous)
            reminders.forEach(::add)
        }
    }

    override fun requestPermission() {
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { _, _ -> }
    }

    private fun add(reminder: Reminder) {
        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(reminder.text)
            setSound(UNNotificationSound.defaultSound)
        }
        for (day in 0 until 7) {
            if (reminder.activeDays and (1 shl day) == 0) continue
            val components = NSDateComponents().apply {
                // NSDateComponents: 1 is Sunday ... 7 is Saturday; bit 0 is Monday.
                weekday = ((day + 1) % 7 + 1).toLong()
                hour = (reminder.minuteOfDay / 60).toLong()
                minute = (reminder.minuteOfDay % 60).toLong()
            }
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = "$REQUEST_PREFIX${reminder.id}.$day",
                content = content,
                trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    dateComponents = components,
                    repeats = true,
                ),
            )
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }
}

private class ForegroundPresenter : NSObject(), UNUserNotificationCenterDelegateProtocol {
    @ObjCSignatureOverride
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or
                UNNotificationPresentationOptionList or
                UNNotificationPresentationOptionSound,
        )
    }
}
