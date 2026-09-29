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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class WeekViewGridTouchTest {

    @Test
    fun isTouchInSystemGestureEdge_respectsLayoutDirection() {
        assertTrue(
            isTouchInSystemGestureEdge(
                touchX = 10f,
                layoutWidthPx = 400f,
                leftGestureInsetPx = 24f,
                rightGestureInsetPx = 24f,
                isRtl = false,
            ),
        )
        assertFalse(
            isTouchInSystemGestureEdge(
                touchX = 100f,
                layoutWidthPx = 400f,
                leftGestureInsetPx = 24f,
                rightGestureInsetPx = 24f,
                isRtl = false,
            ),
        )
        assertTrue(
            isTouchInSystemGestureEdge(
                touchX = 390f,
                layoutWidthPx = 400f,
                leftGestureInsetPx = 24f,
                rightGestureInsetPx = 24f,
                isRtl = true,
            ),
        )
    }

    @Test
    fun shouldCancelGridTapWait_whenSecondPointerIsDownOrTapBlocked() {
        assertTrue(shouldCancelGridTapWait(pressedPointerCount = 2, tapBlocked = false))
        assertTrue(shouldCancelGridTapWait(pressedPointerCount = 1, tapBlocked = true))
        assertFalse(shouldCancelGridTapWait(pressedPointerCount = 1, tapBlocked = false))
    }

    @Test
    fun shouldCancelGridLongPressWait_whenPointerMovedBeyondSlopOrScrollChanged() {
        val down = Offset(100f, 200f)
        val touchSlop = 8f
        assertFalse(
            shouldCancelGridLongPressWait(
                downPosition = down,
                currentPosition = down + Offset(5f, 0f),
                touchSlop = touchSlop,
                pressedPointerCount = 1,
                tapBlocked = false,
                initialGridScrollOffsetPx = 0f,
                currentGridScrollOffsetPx = 0f,
                initialHorizontalScrollOffsetPx = 0f,
                currentHorizontalScrollOffsetPx = 0f,
            ),
        )
        assertTrue(
            shouldCancelGridLongPressWait(
                downPosition = down,
                currentPosition = down + Offset(touchSlop + 1f, 0f),
                touchSlop = touchSlop,
                pressedPointerCount = 1,
                tapBlocked = false,
                initialGridScrollOffsetPx = 0f,
                currentGridScrollOffsetPx = 0f,
                initialHorizontalScrollOffsetPx = 0f,
                currentHorizontalScrollOffsetPx = 0f,
            ),
        )
        assertTrue(
            shouldCancelGridLongPressWait(
                downPosition = down,
                currentPosition = down,
                touchSlop = touchSlop,
                pressedPointerCount = 1,
                tapBlocked = false,
                initialGridScrollOffsetPx = 0f,
                currentGridScrollOffsetPx = 40f,
                initialHorizontalScrollOffsetPx = 0f,
                currentHorizontalScrollOffsetPx = 0f,
            ),
        )
        assertTrue(
            shouldCancelGridLongPressWait(
                downPosition = down,
                currentPosition = down,
                touchSlop = touchSlop,
                pressedPointerCount = 1,
                tapBlocked = false,
                initialGridScrollOffsetPx = 0f,
                currentGridScrollOffsetPx = 0f,
                initialHorizontalScrollOffsetPx = 10f,
                currentHorizontalScrollOffsetPx = 25f,
            ),
        )
    }

    @Test
    fun findEventAtIgnoresBlockedTimeChips() {
        val date = LocalDate(2026, 8, 20)
        val blockedChip = blockedTimeChip(
            id = 99,
            left = 0f,
            top = 100f,
            right = 200f,
            bottom = 300f,
        )
        val eventChip = sampleEventChip(
            id = 1,
            date = date,
            left = 10f,
            top = 120f,
            right = 80f,
            bottom = 180f,
        )

        assertNull(listOf(blockedChip).findEventAt(x = 100f, y = 150f))
        assertEquals(
            eventChip.toWeekViewEvent(),
            listOf(blockedChip, eventChip).findEventAt(x = 50f, y = 150f),
        )
    }

    @Test
    fun calculateTimeFromPointUsesCanvasContentCoordinates() {
        val layout = sampleGridLayout()
        val time = calculateTimeFromPoint(
            touchX = 60f,
            touchY = 150f,
            layout = layout,
            style = WeekViewStyle.Default,
            horizontalTranslationPx = 0f,
        )

        assertEquals(
            LocalDateTime(date = LocalDate(2026, 8, 20), time = kotlinx.datetime.LocalTime(3, 0)),
            time,
        )
    }

    @Test
    fun gridContentYForHitTestDoesNotApplyScrollTwice() {
        assertEquals(551f, gridContentYForHitTest(pointerYInCanvasPx = 551f))
    }

    @Test
    fun resolveGridTapFindsEventUsingCanvasContentCoordinates() {
        val date = LocalDate(2026, 8, 20)
        val chip = sampleEventChip(
            id = 1,
            date = date,
            left = 10f,
            top = 500f,
            right = 80f,
            bottom = 560f,
        )
        var tappedEvent: WeekViewEvent? = null

        resolveGridTap(
            offset = Offset(40f, 551f),
            eventChips = listOf(chip),
            horizontalTranslationPx = 0f,
            displayGridLayout = sampleGridLayout(),
            style = WeekViewStyle.Default,
            onEventClick = { tappedEvent = it },
            onEmptyViewClick = null,
        )

        assertEquals(chip.toWeekViewEvent(), tappedEvent)
    }

    @Test
    fun resolveGridTapFallsThroughBlockedTimeToEmptyClick() {
        var tappedTime: LocalDateTime? = null
        val layout = sampleGridLayout()
        val blockedChip = blockedTimeChip(id = 1, left = 0f, top = 100f, right = 200f, bottom = 300f)

        resolveGridTap(
            offset = Offset(100f, 150f),
            eventChips = listOf(blockedChip),
            horizontalTranslationPx = 0f,
            displayGridLayout = layout,
            style = WeekViewStyle.Default,
            onEventClick = null,
            onEmptyViewClick = { tappedTime = it },
        )

        assertEquals(LocalDateTime(date = LocalDate(2026, 8, 20), time = kotlinx.datetime.LocalTime(3, 0)), tappedTime)
    }

    @Test
    fun resolveGridTapPrefersEventOverEmptyClickWhenChipIsHit() {
        var tappedEvent: WeekViewEvent? = null
        var tappedTime: LocalDateTime? = null
        val date = LocalDate(2026, 8, 20)
        val chip = sampleEventChip(id = 1, date = date, left = 10f, top = 120f, right = 80f, bottom = 180f)

        resolveGridTap(
            offset = Offset(40f, 150f),
            eventChips = listOf(chip),
            horizontalTranslationPx = 0f,
            displayGridLayout = sampleGridLayout(),
            style = WeekViewStyle.Default,
            onEventClick = { tappedEvent = it },
            onEmptyViewClick = { tappedTime = it },
        )

        assertEquals(chip.toWeekViewEvent(), tappedEvent)
        assertNull(tappedTime)
    }

    @Test
    fun resolveGridLongPressStartsDragWhenLongClickReturnsFalse() {
        val date = LocalDate(2026, 8, 20)
        val event = sampleEvent(id = 1, date = date)
        val chip = sampleEventChip(id = 1, date = date, left = 0f, top = 100f, right = 80f, bottom = 200f)
        val layout = sampleGridLayout()

        val result = resolveGridLongPress(
            offset = Offset(40f, 150f),
            eventChips = listOf(chip),
            horizontalTranslationPx = 0f,
            displayGridLayout = layout,
            style = WeekViewStyle.Default,
            dragEnabled = true,
            onEventLongClick = { false },
            onEmptyViewLongClick = null,
        )

        assertFalse(result.handled)
        assertTrue(result.shouldStartDrag)
    }

    @Test
    fun resolveGridLongPressConsumesWhenLongClickReturnsTrue() {
        val date = LocalDate(2026, 8, 20)
        val chip = sampleEventChip(id = 1, date = date, left = 0f, top = 100f, right = 80f, bottom = 200f)

        val result = resolveGridLongPress(
            offset = Offset(40f, 150f),
            eventChips = listOf(chip),
            horizontalTranslationPx = 0f,
            displayGridLayout = sampleGridLayout(),
            style = WeekViewStyle.Default,
            dragEnabled = true,
            onEventLongClick = { true },
            onEmptyViewLongClick = null,
        )

        assertTrue(result.handled)
        assertFalse(result.shouldStartDrag)
    }
}

