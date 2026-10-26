// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.tercad.zwyka.core.model.GoalDraft
import com.tercad.zwyka.core.model.GroupedGoals
import com.tercad.zwyka.core.service.GoalService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalServiceTest {
    private val database = Database(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Database.Schema.synchronous()),
    )
    private val goals = GoalService(SqlGoalRepository(database))

    private fun List<GroupedGoals>.titles() = map { group -> group.group.title to group.goals.map { it.title } }

    @Test
    fun groupsGoalsByNamePerHabit() = runTest {
        goals.create(1, GoalDraft("Open the laptop", null, "Blog"))
        goals.create(1, GoalDraft("Go for a walk", "30 min", "Workout"))
        goals.create(1, GoalDraft("Write one paragraph", null, " blog "))
        goals.create(2, GoalDraft("Read 10 pages", null, "Blog"))

        assertEquals(
            listOf("Blog" to listOf("Open the laptop", "Write one paragraph"), "Workout" to listOf("Go for a walk")),
            goals.getForHabit(1).titles(),
        )
        assertEquals(listOf("Blog" to listOf("Read 10 pages")), goals.getForHabit(2).titles())
        assertEquals("30 min", goals.getForHabit(1)[1].goals.single().description)
    }

    @Test
    fun finishedGroupsGoLast() = runTest {
        goals.create(1, GoalDraft("Open the laptop", null, "Blog"))
        goals.create(1, GoalDraft("Write one paragraph", null, "Blog"))
        val created = goals.create(1, GoalDraft("Go for a walk", null, "Workout"))

        val blog = created.first().goals
        goals.setFinished(1, blog[0].id, true)
        assertEquals(listOf("Blog", "Workout"), goals.getForHabit(1).map { it.group.title })

        val finished = goals.setFinished(1, blog[1].id, true)
        assertEquals(listOf("Workout", "Blog"), finished.map { it.group.title })
        assertTrue(finished.last().isFinished)

        assertEquals(listOf("Blog", "Workout"), goals.setFinished(1, blog[1].id, false).map { it.group.title })
    }

    @Test
    fun movesGroupUp() = runTest {
        goals.create(1, GoalDraft("a", null, "First"))
        goals.create(1, GoalDraft("b", null, "Second"))
        val created = goals.create(1, GoalDraft("c", null, "Third"))

        val third = created.last().group.id
        assertEquals(listOf("First", "Third", "Second"), goals.moveUp(1, third).map { it.group.title })
        assertEquals(listOf("Third", "First", "Second"), goals.moveUp(1, third).map { it.group.title })
        assertEquals(listOf("Third", "First", "Second"), goals.moveUp(1, third).map { it.group.title })
    }

    @Test
    fun habitDeletionRemovesGoals() = runTest {
        goals.create(1, GoalDraft("a", null, "First"))
        goals.create(2, GoalDraft("b", null, "First"))

        SqlHabitRepository(database).delete(1)

        assertEquals(emptyList(), goals.getForHabit(1))
        assertEquals(1, goals.getForHabit(2).size)
    }
}
