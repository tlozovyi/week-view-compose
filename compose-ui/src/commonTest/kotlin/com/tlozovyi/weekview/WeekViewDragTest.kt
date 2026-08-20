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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class WeekViewDragTest {

    @Test
    fun sanitizeEventStartSnapsToNearestQuarterHour() {
        val raw = LocalDate(2026, 8, 20).atTime(10, 7)
        assertEquals(LocalDate(2026, 8, 20).atTime(10, 0), sanitizeEventStartToQuarterHour(raw))

        val rawNearNext = LocalDate(2026, 8, 20).atTime(10, 8)
        assertEquals(LocalDate(2026, 8, 20).atTime(10, 15), sanitizeEventStartToQuarterHour(rawNearNext))
    }

    @Test
    fun calculateNewEventStartUsesDragDelta() {
        val dragState = WeekViewDragState(
            eventId = 1,
            event = sampleEvent(id = 1, hour = 10),
            sourceChip = sampleChip(sampleEvent(id = 1, hour = 10)),
            draggedEventStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
            dragStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
            currentStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
            currentEndTime = LocalDate(2026, 8, 20).atTime(11, 0),
        )

        val newLocation = LocalDate(2026, 8, 20).atTime(11, 30)
        assertEquals(
            LocalDate(2026, 8, 20).atTime(11, 30),
            calculateNewEventStart(dragState, newLocation),
        )
    }

    @Test
    fun calculateTimeFromPointMapsCanvasCoordinatesToDateTime() {
        val layout = sampleLayout(dayWidthPx = 100f, hourHeightPx = 50f)
        val style = WeekViewStyle(minHour = 8, maxHour = 12, numberOfVisibleDays = 1)

        val time = calculateTimeFromPoint(
            touchX = 40f,
            touchY = 100f,
            layout = layout,
            style = style,
            horizontalTranslationPx = 0f,
        )

        assertEquals(LocalDate(2026, 8, 20).atTime(10, 0), time)
    }

    @Test
    fun detectDragScrollEdgeFindsViewportEdges() {
        assertEquals(
            DragScrollEdge.Top,
            detectDragScrollEdge(
                touchXInCanvasPx = 100f,
                touchYInCanvasPx = 150f,
                gridScrollOffsetPx = 100f,
                gridViewportWidthPx = 300f,
                gridViewportHeightPx = 400f,
                scrollThresholdPx = 80f,
            ),
        )
        assertEquals(
            DragScrollEdge.Bottom,
            detectDragScrollEdge(
                touchXInCanvasPx = 100f,
                touchYInCanvasPx = 470f,
                gridScrollOffsetPx = 100f,
                gridViewportWidthPx = 300f,
                gridViewportHeightPx = 400f,
                scrollThresholdPx = 80f,
            ),
        )
        assertEquals(
            DragScrollEdge.Left,
            detectDragScrollEdge(
                touchXInCanvasPx = 50f,
                touchYInCanvasPx = 200f,
                gridScrollOffsetPx = 0f,
                gridViewportWidthPx = 300f,
                gridViewportHeightPx = 400f,
                scrollThresholdPx = 80f,
            ),
        )
        assertEquals(
            DragScrollEdge.Right,
            detectDragScrollEdge(
                touchXInCanvasPx = 250f,
                touchYInCanvasPx = 200f,
                gridScrollOffsetPx = 0f,
                gridViewportWidthPx = 300f,
                gridViewportHeightPx = 400f,
                scrollThresholdPx = 80f,
            ),
        )
    }

    @Test
    fun detectDragScrollEdgeIgnoresHorizontalBufferTranslationAtViewportCenter() {
        assertEquals(
            DragScrollEdge.None,
            detectDragScrollEdge(
                touchXInCanvasPx = 150f,
                touchYInCanvasPx = 200f,
                gridScrollOffsetPx = 0f,
                gridViewportWidthPx = 300f,
                gridViewportHeightPx = 400f,
                scrollThresholdPx = 80f,
            ),
        )
    }

    @Test
    fun applyVerticalDragAutoScrollMovesGridAndEventTime() {
        val result = applyVerticalDragAutoScroll(
            edge = DragScrollEdge.Top,
            gridScrollOffsetPx = 200f,
            maxGridScrollOffsetPx = 500f,
            hourHeightPx = 100f,
            currentStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
            durationInMinutes = 60,
        )

        assertEquals(175f, result?.first)
        assertEquals(LocalDate(2026, 8, 20).atTime(9, 45), result?.second)
        assertEquals(LocalDate(2026, 8, 20).atTime(10, 45), result?.third)
    }

    @Test
    fun applyVerticalDragAutoScrollStopsAtTop() {
        assertNull(
            applyVerticalDragAutoScroll(
                edge = DragScrollEdge.Top,
                gridScrollOffsetPx = 0f,
                maxGridScrollOffsetPx = 500f,
                hourHeightPx = 100f,
                currentStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
                durationInMinutes = 60,
            ),
        )
    }

    @Test
    fun applyHorizontalDragAutoScrollShiftsByOneDay() {
        val result = applyHorizontalDragAutoScroll(
            edge = DragScrollEdge.Right,
            currentStartTime = LocalDate(2026, 8, 20).atTime(10, 0),
            durationInMinutes = 30,
        )

        assertEquals(LocalDate(2026, 8, 21).atTime(10, 0), result?.first)
        assertEquals(LocalDate(2026, 8, 21).atTime(10, 30), result?.second)
    }
}

private fun sampleEvent(id: Long, hour: Int): WeekViewEvent {
    val date = LocalDate(2026, 8, 20)
    return WeekViewEvent(
        id = id,
        title = "Event $id",
        startTime = date.atTime(hour, 0),
        endTime = date.atTime(hour + 1, 0),
    )
}

private fun sampleChip(event: WeekViewEvent): EventChip {
    return EventChip(
        event = ResolvedWeekViewEntity.Event(
            id = event.id,
            title = event.title,
            startTime = event.startTime,
            endTime = event.endTime,
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = event,
        ),
        index = 0,
        startTime = event.startTime,
        endTime = event.endTime,
    )
}

private fun sampleLayout(dayWidthPx: Float, hourHeightPx: Float): WeekViewLayout {
    val date = LocalDate(2026, 8, 20)
    return WeekViewLayout(
        viewportWidthPx = dayWidthPx,
        viewportHeightPx = 400f,
        timeColumnWidthPx = 0f,
        headerHeightPx = 0f,
        hourHeightPx = hourHeightPx,
        dayWidthPx = dayWidthPx,
        columnGapPx = 0f,
        viewportGridWidthPx = dayWidthPx,
        contentGridWidthPx = dayWidthPx,
        gridHeightPx = hourHeightPx * 4,
        visibleDates = listOf(date),
        renderDates = listOf(date),
        scrollBufferDays = 0,
    )
}
