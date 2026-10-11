// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

import com.tercad.zwyka.data.CellKey
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit
import com.tercad.zwyka.defines.applicationColor
import com.tercad.zwyka.defines.applicationIcon
import com.tercad.zwyka.defines.currentColorScheme
import com.tercad.zwyka.defines.statusOptions


@Composable
fun CalendarTable(
    habits: List<Habit>,
    dates: List<String>,
    dayState: List<DayState>,
    onDayStateClick: (DayState) -> Unit,
    onHabitClick: (Habit) -> Unit,
    today: String? = null,
    modifier: Modifier = Modifier,
) {
    /*
     * Sparse representation of the table.
     *
     * There is no object for every possible cell.
     *
     * A cell is identified by:
     *
     *     habitId + date
     *
     * Missing cells are rendered as "|".
     */
    val marks = remember(dayState) {
        dayState.associate { state ->
            CellKey(
                habitId = state.habitId,
                date = state.date,
            ) to state.mark
        }
    }

    val density = LocalDensity.current

    /*
     * Fixed dimensions make the 2D virtualization very cheap.
     */
    val firstColumnWidth = 100.dp
    val headerHeight = 56.dp
    val cellWidth = 48.dp
    val cellHeight = 48.dp

    val firstColumnWidthPx =
        with(density) {
            firstColumnWidth.toPx()
        }

    val headerHeightPx =
        with(density) {
            headerHeight.toPx()
        }

    val cellWidthPx =
        with(density) {
            cellWidth.toPx()
        }

    val cellHeightPx =
        with(density) {
            cellHeight.toPx()
        }

    /*
     * Current scroll position.
     *
     * X = habits
     * Y = dates
     */
    var scrollOffset by remember(dates) {
        mutableStateOf<Offset?>(null)
    }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds(),
    ) {
        val viewportWidthPx =
            with(density) {
                maxWidth.toPx()
            }

        val viewportHeightPx =
            with(density) {
                maxHeight.toPx()
            }

        /*
         * Size of the actual scrollable body.
         */
        val bodyWidthPx =
            max(
                0f,
                viewportWidthPx -
                    firstColumnWidthPx,
            )

        val bodyHeightPx =
            max(
                0f,
                viewportHeightPx -
                    headerHeightPx,
            )

        /*
         * IMPORTANT:
         *
         * Horizontal content = habits.
         * Vertical content = dates.
         */
        val contentWidthPx =
            habits.size * cellWidthPx

        val contentHeightPx =
            dates.size * cellHeightPx

        val maxScrollX =
            max(
                0f,
                contentWidthPx -
                    bodyWidthPx,
            )

        val maxScrollY =
            max(
                0f,
                contentHeightPx -
                    bodyHeightPx,
            )

        val currentScrollOffset =
            scrollOffset ?: Offset(
                x = 0f,
                y = maxScrollY,
            )

        /*
         * Keep the current position valid after:
         *
         * - resize
         * - adding/removing habits
         * - adding/removing dates
         */
        LaunchedEffect(
            maxScrollX,
            maxScrollY,
        ) {
            val clamped = Offset(
                x = currentScrollOffset.x.coerceIn(
                    0f,
                    maxScrollX,
                ),
                y = currentScrollOffset.y.coerceIn(
                    0f,
                    maxScrollY,
                ),
            )

            if (clamped != scrollOffset) {
                scrollOffset = clamped
            }
        }

        /*
         * ============================================================
         * 2D TOUCH SCROLLING
         * ============================================================
         *
         * Do NOT use two independent `scrollable` modifiers here.
         *
         * On a touchscreen, both horizontal and vertical gestures
         * originate from the same finger drag. A single 2D drag
         * handler allows us to update both axes simultaneously.
         *
         * dragAmount:
         *
         *     +X -> finger moves right
         *     -X -> finger moves left
         *     +Y -> finger moves down
         *     -Y -> finger moves up
         *
         * Content moves in the opposite direction, therefore:
         *
         *     scrollX -= dragAmount.x
         *     scrollY -= dragAmount.y
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    maxScrollX,
                    maxScrollY,
                ) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()

                            val currentOffset = scrollOffset ?: Offset(
                                x = 0f,
                                y = maxScrollY,
                            )

                            scrollOffset = Offset(
                                x = (
                                    currentOffset.x -
                                        dragAmount.x
                                    ).coerceIn(
                                        0f,
                                        maxScrollX,
                                    ),
                                y = (
                                    currentOffset.y -
                                        dragAmount.y
                                    ).coerceIn(
                                        0f,
                                        maxScrollY,
                                    ),
                            )
                        },
                    )
                },
        ) {

            /*
             * =====================================================
             * BODY
             *
             * Both axes scroll.
             *
             * X = habits
             * Y = dates
             * =====================================================
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = firstColumnWidthPx
                                .roundToInt(),
                            y = headerHeightPx
                                .roundToInt(),
                        )
                    }
                    .width(
                        with(density) {
                            bodyWidthPx.toDp()
                        },
                    )
                    .height(
                        with(density) {
                            bodyHeightPx.toDp()
                        },
                    )
                    .clipToBounds(),
            ) {
                CalendarBodyViewport(
                    habits = habits,
                    dates = dates,
                    marks = marks,
                    scrollX = currentScrollOffset.x,
                    scrollY = currentScrollOffset.y,
                    viewportWidthPx = bodyWidthPx,
                    viewportHeightPx = bodyHeightPx,
                    cellWidthPx = cellWidthPx,
                    cellHeightPx = cellHeightPx,
                    onDayStateClick = onDayStateClick,
                    today = today,
                )
            }

            /*
             * =====================================================
             * HEADER
             *
             * Contains HABITS.
             *
             * Scrolls horizontally.
             * Stays pinned vertically.
             * =====================================================
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = firstColumnWidthPx
                                .roundToInt(),
                            y = 0,
                        )
                    }
                    .width(
                        with(density) {
                            bodyWidthPx.toDp()
                        },
                    )
                    .height(headerHeight)
                    .clipToBounds(),
            ) {
                CalendarHeaderViewport(
                    habits = habits,
                    scrollX = currentScrollOffset.x,
                    viewportWidthPx = bodyWidthPx,
                    cellWidthPx = cellWidthPx,
                    headerHeight = headerHeight,
                    headerHeightPx = headerHeightPx,
                    onHabitClick = onHabitClick,
                )
            }

            /*
             * =====================================================
             * FIRST COLUMN
             *
             * Contains DATES.
             *
             * Scrolls vertically.
             * Stays pinned horizontally.
             * =====================================================
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = headerHeightPx
                                .roundToInt(),
                        )
                    }
                    .width(firstColumnWidth)
                    .height(
                        with(density) {
                            bodyHeightPx.toDp()
                        },
                    )
                    .clipToBounds(),
            ) {
                CalendarFirstColumnViewport(
                    dates = dates,
                    scrollY = currentScrollOffset.y,
                    viewportHeightPx = bodyHeightPx,
                    firstColumnWidth = firstColumnWidth,
                    firstColumnWidthPx = firstColumnWidthPx,
                    cellHeightPx = cellHeightPx,
                    today = today,
                    habits = habits,
                )
            }

            /*
             * =====================================================
             * TOP-LEFT CORNER
             *
             * Fixed in both directions.
             * =====================================================
             */
            CalendarTableCell(
                text = "",
                width = firstColumnWidth,
                height = headerHeight,
                background =
                    currentColorScheme().surfaceVariant,
            )

            if (maxScrollY > 0f && bodyHeightPx > 0f) {
                val scrollbarWidth = 2.dp
                val minimumThumbHeightPx = with(density) {
                    24.dp.toPx()
                }
                val thumbHeightPx = min(
                    bodyHeightPx,
                    max(
                        minimumThumbHeightPx,
                        bodyHeightPx * bodyHeightPx / contentHeightPx,
                    ),
                )
                val scrollProgress =
                    (currentScrollOffset.y / maxScrollY).coerceIn(0f, 1f)
                val thumbOffsetPx =
                    (bodyHeightPx - thumbHeightPx) * scrollProgress

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = 0,
                                y = headerHeightPx.roundToInt(),
                            )
                        }
                        .width(scrollbarWidth)
                        .height(with(density) { bodyHeightPx.toDp() })
                        .background(currentColorScheme().surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { thumbHeightPx.toDp() })
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = thumbOffsetPx.roundToInt(),
                                )
                            }
                            .background(currentColorScheme().primary),
                    )
                }
            }

            if (maxScrollX > 0f && bodyWidthPx > 0f) {
                val scrollbarHeight = 2.dp
                val minimumThumbWidthPx = with(density) {
                    24.dp.toPx()
                }
                val thumbWidthPx = min(
                    bodyWidthPx,
                    max(
                        minimumThumbWidthPx,
                        bodyWidthPx * bodyWidthPx / contentWidthPx,
                    ),
                )
                val scrollProgress =
                    (currentScrollOffset.x / maxScrollX).coerceIn(0f, 1f)
                val thumbOffsetPx =
                    (bodyWidthPx - thumbWidthPx) * scrollProgress

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = firstColumnWidthPx.roundToInt(),
                                y = (
                                        viewportHeightPx - scrollbarHeight.toPx()
                                    ).roundToInt(),
                            )
                        }
                        .width(with(density) { bodyWidthPx.toDp() })
                        .height(scrollbarHeight)
                        .background(currentColorScheme().surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(with(density) { thumbWidthPx.toDp() })
                            .offset {
                                IntOffset(
                                    x = thumbOffsetPx.roundToInt(),
                                    y = 0,
                                )
                            }
                            .background(currentColorScheme().primary),
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * HEADER
 *
 * Horizontal:
 *
 *     Sample 1 | Sample 2 | Sample 3 | Sample 4
 *
 * ============================================================
 */

@Composable
private fun CalendarHeaderViewport(
    habits: List<Habit>,
    scrollX: Float,
    viewportWidthPx: Float,
    cellWidthPx: Float,
    headerHeight: Dp,
    headerHeightPx: Float,
    onHabitClick: (Habit) -> Unit,
) {
    val firstVisibleColumn =
        floor(
            scrollX / cellWidthPx,
        )
            .toInt()
            .coerceAtLeast(0)

    val lastVisibleColumn =
        ceil(
            (scrollX + viewportWidthPx) /
                cellWidthPx,
        )
            .toInt()
            .coerceAtMost(habits.size)

    /*
     * Overscan.
     */
    val startColumn =
        (firstVisibleColumn - 2)
            .coerceAtLeast(0)

    val endColumn =
        (lastVisibleColumn + 2)
            .coerceAtMost(habits.size)

    Layout(
        content = {
            for (
                column in startColumn until endColumn
            ) {
                val habit = habits[column]
                val icon = applicationIcon(habit.icon)
                val iconTint =
                    applicationColor(habit.color) ?: currentColorScheme().onSurface

                CalendarTableCell(
                    text = if (icon == null) "?" else "",
                    width = with(LocalDensity.current) {
                        cellWidthPx.toDp()
                    },
                    height = headerHeight,
                    background =
                        currentColorScheme().surfaceVariant,
                    icon = icon,
                    iconTint = iconTint,
                    textColor = iconTint,
                    contentDescription = habit.title,
                    modifier = Modifier.clickable { onHabitClick(habit) },
                )
            }
        },
    ) { measurables, constraints ->

        val cellWidth =
            cellWidthPx.roundToInt()

        val cellHeight =
            headerHeightPx.roundToInt()

        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints.fixed(
                        width = cellWidth,
                        height = cellHeight,
                    ),
                )
            }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            placeables.forEachIndexed { index, placeable ->

                val column =
                    startColumn + index

                val x =
                    column * cellWidth -
                        scrollX.roundToInt()

                placeable.place(
                    x = x,
                    y = 0,
                )
            }
        }
    }
}


