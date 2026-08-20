/*
 * Copyright 2026 Taras Lozovyi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlozovyi.weekview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Computes the vertical scroll offset that centers [today]'s current time in the grid viewport. */
internal fun scrollOffsetForCurrentTime(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    today: LocalDate,
    gridViewportHeightPx: Float? = null,
): Float? {
    if (!layout.visibleDates.contains(today)) {
        return null
    }

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    if (now.hour < style.minHour || now.hour >= style.maxHour) {
        return null
    }

    val y = layout.hourY(now.hour, style.minHour) + (now.minute / 60f) * layout.hourHeightPx
    val viewportHeight = gridViewportHeightPx
        ?: (layout.viewportHeightPx - layout.headerHeightPx).coerceAtLeast(0f)
    return (y - viewportHeight / 2f).coerceAtLeast(0f)
}

/** Repeatedly scrolls the grid and shifts the dragged event while the pointer stays near an edge. */
@Composable
internal fun WeekViewDragAutoScrollEffect(
    isDragging: Boolean,
    dragScrollEdge: DragScrollEdge,
    dragState: WeekViewDragState?,
    gridScrollOffsetPx: Float,
    maxGridScrollOffsetPx: () -> Float,
    hourHeightPx: Float,
    clampGridScrollOffsetPx: (Float) -> Float,
    onGridScrollOffsetChange: (Float) -> Unit,
    onDragStateChange: (WeekViewDragState?) -> Unit,
    onDragScrollEdgeChange: (DragScrollEdge) -> Unit,
    anchorDate: LocalDate,
    onAnchorDateChange: (LocalDate) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
) {
    LaunchedEffect(isDragging, dragScrollEdge, dragState?.eventId) {
        if (!isDragging || dragScrollEdge == DragScrollEdge.None || dragState == null) {
            return@LaunchedEffect
        }

        while (isActive && dragState != null && dragScrollEdge != DragScrollEdge.None) {
            val state = dragState ?: break
            val edge = dragScrollEdge
            val delayMillis = dragScrollDelayMillis(edge)
            if (delayMillis <= 0L) {
                break
            }

            when (edge) {
                DragScrollEdge.Top, DragScrollEdge.Bottom -> {
                    val result = applyVerticalDragAutoScroll(
                        edge = edge,
                        gridScrollOffsetPx = gridScrollOffsetPx,
                        maxGridScrollOffsetPx = maxGridScrollOffsetPx(),
                        hourHeightPx = hourHeightPx,
                        currentStartTime = state.currentStartTime,
                        durationInMinutes = state.durationInMinutes,
                    )
                    if (result == null) {
                        onDragScrollEdgeChange(DragScrollEdge.None)
                        break
                    }
                    val (newScrollOffsetPx, newStart, newEnd) = result
                    onGridScrollOffsetChange(clampGridScrollOffsetPx(newScrollOffsetPx))
                    onDragStateChange(
                        state.copy(
                            currentStartTime = newStart,
                            currentEndTime = newEnd,
                        ),
                    )
                }
                DragScrollEdge.Left, DragScrollEdge.Right -> {
                    val result = applyHorizontalDragAutoScroll(
                        edge = edge,
                        currentStartTime = state.currentStartTime,
                        durationInMinutes = state.durationInMinutes,
                    )
                    if (result == null) {
                        onDragScrollEdgeChange(DragScrollEdge.None)
                        break
                    }
                    val (newStart, newEnd) = result
                    onDragStateChange(
                        state.copy(
                            currentStartTime = newStart,
                            currentEndTime = newEnd,
                        ),
                    )
                    val dayDelta = if (edge == DragScrollEdge.Left) -1 else 1
                    val newAnchorDate = anchorDate.plusDays(dayDelta)
                    if (newAnchorDate != anchorDate) {
                        onAnchorGenerationBump()
                    }
                    onAnchorDateChange(newAnchorDate)
                    onFirstVisibleDateChange?.invoke(newAnchorDate)
                }
                DragScrollEdge.None -> Unit
            }

            delay(delayMillis)
        }
    }
}

internal data class WeekViewPinchScrollOps(
    val layoutGridHeightForHourHeight: (Float) -> Float,
    val maxGridScrollOffsetPx: () -> Float,
    val clampGridScrollOffsetPx: (Float) -> Float,
    val pinchScrollForHourHeight: (Float) -> Float,
)

internal fun createPinchScrollOps(
    style: WeekViewStyle,
    density: androidx.compose.ui.unit.Density,
    gridViewportHeightPx: Float,
    pinchBaselineScrollOffsetPx: () -> Float,
    pinchBaselineLayoutGridHeightPx: () -> Float,
    pinchBaselineFocalY: () -> Float,
    hourHeightPx: Float,
): WeekViewPinchScrollOps {
    fun layoutGridHeightForHourHeight(hourHeight: Float): Float {
        return with(density) { layoutHeightPx(style.hoursCount * hourHeight) }
    }

    fun maxGridScrollOffsetPx(hourHeight: Float = hourHeightPx): Float {
        return maxVerticalScrollOffsetPx(
            gridHeightPx = layoutGridHeightForHourHeight(hourHeight),
            viewportGridHeightPx = gridViewportHeightPx,
        )
    }

    fun clampGridScrollOffsetPx(scrollOffsetPx: Float, hourHeight: Float = hourHeightPx): Float {
        return scrollOffsetPx.coerceIn(0f, maxGridScrollOffsetPx(hourHeight))
    }

    fun pinchScrollForHourHeight(hourHeight: Float): Float {
        val layoutGridHeightPx = layoutGridHeightForHourHeight(hourHeight)
        val rawScrollOffsetPx = scrollOffsetForLayoutGridZoomAtFocalPoint(
            baselineScrollOffsetPx = pinchBaselineScrollOffsetPx(),
            baselineLayoutGridHeightPx = pinchBaselineLayoutGridHeightPx(),
            newLayoutGridHeightPx = layoutGridHeightPx,
            focalYInViewportPx = pinchBaselineFocalY(),
        )
        return clampGridScrollOffsetPx(rawScrollOffsetPx, hourHeight)
    }

    return WeekViewPinchScrollOps(
        layoutGridHeightForHourHeight = ::layoutGridHeightForHourHeight,
        maxGridScrollOffsetPx = { maxGridScrollOffsetPx() },
        clampGridScrollOffsetPx = { scrollOffsetPx -> clampGridScrollOffsetPx(scrollOffsetPx) },
        pinchScrollForHourHeight = ::pinchScrollForHourHeight,
    )
}