class EventChipBoundsCalculatorBlockedTimeTest {

    @Test
    fun blockedTimeUsesFullDayWidth() {
        val layout = sampleGridLayout(dayWidthPx = 120f, columnGapPx = 20f)
        val style = WeekViewStyle.Default
        val density = Density(density = 1f)
        val calculator = EventChipBoundsCalculator(layout, style, density)
        val startTime = LocalDate(2026, 8, 20).atTime(9, 0)
        val endTime = startTime.date.atTime(10, 0)
        val chip = EventChip(
            event = ResolvedWeekViewEntity.BlockedTime(
                id = 1,
                title = "",
                subtitle = null,
                startTime = startTime,
                endTime = endTime,
                style = ResolvedWeekViewEntity.Style(),
            ),
            index = 0,
            startTime = startTime,
            endTime = endTime,
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
            minutesFromStartHour = 60
        }

        val bounds = calculator.calculateSingleEvent(chip, dayStartX = 0f)

        assertEquals(120f, bounds.width(), 0.01f)
    }

    @Test
    fun calculateSingleEventUsesResolvedGridHeightForVerticalPosition() {
        val style = WeekViewStyle(minHour = 0, maxHour = 10)
        val density = Density(density = 1.75f)
        val rawLayout = WeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 800f,
            timeColumnWidthPx = 40f,
            headerHeightPx = 80f,
            hourHeightPx = 50f,
            dayWidthPx = 120f,
            columnGapPx = 1f,
            viewportGridWidthPx = 360f,
            contentGridWidthPx = 120f,
            gridHeightPx = 500f,
            visibleDates = listOf(LocalDate(2026, 8, 20)),
            renderDates = listOf(LocalDate(2026, 8, 20)),
            scrollBufferDays = 0,
        )
        val resolvedLayout = with(density) { resolveDisplayGridLayout(rawLayout, style) }
        val chip = timedEventChip(minutes = 120)
        val calculator = EventChipBoundsCalculator(resolvedLayout, style, density)