/*
 * ============================================================
 * FIRST COLUMN
 *
 * Vertical:
 *
 *     2026-10-01
 *     2026-10-02
 *     2026-10-03
 *     2026-10-04
 *
 * ============================================================
 */

@Composable
private fun CalendarFirstColumnViewport(
    dates: List<String>,
    scrollY: Float,
    viewportHeightPx: Float,
    firstColumnWidth: Dp,
    firstColumnWidthPx: Float,
    cellHeightPx: Float,
    today: String?,
    habits: List<Habit>,
) {
    val firstVisibleRow =
        floor(
            scrollY / cellHeightPx,
        )
            .toInt()
            .coerceAtLeast(0)

    val lastVisibleRow =
        ceil(
            (scrollY + viewportHeightPx) /
                cellHeightPx,
        )
            .toInt()
            .coerceAtMost(dates.size)

    /*
     * Overscan.
     */
    val startRow =
        (firstVisibleRow - 2)
            .coerceAtLeast(0)

    val endRow =
        (lastVisibleRow + 2)
            .coerceAtMost(dates.size)

    Layout(
        content = {
            for (
                row in startRow until endRow
            ) {
                CalendarTableCell(
                    text = dates[row],
                    width = firstColumnWidth,
                    height = with(LocalDensity.current) {
                        cellHeightPx.toDp()
                    },
                    background = currentColorScheme().surface,
                    highlight = todayHighlight(
                        isToday = dates[row] == today,
                        left = true,
                        right = habits.isEmpty(),
                    ),
                )
            }
        },
    ) { measurables, constraints ->

        val cellWidth =
            firstColumnWidthPx.roundToInt()

        val cellHeight =
            cellHeightPx.roundToInt()

        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints.fixed(
                        width = cellWidth,
                        height = cellHeight,
                    ),
                )
            }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            placeables.forEachIndexed { index, placeable ->

                val row =
                    startRow + index

                val y =
                    row * cellHeight -
                        scrollY.roundToInt()

                placeable.place(
                    x = 0,
                    y = y,
                )
            }
        }
    }
}


