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
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import kotlinx.datetime.LocalDate

@Composable
internal fun rememberDragGhostChip(
    dragState: WeekViewDragState?,
    displayGridLayout: WeekViewLayout,
    layoutConfig: WeekViewLayoutConfig,
    style: WeekViewStyle,
    density: Density,
): EventChip? {
    return remember(dragState, displayGridLayout, layoutConfig, density, style) {
        val state = dragState ?: return@remember null
        val dateIndex = displayGridLayout.dateIndex(state.currentStartTime.date)
        if (dateIndex < 0) {
            return@remember null
        }

        val dayStartX = displayGridLayout.dayStartX(dateIndex)
        val modifiedDayStartX = if (style.numberOfVisibleDays == 1) {
            dayStartX + style.singleDayHorizontalPaddingDp.toPx(density)
        } else {
            dayStartX
        }

        val calculator = EventChipBoundsCalculator(displayGridLayout, style, density)
        val bounds = calculator.calculateDraggedEvent(
            dayStartX = modifiedDayStartX,
            startTime = state.currentStartTime,
            durationInMinutes = state.durationInMinutes,
            layoutConfig = layoutConfig,
        )

        state.sourceChip.copy(
            startTime = state.currentStartTime,
            endTime = state.currentEndTime,
        ).apply {
            this.bounds = bounds
        }
    }
}

internal fun WeekViewGestureScope.bindHorizontalScrollGestures(
    dragStateProvider: () -> WeekViewDragState?,
    isHorizontalSnappingProvider: () -> Boolean,
    dayWidthPx: Float,
    anchorDateProvider: () -> LocalDate,
    horizontalScrollOffsetProvider: () -> Float,
    isLtr: Boolean = true,
    onHorizontalScrollOffsetChange: (Float) -> Unit,
    onAnchorDateChange: (LocalDate) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onHorizontalScrollSnapRequest: () -> Unit,
    onHorizontalScrollStart: () -> Unit,
) {
    isScrollBlocked = {
        dragStateProvider() != null || isHorizontalSnappingProvider()
    }
    isPinchBlocked = { dragStateProvider() != null }
    this.onHorizontalScrollStart = onHorizontalScrollStart
    onHorizontalDrag = { delta ->
        onHorizontalScrollOffsetChange(
            applyHorizontalScrollDelta(
                offsetPx = horizontalScrollOffsetProvider(),
                deltaPx = delta,
                dayWidthPx = dayWidthPx,
                firstVisibleDate = anchorDateProvider(),
                onFirstVisibleDateChange = { newDate ->
                    if (newDate != anchorDateProvider()) {
                        onAnchorGenerationBump()
                    }
                    onAnchorDateChange(newDate)
                },
                isLtr = isLtr,
            ),
        )
    }
    onHorizontalScrollEnd = onHorizontalScrollSnapRequest
}

internal fun WeekViewGestureScope.bindEventDragGestures(
    eventChipsProvider: () -> List<EventChip>,
    horizontalTranslationPxProvider: () -> Float,
    displayGridLayoutProvider: () -> WeekViewLayout,
    styleProvider: () -> WeekViewStyle,
    tapEnabled: Boolean,
    longPressEnabled: Boolean,
    dragEnabled: Boolean,
    onEventClick: ((WeekViewEvent) -> Unit)?,
    onEmptyViewClick: ((kotlinx.datetime.LocalDateTime) -> Unit)?,
    onEmptyViewLongClick: ((kotlinx.datetime.LocalDateTime) -> Unit)?,
    onEventLongClick: ((WeekViewEvent) -> Boolean)?,
    dragStateProvider: () -> WeekViewDragState?,
    onDragStateChange: (WeekViewDragState?) -> Unit,
    onDragScrollEdgeChange: (DragScrollEdge) -> Unit,
    gridScrollOffsetPxProvider: () -> Float,
    gridViewportWidthPx: Float,
    gridViewportHeightPx: Float,
    onEventDrop: ((WeekViewEvent, kotlinx.datetime.LocalDateTime, kotlinx.datetime.LocalDateTime) -> Unit)?,
) {
    eventChips = eventChipsProvider()
    horizontalTranslationPx = horizontalTranslationPxProvider()
    displayGridLayout = displayGridLayoutProvider()
    style = styleProvider()
    this.tapEnabled = tapEnabled
    this.longPressEnabled = longPressEnabled
    this.dragEnabled = dragEnabled
    this.onEventClick = onEventClick
    this.onEmptyViewClick = onEmptyViewClick
    this.onEmptyViewLongClick = onEmptyViewLongClick
    this.onEventLongClick = onEventLongClick
    onDragStart = start@{ event, offset ->
        val chips = eventChipsProvider()
        val chip = chips.firstOrNull { it.eventId == event.id } ?: return@start
        val layout = displayGridLayoutProvider()
        val dragStartTime = calculateTimeFromPoint(
            touchX = offset.x,
            touchY = offset.y,
            layout = layout,
            style = styleProvider(),
            horizontalTranslationPx = horizontalTranslationPxProvider(),
        ) ?: event.startTime
        onDragStateChange(
            WeekViewDragState(
                eventId = event.id,
                event = event,
                sourceChip = chip,
                draggedEventStartTime = event.startTime,
                dragStartTime = dragStartTime,
                currentStartTime = event.startTime,
                currentEndTime = event.endTime,
            ),
        )
    }
    onDragMove = move@{ offset ->
        val state = dragStateProvider() ?: return@move
        val currentLocation = calculateTimeFromPoint(
            touchX = offset.x,
            touchY = offset.y,
            layout = displayGridLayoutProvider(),
            style = styleProvider(),
            horizontalTranslationPx = horizontalTranslationPxProvider(),
        ) ?: return@move

        val newStart = sanitizeEventStartToQuarterHour(
            calculateNewEventStart(state, currentLocation),
        )
        val (startTime, endTime) = eventTimesForDraggedStart(
            originalStartTime = state.draggedEventStartTime,
            originalEndTime = state.event.endTime,
            sanitizedStartTime = newStart,
        )
        onDragStateChange(
            state.copy(
                currentStartTime = startTime,
                currentEndTime = endTime,
            ),
        )
        onDragScrollEdgeChange(
            detectDragScrollEdge(
                touchXInCanvasPx = offset.x,
                touchYInCanvasPx = offset.y,
                gridScrollOffsetPx = gridScrollOffsetPxProvider(),
                gridViewportWidthPx = gridViewportWidthPx,
                gridViewportHeightPx = gridViewportHeightPx,
            ),
        )
    }
    onDragEnd = {
        val state = dragStateProvider()
        onDragStateChange(null)
        onDragScrollEdgeChange(DragScrollEdge.None)
        if (state != null) {
            onEventDrop?.invoke(
                state.event,
                state.currentStartTime,
                state.currentEndTime,
            )
        }
    }
    onDragCancel = {
        onDragStateChange(null)
        onDragScrollEdgeChange(DragScrollEdge.None)
    }
}
