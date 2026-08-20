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
import kotlinx.datetime.LocalDate

class WeekViewHorizontalScrollTest {

    @Test
    fun applyHorizontalScrollDeltaRollsDateWhenCrossingDayWidth() {
        val date = LocalDate(2026, 8, 20)
        var currentDate = date

        val offsetAfterForward = applyHorizontalScrollDelta(
            offsetPx = 0f,
            deltaPx = -100f,
            dayWidthPx = 100f,
            firstVisibleDate = currentDate,
            onFirstVisibleDateChange = { currentDate = it },
        )

        assertEquals(0f, offsetAfterForward, absoluteTolerance = 0.01f)
        assertEquals(LocalDate(2026, 8, 21), currentDate)
    }

    @Test
    fun applyHorizontalScrollDeltaRollsDateBackward() {
        val date = LocalDate(2026, 8, 20)
        var currentDate = date

        val offsetAfterBackward = applyHorizontalScrollDelta(
            offsetPx = 0f,
            deltaPx = 100f,
            dayWidthPx = 100f,
            firstVisibleDate = currentDate,
            onFirstVisibleDateChange = { currentDate = it },
        )

        assertEquals(0f, offsetAfterBackward, absoluteTolerance = 0.01f)
        assertEquals(LocalDate(2026, 8, 19), currentDate)
    }

    @Test
    fun applyHorizontalScrollDeltaPreservesFractionalOffsetAfterRoll() {
        val date = LocalDate(2026, 8, 20)
        var currentDate = date

        val offsetAfterBackward = applyHorizontalScrollDelta(
            offsetPx = 95f,
            deltaPx = 10f,
            dayWidthPx = 100f,
            firstVisibleDate = currentDate,
            onFirstVisibleDateChange = { currentDate = it },
        )

        assertEquals(5f, offsetAfterBackward, absoluteTolerance = 0.01f)
        assertEquals(LocalDate(2026, 8, 19), currentDate)
    }

    @Test
    fun applyHorizontalScrollDeltaCanRollMultipleDaysInOneGesture() {
        val date = LocalDate(2026, 8, 20)
        var currentDate = date

        val offset = applyHorizontalScrollDelta(
            offsetPx = 0f,
            deltaPx = -250f,
            dayWidthPx = 100f,
            firstVisibleDate = currentDate,
            onFirstVisibleDateChange = { currentDate = it },
        )

        assertEquals(-50f, offset, absoluteTolerance = 0.01f)
        assertEquals(LocalDate(2026, 8, 22), currentDate)
    }

    @Test
    fun horizontalContentTranslationAccountsForScrollBuffer() {
        assertEquals(-200f, horizontalContentTranslationPx(scrollOffsetPx = 0f, dayWidthPx = 100f))
        assertEquals(-150f, horizontalContentTranslationPx(scrollOffsetPx = 50f, dayWidthPx = 100f))
    }

    @Test
    fun buildRenderDatesIncludesOffScreenBufferDays() {
        val anchor = LocalDate(2026, 8, 20)
        val dates = buildRenderDates(
            firstVisibleDate = anchor,
            numberOfVisibleDays = 3,
            scrollBufferDays = HORIZONTAL_SCROLL_BUFFER_DAYS,
        )

        assertEquals(7, dates.size)
        assertEquals(LocalDate(2026, 8, 18), dates.first())
        assertEquals(LocalDate(2026, 8, 24), dates.last())
    }

    @Test
    fun dayColumnScreenBoundsAnchorColumnsStartAtViewportOriginAtRest() {
        val dayWidthPx = 100f
        val translationPx = horizontalContentTranslationPx(
            scrollOffsetPx = 0f,
            dayWidthPx = dayWidthPx,
        )

        assertEquals(0f to 100f, dayColumnScreenBounds(2, dayWidthPx, translationPx))
        assertEquals(100f to 200f, dayColumnScreenBounds(3, dayWidthPx, translationPx))
        assertEquals(200f to 300f, dayColumnScreenBounds(4, dayWidthPx, translationPx))
    }

    @Test
    fun headerLabelCenteredScreenXUsesFixedColumnCenter() {
        val textX = headerLabelCenteredScreenX(
            screenLeft = 250f,
            screenRight = 350f,
            textWidth = 40f,
            paddingHorizontalPx = 4f,
        )

        assertEquals(280f, textX, absoluteTolerance = 0.01f)
    }

