// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core

import com.tercad.zwyka.core.notifications.ReminderScheduler
import com.tercad.zwyka.core.repository.BackupRepository
import com.tercad.zwyka.core.repository.DayStateRepository
import com.tercad.zwyka.core.repository.GoalRepository
import com.tercad.zwyka.core.repository.HabitRepository
import com.tercad.zwyka.core.repository.HabitTimeRepository
import com.tercad.zwyka.core.repository.IntentRepository
import com.tercad.zwyka.core.repository.SettingsRepository
import com.tercad.zwyka.core.service.BackupService
import com.tercad.zwyka.core.service.DayStateService
import com.tercad.zwyka.core.service.GoalService
import com.tercad.zwyka.core.service.HabitService
import com.tercad.zwyka.core.service.ReminderService
import com.tercad.zwyka.core.service.ScheduleService
import com.tercad.zwyka.core.service.SettingsService

/*
 * Business logic used by the presentation layer.
 */
class Services(
    val habits: HabitService,
    val dayStates: DayStateService,
    val goals: GoalService,
    val schedule: ScheduleService,
    val reminders: ReminderService,
    val settings: SettingsService,
    val backup: BackupService,
)

/*
 * Wires the services over storage implementations.
 */
fun createServices(
    habitRepository: HabitRepository,
    dayStateRepository: DayStateRepository,
    goalRepository: GoalRepository,
    habitTimeRepository: HabitTimeRepository,
    intentRepository: IntentRepository,
    settingsRepository: SettingsRepository,
    backupRepository: BackupRepository,
    reminderScheduler: ReminderScheduler,
): Services {
    val reminders = ReminderService(
        habits = habitRepository,
        intents = intentRepository,
        habitTimes = habitTimeRepository,
        settings = settingsRepository,
        scheduler = reminderScheduler,
    )
    val habits = HabitService(habitRepository, dayStateRepository, reminders)
    return Services(
        habits = habits,
        dayStates = DayStateService(dayStateRepository, habitRepository),
        goals = GoalService(goalRepository),
        schedule = ScheduleService(habitTimeRepository, intentRepository, habits, reminders),
        reminders = reminders,
        settings = SettingsService(settingsRepository),
        backup = BackupService(backupRepository, reminders),
    )
}
