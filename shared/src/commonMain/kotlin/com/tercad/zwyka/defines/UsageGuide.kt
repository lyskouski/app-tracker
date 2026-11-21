// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.calendar
import com.tercad.zwyka.resources.gesture_arrows
import com.tercad.zwyka.resources.gesture_click
import com.tercad.zwyka.resources.gesture_drag
import com.tercad.zwyka.resources.gesture_long_press
import com.tercad.zwyka.resources.gesture_shift_wheel
import com.tercad.zwyka.resources.gesture_swipe_left
import com.tercad.zwyka.resources.gesture_swipe_right
import com.tercad.zwyka.resources.gesture_wheel
import com.tercad.zwyka.resources.guide_alt_digit
import com.tercad.zwyka.resources.guide_back
import com.tercad.zwyka.resources.guide_date_skip
import com.tercad.zwyka.resources.guide_day_delete
import com.tercad.zwyka.resources.guide_day_state
import com.tercad.zwyka.resources.guide_event_edit
import com.tercad.zwyka.resources.guide_focus
import com.tercad.zwyka.resources.guide_general
import com.tercad.zwyka.resources.guide_goal_edit
import com.tercad.zwyka.resources.guide_goal_toggle
import com.tercad.zwyka.resources.guide_group_delete
import com.tercad.zwyka.resources.guide_group_edit
import com.tercad.zwyka.resources.guide_group_move
import com.tercad.zwyka.resources.guide_habit_details
import com.tercad.zwyka.resources.guide_habit_done
import com.tercad.zwyka.resources.guide_habit_icon
import com.tercad.zwyka.resources.guide_habit_skip
import com.tercad.zwyka.resources.guide_habit_state
import com.tercad.zwyka.resources.guide_home_view
import com.tercad.zwyka.resources.guide_long_press_keys
import com.tercad.zwyka.resources.guide_metrics
import com.tercad.zwyka.resources.guide_new_item
import com.tercad.zwyka.resources.guide_next_day
import com.tercad.zwyka.resources.guide_pomodoro
import com.tercad.zwyka.resources.guide_popup_apply
import com.tercad.zwyka.resources.guide_popup_close
import com.tercad.zwyka.resources.guide_popup_delete
import com.tercad.zwyka.resources.guide_press
import com.tercad.zwyka.resources.guide_previous_day
import com.tercad.zwyka.resources.guide_sync
import com.tercad.zwyka.resources.guide_table_drag
import com.tercad.zwyka.resources.guide_table_keys
import com.tercad.zwyka.resources.guide_table_shift_wheel
import com.tercad.zwyka.resources.guide_table_wheel
import com.tercad.zwyka.resources.guide_tabs
import com.tercad.zwyka.resources.guide_time_slot
import com.tercad.zwyka.resources.habit_details
import com.tercad.zwyka.resources.overall
import com.tercad.zwyka.resources.settings
import com.tercad.zwyka.resources.timer
import com.tercad.zwyka.resources.today
import com.tercad.zwyka.resources.weekly

/**
 * An action of the usage guide: a pointer [gesture], or [keys] shown as they are (key names are not translated).
 */
data class GuideAction(
    val description: StringResource,
    val gesture: StringResource? = null,
    val keys: String? = null,
)

/** Actions of a page; [titleArg] fills the placeholder of [title]. */
data class GuideSection(
    val title: StringResource,
    val actions: List<GuideAction>,
    val titleArg: StringResource? = null,
)

/*
 * Actions of the pages, kept in line with the handlers:
 * see `widgets/Shortcuts.kt`, `widgets/Actionable.kt` and the pages themselves.
 */
val usageGuide = listOf(
    GuideSection(
        title = Res.string.guide_general,
        actions = listOf(
            GuideAction(Res.string.guide_new_item, keys = "Ctrl + N (⌘ + N)"),
            GuideAction(Res.string.guide_back, keys = "Backspace"),
            GuideAction(Res.string.guide_focus, keys = "Tab, Shift + Tab"),
            GuideAction(Res.string.guide_press, keys = "Enter, Space"),
            GuideAction(Res.string.guide_long_press_keys, keys = "Ctrl + Enter, Shift + F10, Menu"),
            GuideAction(Res.string.guide_popup_apply, keys = "Enter"),
            GuideAction(Res.string.guide_popup_delete, keys = "Delete"),
            GuideAction(Res.string.guide_popup_close, keys = "Esc"),
            GuideAction(Res.string.guide_tabs, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_alt_digit, keys = "Alt + 1 … Alt + 0"),
        ),
    ),
    GuideSection(
        title = Res.string.guide_home_view,
        titleArg = Res.string.today,
        actions = listOf(
            GuideAction(Res.string.guide_habit_state, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_habit_icon, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_habit_details, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_habit_done, gesture = Res.string.gesture_swipe_right),
            GuideAction(Res.string.guide_habit_skip, gesture = Res.string.gesture_swipe_left),
        ),
    ),
    GuideSection(
        title = Res.string.guide_home_view,
        titleArg = Res.string.weekly,
        actions = listOf(
            GuideAction(Res.string.guide_habit_state, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_habit_icon, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_habit_details, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_day_state, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_day_delete, gesture = Res.string.gesture_long_press),
        ),
    ),
    GuideSection(
        title = Res.string.guide_home_view,
        titleArg = Res.string.overall,
        actions = listOf(
            GuideAction(Res.string.guide_day_state, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_day_delete, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_habit_icon, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_habit_details, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_date_skip, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_metrics, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_table_drag, gesture = Res.string.gesture_drag),
            GuideAction(Res.string.guide_table_wheel, gesture = Res.string.gesture_wheel),
            GuideAction(Res.string.guide_table_shift_wheel, gesture = Res.string.gesture_shift_wheel),
            GuideAction(Res.string.guide_table_keys, gesture = Res.string.gesture_arrows),
        ),
    ),
    GuideSection(
        title = Res.string.habit_details,
        actions = listOf(
            GuideAction(Res.string.guide_goal_toggle, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_goal_edit, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_group_edit, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_group_delete, gesture = Res.string.gesture_long_press),
            GuideAction(Res.string.guide_group_move, gesture = Res.string.gesture_click),
        ),
    ),
    GuideSection(
        title = Res.string.calendar,
        actions = listOf(
            GuideAction(Res.string.guide_previous_day, gesture = Res.string.gesture_swipe_right),
            GuideAction(Res.string.guide_next_day, gesture = Res.string.gesture_swipe_left),
            GuideAction(Res.string.guide_time_slot, gesture = Res.string.gesture_click),
            GuideAction(Res.string.guide_event_edit, gesture = Res.string.gesture_click),
        ),
    ),
    GuideSection(
        title = Res.string.timer,
        actions = listOf(
            GuideAction(Res.string.guide_pomodoro, gesture = Res.string.gesture_click),
        ),
    ),
    GuideSection(
        title = Res.string.settings,
        actions = listOf(
            GuideAction(Res.string.guide_sync, gesture = Res.string.gesture_click),
        ),
    ),
)