/*
 * ============================================================
 * BODY
 *
 * Example:
 *
 *                 Sample 1  Sample 2  Sample 3  Sample 4
 *
 * 2026-10-01          X         |         |         |
 * 2026-10-02          |         X         X         |
 * 2026-10-03          /         X         X         |
 * 2026-10-04          |         |         |         |
 *
 * ============================================================
 */

@Composable
private fun CalendarBodyViewport(
    habits: List<Habit>,
    dates: List<String>,
    marks: Map<CellKey, String>,
    scrollX: Float,
    scrollY: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    cellWidthPx: Float,
    cellHeightPx: Float,
    onDayStateClick: (DayState) -> Unit,
    today: String?,
) {
    /*
     * X = habits / columns.
     */
    val firstVisibleColumn =
        floor(
            scrollX / cellWidthPx,
        )
            .toInt()
            .coerceAtLeast(0)

    val lastVisibleColumn =
        ceil(
            (scrollX + viewportWidthPx) /
                cellWidthPx,
        )
            .toInt()
            .coerceAtMost(habits.size)

    /*
     * Y = dates / rows.
     */
    val firstVisibleRow =
        floor(
            scrollY / cellHeightPx,
        )
            .toInt()
            .coerceAtLeast(0)

    val lastVisibleRow =
        ceil(
            (scrollY + viewportHeightPx) /
                cellHeightPx,
        )
            .toInt()
            .coerceAtMost(dates.size)

    /*
     * Overscan.
     */
    val startColumn =
        (firstVisibleColumn - 2)
            .coerceAtLeast(0)

    val endColumn =
        (lastVisibleColumn + 2)
            .coerceAtMost(habits.size)

    val startRow =
        (firstVisibleRow - 2)
            .coerceAtLeast(0)

    val endRow =
        (lastVisibleRow + 2)
            .coerceAtMost(dates.size)

    val columnCount =
        endColumn - startColumn

    val rowCount =
        endRow - startRow

    /*
     * Only visible dates × visible habits are composed.
     */
    Layout(
        content = {

            for (
                row in startRow until endRow
            ) {
                val date =
                    dates[row]

                for (
                    column in startColumn until endColumn
                ) {
                    val habit =
                        habits[column]
                    val backgroundColor =
                        applicationColor(habit.color)?.copy(alpha = 0.05f)
                            ?: currentColorScheme().surface

                    val key = CellKey(
                            habitId = habit.id,
                            date = date,
                        )
                    val value = marks[key] ?: " "
                    val status = marks[key]
                        ?.takeIf { mark -> statusOptions.any { it.mark == mark } }
                        ?: "·"

                    CalendarTableCell(
                        text = value,
                        width = with(LocalDensity.current) {
                            cellWidthPx.toDp()
                        },
                        height = with(LocalDensity.current) {
                            cellHeightPx.toDp()
                        },
                        background = backgroundColor,
                        highlight = todayHighlight(
                            isToday = date == today,
                            left = false,
                            right = column == habits.lastIndex,
                        ),
                        modifier = Modifier
                            .clickable {
                                onDayStateClick(
                                    DayState(
                                        habitId = habit.id,
                                        date = date,
                                        mark = status,
                                    ),
                                )
                            }
                            .semantics {
                                contentDescription = "${habit.title}, $date, $value"
                            },
                    )
                }
            }
        },
    ) { measurables, constraints ->

        val cellWidth =
            cellWidthPx.roundToInt()

        val cellHeight =
            cellHeightPx.roundToInt()

        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints.fixed(
                        width = cellWidth,
                        height = cellHeight,
                    ),
                )
            }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {

            var index = 0

            for (
                rowOffset in 0 until rowCount
            ) {
                val row =
                    startRow + rowOffset

                val y =
                    row * cellHeight -
                        scrollY.roundToInt()

                for (
                    columnOffset in 0 until columnCount
                ) {
                    val column =
                        startColumn + columnOffset

                    val x =
                        column * cellWidth -
                            scrollX.roundToInt()

                    placeables[index++].place(
                        x = x,
                        y = y,
                    )
                }
            }
        }
    }
}

