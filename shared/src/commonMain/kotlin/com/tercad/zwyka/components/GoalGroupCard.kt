// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

import com.tercad.zwyka.core.model.Goal
import com.tercad.zwyka.core.model.GroupedGoals
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.move_up

private val cardShape = RoundedCornerShape(16.dp)

/*
 * ┌──────────────────────────┐
 * │ 1/2                  [↑] │
 * │  ○ 1 Open the laptop     │
 * │  ✓ 2 Write a paragraph   │
 * │ ( ) Group title          │
 * └──────────────────────────┘
 * A goal toggles its status on click; a finished group is struck through.
 */
@Composable
fun GoalGroupCard(
    groupedGoals: GroupedGoals,
    canMoveUp: Boolean,
    enabled: Boolean,
    onMoveUp: () -> Unit,
    onToggle: (Goal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = currentColorScheme()
    val isFinished = groupedGoals.isFinished
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant, cardShape)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${groupedGoals.goals.count { it.isFinished }}/${groupedGoals.goals.size}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onMoveUp, enabled = enabled && canMoveUp) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = stringResource(Res.string.move_up),
                )
            }
        }
        groupedGoals.goals.forEachIndexed { index, goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { onToggle(goal) }
                    .padding(vertical = 4.dp)
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (goal.isFinished) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp).size(16.dp),
                )
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.width(16.dp).padding(top = 2.dp),
                )
                Column {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        textDecoration = if (goal.isFinished) TextDecoration.LineThrough else null,
                    )
                    goal.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        if (isFinished) {
                            Modifier.background(colors.onSurface)
                        } else {
                            Modifier.border(2.dp, colors.onSurface, CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isFinished) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.surface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = groupedGoals.group.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textDecoration = if (isFinished) TextDecoration.LineThrough else null,
            )
        }
    }
}
