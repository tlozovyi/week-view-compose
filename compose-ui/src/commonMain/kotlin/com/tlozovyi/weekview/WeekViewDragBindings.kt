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
    dayWidthPx: Float,
    anchorDateProvider: () -> LocalDate,
    horizontalScrollOffsetProvider: () -> Float,
    onHorizontalScrollOffsetChange: (Float) -> Unit,
    onAnchorDateChange: (LocalDate) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
) {
    isScrollBlocked = { dragStateProvider() != null }
    isPinchBlocked = { dragStateProvider() != null }
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
                    onFirstVisibleDateChange?.invoke(newDate)
                },
            ),
        )
    }
}

internal fun WeekViewGestureScope.bindEventDragGestures(
    eventChipsProvider: () -> List<EventChip>,
    horizontalTranslationPxProvider: () -> Float,
    onEventClickHandler: (WeekViewEvent) -> Unit,
    dragStateProvider: () -> WeekViewDragState?,
    onDragStateChange: (WeekViewDragState?) -> Unit,
    onDragScrollEdgeChange: (DragScrollEdge) -> Unit,
    displayGridLayout: WeekViewLayout,
    style: WeekViewStyle,
    gridScrollOffsetPxProvider: () -> Float,
    gridViewportWidthPx: Float,
    gridViewportHeightPx: Float,
    onEventDrop: ((WeekViewEvent, kotlinx.datetime.LocalDateTime, kotlinx.datetime.LocalDateTime) -> Unit)?,
) {
    eventChips = eventChipsProvider()
    horizontalTranslationPx = horizontalTranslationPxProvider()
    onEventClick = onEventClickHandler
    onDragStart = start@{ event, offset ->
        val chips = eventChipsProvider()
        val chip = chips.firstOrNull { it.eventId == event.id } ?: return@start
        val dragStartTime = calculateTimeFromPoint(
            touchX = offset.x,
            touchY = offset.y,
            layout = displayGridLayout,
            style = style,
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
            layout = displayGridLayout,
            style = style,
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