/*
 * ============================================================
 * CELL
 * ============================================================
 */

@Composable
private fun CalendarTableCell(
    text: String,
    width: Dp,
    height: Dp,
    background: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: androidx.compose.ui.graphics.Color = currentColorScheme().onSurface,
    textColor: androidx.compose.ui.graphics.Color = currentColorScheme().onSurface,
    contentDescription: String? = null,
    highlight: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(background)
            .border(
                width = 1.dp,
                color =
                    currentColorScheme().outlineVariant,
            )
            .then(highlight),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
            )
        } else {
            Text(
                text = text,
                modifier = if (contentDescription == null) {
                    Modifier
                } else {
                    Modifier.semantics {
                        this.contentDescription = contentDescription
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

/*
 * Draws one continuous outline around the current day's row.
 */
@Composable
private fun todayHighlight(
    isToday: Boolean,
    left: Boolean,
    right: Boolean,
): Modifier {
    if (!isToday) return Modifier
    val color = currentColorScheme().primary
    return Modifier.drawWithContent {
        drawContent()
        val stroke = 2.dp.toPx()
        val half = stroke / 2
        drawLine(color, Offset(0f, half), Offset(size.width, half), stroke)
        drawLine(
            color,
            Offset(0f, size.height - half),
            Offset(size.width, size.height - half),
            stroke,
        )
        if (left) drawLine(color, Offset(half, 0f), Offset(half, size.height), stroke)
        if (right) {
            drawLine(
                color,
                Offset(size.width - half, 0f),
                Offset(size.width - half, size.height),
                stroke,
            )
        }
    }
}
