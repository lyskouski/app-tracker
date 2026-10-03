package com.tercad.zwyka.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
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
     * Sparse representation:
     *
     * We don't create objects for all possible cells.
     *
     * For example:
     *
     * 10,000 habits × 10,000 dates
     *
     * means 100,000,000 possible cells,
     * but only entries existing in dayState
     * are stored here.
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

    val firstColumnWidth = 180.dp
    val headerHeight = 56.dp
    val cellWidth = 72.dp
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
     * Scroll position in pixels.
     *
     * 0,0 = top-left.
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
         * Area occupied by the scrollable body.
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
         * Total scrollable content dimensions.
         */
        val contentWidthPx =
            dates.size * cellWidthPx

        val contentHeightPx =
            habits.size * cellHeightPx

        /*
         * Maximum scroll positions.
         */
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
         * If the window is resized, or the number of rows/
         * columns changes, keep the current position valid.
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
         * Horizontal scrolling.
         *
         * Modifier.scrollable reports the physical gesture
         * delta. We use that delta directly as the scroll
         * position, exactly as Compose's custom scroll examples
         * do.
         */
        val horizontalScrollState =
            rememberScrollableState { delta ->

                val oldX = scrollOffset.x

                val newX =
                    (oldX + delta)
                        .coerceIn(
                            0f,
                            maxScrollX,
                        )

                scrollOffset = Offset(
                    x = newX,
                    y = scrollOffset.y,
                )

                /*
                 * Return the amount actually consumed.
                 */
                newX - oldX
            }

        /*
         * Vertical scrolling.
         */
        val verticalScrollState =
            rememberScrollableState { delta ->

                val oldY = scrollOffset.y

                val newY =
                    (oldY + delta)
                        .coerceIn(
                            0f,
                            maxScrollY,
                        )

                scrollOffset = Offset(
                    x = scrollOffset.x,
                    y = newY,
                )

                /*
                 * Return the amount actually consumed.
                 */
                newY - oldY
            }

        /*
         * The outer container receives both horizontal and
         * vertical gestures.
         *
         * We don't use horizontalScroll()/verticalScroll()
         * because those would require a conventional scrollable
         * content hierarchy. Here the content is virtualized
         * manually.
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(
                    state = horizontalScrollState,
                    orientation = Orientation.Horizontal,
                )
                .scrollable(
                    state = verticalScrollState,
                    orientation = Orientation.Vertical,
                ),
        ) {

            /*
             * -------------------------------------------------
             * 1. BODY
             *
             * Scrolls in BOTH directions.
             * -------------------------------------------------
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = firstColumnWidthPx.roundToInt(),
                            y = headerHeightPx.roundToInt(),
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
             * -------------------------------------------------
             * 2. HEADER
             *
             * Scrolls horizontally.
             * Stays pinned vertically.
             * -------------------------------------------------
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = firstColumnWidthPx.roundToInt(),
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
                    dates = dates,
                    scrollX = scrollOffset.x,
                    viewportWidthPx = bodyWidthPx,
                    cellWidthPx = cellWidthPx,
                    headerHeight = headerHeight,
                    headerHeightPx = headerHeightPx,
                )
            }

            /*
             * -------------------------------------------------
             * 3. FIRST COLUMN
             *
             * Scrolls vertically.
             * Stays pinned horizontally.
             * -------------------------------------------------
             */
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = headerHeightPx.roundToInt(),
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
                    habits = habits,
                    scrollY = scrollOffset.y,
                    viewportHeightPx = bodyHeightPx,
                    firstColumnWidth = firstColumnWidth,
                    firstColumnWidthPx = firstColumnWidthPx,
                    cellHeightPx = cellHeightPx,
                )
            }

            /*
             * -------------------------------------------------
             * 4. TOP-LEFT CORNER
             *
             * Never scrolls.
             * -------------------------------------------------
             */
            CalendarTableCell(
                text = "",
                width = firstColumnWidth,
                height = headerHeight,
                background =
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier,
            )
        }
    }
}


/*
 * ============================================================
 * HEADER
 * ============================================================
 */

@Composable
private fun CalendarHeaderViewport(
    dates: List<String>,
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
            .coerceAtMost(dates.size)

    /*
     * Compose a few cells outside the viewport.
     */
    val startColumn =
        (firstVisibleColumn - 2)
            .coerceAtLeast(0)

    val endColumn =
        (lastVisibleColumn + 2)
            .coerceAtMost(dates.size)

    Layout(
        content = {

            for (
                column in startColumn until endColumn
            ) {
                CalendarTableCell(
                    text = dates[column],
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
 * ============================================================
 */

@Composable
private fun CalendarFirstColumnViewport(
    habits: List<Habit>,
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
            .coerceAtMost(habits.size)

    val startRow =
        (firstVisibleRow - 2)
            .coerceAtLeast(0)

    val endRow =
        (lastVisibleRow + 2)
            .coerceAtMost(habits.size)

    Layout(
        content = {

            for (
                row in startRow until endRow
            ) {
                CalendarTableCell(
                    text = habits[row].title,
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
            .coerceAtMost(dates.size)

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
            .coerceAtMost(habits.size)

    /*
     * Overscan.
     */
    val startColumn =
        (firstVisibleColumn - 2)
            .coerceAtLeast(0)

    val endColumn =
        (lastVisibleColumn + 2)
            .coerceAtMost(dates.size)

    val startRow =
        (firstVisibleRow - 2)
            .coerceAtLeast(0)

    val endRow =
        (lastVisibleRow + 2)
            .coerceAtMost(habits.size)

    val columnCount =
        endColumn - startColumn

    val rowCount =
        endRow - startRow

    /*
     * Compose ONLY:
     *
     * visible rows × visible columns
     *
     * plus the small overscan area.
     */
    Layout(
        content = {

            for (
                row in startRow until endRow
            ) {
                val habit =
                    habits[row]

                for (
                    column in startColumn until endColumn
                ) {
                    val date =
                        dates[column]

                    /*
                     * Missing cells are represented by "|".
                     */
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
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
