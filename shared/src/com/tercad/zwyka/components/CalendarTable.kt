package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

import com.tercad.zwyka.data.CellKey
import com.tercad.zwyka.data.DayState
import com.tercad.zwyka.data.Habit


@Composable
fun CalendarTable(
    habits: List<Habit>,
    dates: List<String>,
    dayState: List<DayState>,
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
    val cellWidth = 120.dp
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
    var scrollOffset by remember {
        mutableStateOf(Offset.Zero)
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
                x = scrollOffset.x.coerceIn(
                    0f,
                    maxScrollX,
                ),
                y = scrollOffset.y.coerceIn(
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

                            scrollOffset = Offset(
                                x = (
                                    scrollOffset.x -
                                        dragAmount.x
                                    ).coerceIn(
                                        0f,
                                        maxScrollX,
                                    ),
                                y = (
                                    scrollOffset.y -
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
                    scrollX = scrollOffset.x,
                    scrollY = scrollOffset.y,
                    viewportWidthPx = bodyWidthPx,
                    viewportHeightPx = bodyHeightPx,
                    cellWidthPx = cellWidthPx,
                    cellHeightPx = cellHeightPx,
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
                    scrollX = scrollOffset.x,
                    viewportWidthPx = bodyWidthPx,
                    cellWidthPx = cellWidthPx,
                    headerHeight = headerHeight,
                    headerHeightPx = headerHeightPx,
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
                    scrollY = scrollOffset.y,
                    viewportHeightPx = bodyHeightPx,
                    firstColumnWidth = firstColumnWidth,
                    firstColumnWidthPx = firstColumnWidthPx,
                    cellHeightPx = cellHeightPx,
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
                    MaterialTheme.colorScheme.surfaceVariant,
            )
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
                CalendarTableCell(
                    text = habits[column].title,
                    width = with(LocalDensity.current) {
                        cellWidthPx.toDp()
                    },
                    height = headerHeight,
                    background =
                        MaterialTheme.colorScheme.surfaceVariant,
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
                    background =
                        MaterialTheme.colorScheme.surface,
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

                    val value =
                        marks[
                            CellKey(
                                habitId = habit.id,
                                date = date,
                            )
                        ] ?: "|"

                    CalendarTableCell(
                        text = value,
                        width = with(LocalDensity.current) {
                            cellWidthPx.toDp()
                        },
                        height = with(LocalDensity.current) {
                            cellHeightPx.toDp()
                        },
                        background =
                            MaterialTheme.colorScheme.surface,
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
                    MaterialTheme.colorScheme
                        .outlineVariant,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurface,
        )
    }
}