        val top = calculator.calculateSingleEvent(chip, dayStartX = 0f).top
        val expectedTop = resolvedLayout.gridHeightPx * (120f / (style.hoursCount * 60))

        assertEquals(expectedTop, top, 0.01f)
    }
}

private fun sampleGridLayout(
    dayWidthPx: Float = 120f,
    columnGapPx: Float = 20f,
): WeekViewLayout {
    val date = LocalDate(2026, 8, 20)
    return WeekViewLayout(
        viewportWidthPx = 400f,
        viewportHeightPx = 800f,
        timeColumnWidthPx = 40f,
        headerHeightPx = 80f,
        hourHeightPx = 50f,
        dayWidthPx = dayWidthPx,
        columnGapPx = columnGapPx,
        viewportGridWidthPx = 360f,
        contentGridWidthPx = dayWidthPx,
        gridHeightPx = 1200f,
        visibleDates = listOf(date),
        renderDates = listOf(date),
        scrollBufferDays = 0,
    )
}

private fun sampleEvent(id: Long, date: LocalDate): WeekViewEvent {
    return WeekViewEvent(
        id = id,
        title = "Event $id",
        startTime = date.atTime(10, 0),
        endTime = date.atTime(11, 0),
    )
}

private fun timedEventChip(minutes: Int): EventChip {
    val startTime = LocalDate(2026, 8, 20).atTime(9, 0)
    val endTime = startTime.date.atTime(10, 0)
    return EventChip(
        event = ResolvedWeekViewEntity.Event(
            id = 1,
            title = "Timed",
            startTime = startTime,
            endTime = endTime,
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = null,
        ),
        index = 0,
        startTime = startTime,
        endTime = endTime,
    ).apply {
        relativeStart = 0f
        relativeWidth = 1f
        minutesFromStartHour = minutes
    }
}

private fun sampleEventChip(
    id: Long,
    date: LocalDate,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): EventChip {
    val event = sampleEvent(id, date)
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
    ).apply {
        bounds = ChipBounds(left = left, top = top, right = right, bottom = bottom)
    }
}

private fun blockedTimeChip(
    id: Long,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): EventChip {
    val startTime = LocalDate(2026, 8, 20).atTime(8, 0)
    return EventChip(
        event = ResolvedWeekViewEntity.BlockedTime(
            id = id,
            title = "",
            subtitle = null,
            startTime = startTime,
            endTime = startTime.date.atTime(9, 0),
            style = ResolvedWeekViewEntity.Style(),
        ),
        index = 0,
        startTime = startTime,
        endTime = startTime.date.atTime(9, 0),
    ).apply {
        bounds = ChipBounds(left = left, top = top, right = right, bottom = bottom)
    }
}
