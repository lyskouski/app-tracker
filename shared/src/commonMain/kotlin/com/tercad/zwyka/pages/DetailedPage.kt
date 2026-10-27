// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.Goal
import com.tercad.zwyka.core.model.GoalGroup
import com.tercad.zwyka.core.model.GroupedGoals
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.progress.calculateStreakSummary
import com.tercad.zwyka.components.GoalEditorDialog
import com.tercad.zwyka.components.GoalGroupCard
import com.tercad.zwyka.components.HabitContributionGraph
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.add_goal
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.cancel
import com.tercad.zwyka.resources.current_streak
import com.tercad.zwyka.resources.database_error_unknown
import com.tercad.zwyka.resources.delete
import com.tercad.zwyka.resources.delete_goal_group_confirmation
import com.tercad.zwyka.resources.delete_goal_group_confirmation_message
import com.tercad.zwyka.resources.goal_save_failed
import com.tercad.zwyka.resources.habit_target
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.max_streak

/** Details of one habit: description, target, completion of every day, streaks and goals. */
class DetailedPage(
    private val habit: Habit,
    private val services: Services,
    private val today: LocalDate,
    private val weekStart: WeekStart,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Marks of the habit by date; null while loading.
        var marks by remember(habit.id) { mutableStateOf<Map<String, String>?>(null) }
        var error by remember(habit.id) { mutableStateOf<String?>(null) }
        // Goal groups of the habit as shown; null while loading.
        var goals by remember(habit.id) { mutableStateOf<List<GroupedGoals>?>(null) }
        var showGoalDialog by remember { mutableStateOf(false) }
        // Goal changed by the dialog; null to create a new one.
        var editingGoal by remember { mutableStateOf<Goal?>(null) }
        var deletingGroup by remember { mutableStateOf<GoalGroup?>(null) }
        var isGoalSaving by remember { mutableStateOf(false) }
        var goalError by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()
        val unknownDatabaseError = stringResource(Res.string.database_error_unknown)
        LaunchedEffect(habit.id) {
            try {
                marks = services.dayStates.getForHabit(habit.id).associate { it.date to it.mark }
                goals = services.goals.getForHabit(habit.id)
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Exception) {
                error = cause.message.orEmpty()
            }
        }

        // Applies a change of goals; [onSuccess] runs after the reload.
        fun changeGoals(onSuccess: () -> Unit = {}, change: suspend () -> List<GroupedGoals>) {
            isGoalSaving = true
            goalError = null
            coroutineScope.launch {
                try {
                    goals = change()
                    onSuccess()
                } catch (cause: CancellationException) {
                    throw cause
                } catch (cause: Exception) {
                    goalError = cause.message ?: unknownDatabaseError
                } finally {
                    isGoalSaving = false
                }
            }
        }

        Scaffold(
            containerColor = currentColorScheme().surface,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(currentColorScheme().primary),
                ) {
                    TextButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.align(Alignment.CenterStart),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(Res.string.back),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                // Scaffold lifts the FAB 16.dp above the bar; shift it so it protrudes 12.dp.
                FloatingActionButton(
                    onClick = {
                        goalError = null
                        editingGoal = null
                        showGoalDialog = true
                    },
                    modifier = Modifier.offset(y = 52.dp),
                    containerColor = currentColorScheme().inversePrimary,
                    contentColor = currentColorScheme().secondary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.add_goal),
                    )
                }
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val textColor = currentColorScheme().onSurface
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                )
                habit.remarks?.let {
                    Text(text = it, color = textColor)
                }
                habit.targetValue?.let { target ->
                    Text(
                        text = stringResource(
                            Res.string.habit_target,
                            formatNumericValue(target),
                            habit.targetType.orEmpty(),
                        ).trim(),
                        color = textColor,
                    )
                }
                error?.let {
                    Text(stringResource(Res.string.habits_load_failed, it), color = currentColorScheme().error)
                }
                marks?.let { habitMarks ->
                    HabitContributionGraph(
                        habit = habit,
                        marks = habitMarks,
                        today = today,
                        weekStart = weekStart,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    val streaks = remember(habitMarks, today) {
                        calculateStreakSummary(habit, habitMarks, today)
                    }
                    Text(stringResource(Res.string.current_streak, streaks.current.toString()), color = textColor)
                    Text(stringResource(Res.string.max_streak, streaks.max.toString()), color = textColor)
                }
                goals?.forEachIndexed { index, groupedGoals ->
                    GoalGroupCard(
                        groupedGoals = groupedGoals,
                        canMoveUp = index > 0,
                        enabled = !isGoalSaving,
                        onMoveUp = { changeGoals { services.goals.moveUp(habit.id, groupedGoals.group.id) } },
                        onToggle = { goal ->
                            changeGoals { services.goals.setFinished(habit.id, goal.id, !goal.isFinished) }
                        },
                        onEdit = { goal ->
                            goalError = null
                            editingGoal = goal
                            showGoalDialog = true
                        },
                        onDelete = {
                            goalError = null
                            deletingGroup = groupedGoals.group
                        },
                    )
                }
                if (!showGoalDialog) {
                    goalError?.let {
                        Text(stringResource(Res.string.goal_save_failed, it), color = currentColorScheme().error)
                    }
                }
            }
        }

        if (showGoalDialog) {
            val goal = editingGoal
            GoalEditorDialog(
                groups = goals.orEmpty().map { it.group.title },
                isSaving = isGoalSaving,
                error = goalError,
                onDismiss = {
                    showGoalDialog = false
                    goalError = null
                },
                onSave = { draft ->
                    changeGoals(onSuccess = { showGoalDialog = false }) {
                        if (goal == null) {
                            services.goals.create(habit.id, draft)
                        } else {
                            services.goals.update(habit.id, goal.id, draft)
                        }
                    }
                },
                goal = goal,
                goalGroup = goals.orEmpty().firstOrNull { it.group.id == goal?.groupId }?.group?.title.orEmpty(),
            )
        }

        deletingGroup?.let { group ->
            AlertDialog(
                onDismissRequest = { deletingGroup = null },
                title = { Text(stringResource(Res.string.delete_goal_group_confirmation)) },
                text = { Text(stringResource(Res.string.delete_goal_group_confirmation_message, group.title)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deletingGroup = null
                            changeGoals { services.goals.deleteGroup(habit.id, group.id) }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = currentColorScheme().error,
                        ),
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingGroup = null }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
    }
}
