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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.Services
import com.tercad.zwyka.core.model.Habit
import com.tercad.zwyka.core.model.formatNumericValue
import com.tercad.zwyka.core.progress.calculateStreakSummary
import com.tercad.zwyka.components.HabitContributionGraph
import com.tercad.zwyka.defines.WeekStart
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.back
import com.tercad.zwyka.resources.current_streak
import com.tercad.zwyka.resources.habit_target
import com.tercad.zwyka.resources.habits_load_failed
import com.tercad.zwyka.resources.max_streak

/** Details of one habit: description, target, completion of every day and streaks. */
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
        LaunchedEffect(habit.id) {
            try {
                marks = services.dayStates.getForHabit(habit.id).associate { it.date to it.mark }
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Exception) {
                error = cause.message.orEmpty()
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
            }
        }
    }
}
