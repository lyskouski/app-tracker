package com.tercad.zwyka

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tercad.zwyka.core.model.parseNumericValue
import com.tercad.zwyka.core.notifications.NoReminderScheduler
import com.tercad.zwyka.core.service.AutomationResult
import com.tercad.zwyka.core.service.HabitQuery
import com.tercad.zwyka.defines.leftTodayMessage
import com.tercad.zwyka.defines.message
import com.tercad.zwyka.infrastructures.createServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val ACTION_COMPLETE = "com.tercad.zwyka.action.COMPLETE"
private const val ACTION_ADD = "com.tercad.zwyka.action.ADD"
private const val ACTION_SKIP = "com.tercad.zwyka.action.SKIP"
private const val ACTION_LEFT_TODAY = "com.tercad.zwyka.action.LEFT_TODAY"
private const val EXTRA_HABIT = "habit"
private const val EXTRA_HABIT_ID = "habit_id"
private const val EXTRA_VALUE = "value"

/*
 * Today's habit states from other apps (Tasker, Automate, adb), by explicit broadcasts:
 *
 *     am broadcast -n com.tercad.zwyka/.AutomationReceiver \
 *         -a com.tercad.zwyka.action.ADD --es habit Water --ef value 1
 *
 * - COMPLETE: done; "value" stores the reached value of a numeric target;
 * - ADD: adds "value" (1 by default) to the reached value of a numeric target;
 * - SKIP: skipped ("—");
 * - LEFT_TODAY: nothing is changed, the result lists the habits left for today.
 *
 * The habit is given by its title ("habit") or id ("habit_id"); "value" is a number or a text.
 * An ordered broadcast gets RESULT_OK or RESULT_CANCELED with the outcome as the result data.
 */
class AutomationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action?.takeIf { it in actions } ?: return
        val query = HabitQuery(
            id = intent.getLongExtra(EXTRA_HABIT_ID, -1).takeIf { it >= 0 },
            title = intent.getStringExtra(EXTRA_HABIT),
        )
        val value = intent.extras?.let {
            @Suppress("DEPRECATION")
            it.get(EXTRA_VALUE)
        }?.toString()?.let(::parseNumericValue)
        val isOrdered = isOrderedBroadcast
        val pendingResult = goAsync()
        scope.launch {
            val (code, message) = try {
                val automation = AppDatabase.get(context).createServices(NoReminderScheduler).automation
                val result = when (action) {
                    ACTION_COMPLETE -> automation.complete(query, value)
                    ACTION_ADD -> automation.add(query, value ?: 1.0)
                    ACTION_SKIP -> automation.skip(query)
                    else -> null
                }
                when (result) {
                    null -> Activity.RESULT_OK to leftTodayMessage(automation.leftToday())
                    is AutomationResult.Marked -> Activity.RESULT_OK to result.message()
                    is AutomationResult.NotFound -> Activity.RESULT_CANCELED to result.message()
                }
            } catch (error: Exception) {
                Activity.RESULT_CANCELED to error.message
            }
            if (isOrdered) pendingResult.setResult(code, message, null)
            pendingResult.finish()
        }
    }

    private companion object {
        val actions = setOf(ACTION_COMPLETE, ACTION_ADD, ACTION_SKIP, ACTION_LEFT_TODAY)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