    @Test
    fun headerLabelCenteredScreenXMatchesFullyVisibleColumn() {
        val textX = headerLabelCenteredScreenX(
            screenLeft = 100f,
            screenRight = 200f,
            textWidth = 40f,
            paddingHorizontalPx = 4f,
        )

        assertEquals(130f, textX, absoluteTolerance = 0.01f)
    }

    @Test
    fun isDayColumnVisibleOnScreenDetectsViewportIntersection() {
        assertEquals(false, isDayColumnVisibleOnScreen(320f, 420f, 300f))
        assertEquals(true, isDayColumnVisibleOnScreen(250f, 350f, 300f))
    }

    @Test
    fun nowDotCenterScreenXPinsToTimeDividerWhenTodayScrollsOffLeft() {
        val dayWidthPx = 100f
        val columnLeft = 200f
        val horizontalTranslationPx = -250f

        assertEquals(0f, nowDotCenterScreenX(columnLeft, horizontalTranslationPx), absoluteTolerance = 0.01f)
    }

    @Test
    fun nowDotCenterScreenXUsesTodayDividerWhenFullyVisible() {
        val columnLeft = 200f
        val horizontalTranslationPx = -200f

        assertEquals(0f, nowDotCenterScreenX(columnLeft, horizontalTranslationPx), absoluteTolerance = 0.01f)
    }

    @Test
    fun nowDotCenterScreenXFollowsTodayDividerWhenPastTimeEdge() {
        val columnLeft = 250f
        val horizontalTranslationPx = -200f

        assertEquals(50f, nowDotCenterScreenX(columnLeft, horizontalTranslationPx), absoluteTolerance = 0.01f)
    }

    @Test
    fun isNowDotVisibleOnScreenHidesWhenTodayFullyScrolledToFuture() {
        assertEquals(false, isNowDotVisibleOnScreen(screenLeft = -150f, screenRight = -50f, viewportWidthPx = 300f))
    }

    @Test
    fun isNowDotVisibleOnScreenShowsWhenTodayPartiallyVisibleFromTimeDivider() {
        assertEquals(true, isNowDotVisibleOnScreen(screenLeft = -40f, screenRight = 60f, viewportWidthPx = 300f))
    }

    @Test
    fun isTodayColumnVisibleOnScreenUsesRenderDatesNotVisibleDates() {
        val layout = WeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 800f,
            timeColumnWidthPx = 56f,
            headerHeightPx = 40f,
            hourHeightPx = 50f,
            dayWidthPx = 100f,
            columnGapPx = 1f,
            viewportGridWidthPx = 300f,
            contentGridWidthPx = 700f,
            gridHeightPx = 750f,
            visibleDates = listOf(
                LocalDate(2026, 8, 21),
                LocalDate(2026, 8, 22),
                LocalDate(2026, 8, 23),
            ),
            renderDates = buildRenderDates(
                firstVisibleDate = LocalDate(2026, 8, 21),
                numberOfVisibleDays = 3,
                scrollBufferDays = HORIZONTAL_SCROLL_BUFFER_DAYS,
            ),
            scrollBufferDays = HORIZONTAL_SCROLL_BUFFER_DAYS,
        )
        val today = LocalDate(2026, 8, 20)
        assertEquals(1, layout.dateIndex(today))
        val horizontalTranslationPx = -140f

        assertEquals(true, isTodayColumnVisibleOnScreen(layout, today, horizontalTranslationPx))
        assertEquals(false, layout.visibleDates.contains(today))
    }

    @Test
    fun offScreenBufferColumnStartsOutsideViewportAtRest() {
        val numberOfVisibleDays = 3
        val dayWidthPx = 100f
        val viewportGridWidthPx = numberOfVisibleDays * dayWidthPx
        val translationPx = horizontalContentTranslationPx(
            scrollOffsetPx = 0f,
            dayWidthPx = dayWidthPx,
            scrollBufferDays = HORIZONTAL_SCROLL_BUFFER_DAYS,
        )
        val firstRightBufferIndex = HORIZONTAL_SCROLL_BUFFER_DAYS + numberOfVisibleDays
        val fullyOffScreenRightBufferStartPx =
            (firstRightBufferIndex + 1) * dayWidthPx + translationPx

        assertEquals(
            viewportGridWidthPx + dayWidthPx,
            fullyOffScreenRightBufferStartPx,
            absoluteTolerance = 0.01f,
        )
    }
}

private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
    kotlin.test.assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual",
    )
}
