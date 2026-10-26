// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.model

/** Named group of [Goal]s of one habit; groups are shown by [position]. */
data class GoalGroup(
    val id: Long,
    val habitId: Long,
    val title: String,
    val position: Int = 0,
)

data class Goal(
    val id: Long,
    val groupId: Long,
    val title: String,
    val description: String? = null,
    val isFinished: Boolean = false,
    val createdAt: Long = 0,
)

/** A group with its goals, in order of creation. */
data class GroupedGoals(
    val group: GoalGroup,
    val goals: List<Goal>,
) {
    /** Every goal of the group is finished. */
    val isFinished: Boolean
        get() = goals.isNotEmpty() && goals.all { it.isFinished }
}

/** Groups by [GoalGroup.position]; fully finished groups go after the active ones. */
fun groupGoals(groups: List<GoalGroup>, goals: List<Goal>): List<GroupedGoals> {
    val goalsByGroup = goals.groupBy { it.groupId }
    return groups
        .map { group -> GroupedGoals(group, goalsByGroup[group.id].orEmpty().sortedBy { it.id }) }
        .sortedWith(compareBy({ it.isFinished }, { it.group.position }, { it.group.id }))
}
