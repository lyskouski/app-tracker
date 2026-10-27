// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.model.GoalDraft
import com.tercad.zwyka.core.model.GroupedGoals
import com.tercad.zwyka.core.model.groupGoals
import com.tercad.zwyka.core.repository.GoalRepository

/** Goals of a habit, grouped by name; groups and goals are not shared between habits. */
class GoalService(private val goals: GoalRepository) {
    /** Groups of the habit as shown: by position, fully finished ones last. */
    suspend fun getForHabit(habitId: Long): List<GroupedGoals> =
        groupGoals(goals.getGroups(habitId), goals.getGoals(habitId))

    /**
     * Adds a goal into the group named [GoalDraft.groupTitle] (case-insensitive);
     * a missing group is created as the last one.
     *
     * @return groups of the habit after the change.
     */
    suspend fun create(habitId: Long, draft: GoalDraft): List<GroupedGoals> {
        val groupId = resolveGroup(habitId, draft.groupTitle)
        goals.insert(groupId, draft.title.trim(), draft.description?.trim()?.ifEmpty { null })
        return getForHabit(habitId)
    }

    /**
     * Changes the goal, moving it into the group named [GoalDraft.groupTitle] as [create] does;
     * a group left without goals is deleted.
     *
     * @return groups of the habit after the change.
     */
    suspend fun update(habitId: Long, goalId: Long, draft: GoalDraft): List<GroupedGoals> {
        val previousGroupId = goals.getGoals(habitId).first { it.id == goalId }.groupId
        val groupId = resolveGroup(habitId, draft.groupTitle)
        goals.update(goalId, groupId, draft.title.trim(), draft.description?.trim()?.ifEmpty { null })
        if (goals.getGoals(habitId).none { it.groupId == previousGroupId }) {
            goals.deleteGroup(previousGroupId)
        }
        return getForHabit(habitId)
    }

    /** @return groups of the habit after the change. */
    suspend fun deleteGroup(habitId: Long, groupId: Long): List<GroupedGoals> {
        goals.deleteGroup(groupId)
        return getForHabit(habitId)
    }

    /** @return groups of the habit after the change. */
    suspend fun setFinished(habitId: Long, goalId: Long, isFinished: Boolean): List<GroupedGoals> {
        goals.updateFinished(goalId, isFinished)
        return getForHabit(habitId)
    }

    /**
     * Swaps the group with the one shown above it.
     *
     * @return groups of the habit after the change.
     */
    suspend fun moveUp(habitId: Long, groupId: Long): List<GroupedGoals> {
        val order = getForHabit(habitId).map { it.group.id }.toMutableList()
        val index = order.indexOf(groupId)
        if (index <= 0) return getForHabit(habitId)
        order[index] = order[index - 1]
        order[index - 1] = groupId
        goals.updatePositions(order)
        return getForHabit(habitId)
    }

    /** @return id of the group named [title] (case-insensitive), created as the last one when missing. */
    private suspend fun resolveGroup(habitId: Long, title: String): Long {
        val groups = goals.getGroups(habitId)
        val groupTitle = title.trim()
        return groups.firstOrNull { it.title.equals(groupTitle, ignoreCase = true) }?.id
            ?: goals.insertGroup(habitId, groupTitle, (groups.maxOfOrNull { it.position } ?: -1) + 1)
    }
}
