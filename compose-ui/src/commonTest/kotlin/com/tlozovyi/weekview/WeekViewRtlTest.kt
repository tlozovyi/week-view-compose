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
import kotlin.test.assertNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class WeekViewRtlTest {

    @Test
    fun buildRenderDates_decrementsDatesInRtl() {
        val firstVisible = LocalDate(2026, 8, 21)
        val dates = buildRenderDates(
            firstVisibleDate = firstVisible,
            numberOfVisibleDays = 3,
            scrollBufferDays = 1,
            isLtr = false,
        )
        assertEquals(
            listOf(
                LocalDate(2026, 8, 22),
                firstVisible,
                LocalDate(2026, 8, 20),
                LocalDate(2026, 8, 19),
                LocalDate(2026, 8, 18),
            ),
            dates,
        )
    }

    @Test
    fun applyHorizontalScrollDelta_usesSameDragDirectionInRtl() {
        val date = LocalDate(2026, 8, 21)
        var currentDate = date

        val offsetAfterForward = applyHorizontalScrollDelta(
            offsetPx = 0f,
            deltaPx = 100f,
            dayWidthPx = 100f,
            firstVisibleDate = currentDate,
            onFirstVisibleDateChange = { currentDate = it },
            isLtr = false,
        )

        assertEquals(0f, offsetAfterForward, absoluteTolerance = 0.01f)
        assertEquals(LocalDate(2026, 8, 22), currentDate)
    }

    @Test
    fun referenceColumnScreenX_negatesDayDeltaInRtl() {
        val anchor = LocalDate(2026, 8, 21)
        val reference = LocalDate(2026, 8, 22)
        val ltrX = referenceColumnScreenX(anchor, 0f, reference, 100f, isLtr = true)
        val rtlX = referenceColumnScreenX(anchor, 0f, reference, 100f, isLtr = false)
        assertEquals(100f, ltrX, absoluteTolerance = 0.01f)
        assertEquals(-100f, rtlX, absoluteTolerance = 0.01f)
    }

    @Test
    fun nowDotCenterScreenX_usesTodayTrailingEdgeInRtl() {
        val viewportWidthPx = 300f
        val columnLeft = 50f
        val columnRight = 150f
        val horizontalTranslationPx = 0f

        assertEquals(
            columnRight,
            nowDotCenterScreenX(
                columnLeft = columnLeft,
                columnRight = columnRight,
                horizontalTranslationPx = horizontalTranslationPx,
                viewportGridWidthPx = viewportWidthPx,
                isLtr = false,
            ),
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun nowDotCenterScreenX_pinsToViewportTrailingEdgeWhenColumnIsOffScreenInRtl() {
        val viewportWidthPx = 300f
        val columnRight = 350f
        val horizontalTranslationPx = 250f

        assertEquals(
            viewportWidthPx,
            nowDotCenterScreenX(
                columnLeft = 250f,
                columnRight = columnRight,
                horizontalTranslationPx = horizontalTranslationPx,
                viewportGridWidthPx = viewportWidthPx,
                isLtr = false,
            ),
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun toResolvedEntities_includesEventsInsideRtlRenderDateRange() {
        val renderDates = buildRenderDates(
            firstVisibleDate = LocalDate(2026, 8, 21),
            numberOfVisibleDays = 3,
            scrollBufferDays = 1,
            isLtr = false,
        )
        val events = listOf(
            WeekViewEvent(
                id = 1L,
                title = "Today",
                startTime = LocalDate(2026, 8, 21).atTime(10, 0),
                endTime = LocalDate(2026, 8, 21).atTime(11, 0),
            ),
        )
        val resolved = events.toResolvedEntities(
            style = WeekViewStyle(),
            density = androidx.compose.ui.unit.Density(1f),
            visibleDates = renderDates,
        )
        assertEquals(1, resolved.size)
        assertNotNull(resolved.first() as? ResolvedWeekViewEntity.Event<*>)
    }

    @Test
    fun eventChipBounds_addsColumnGapOffsetInRtl() {
        val layout = WeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 800f,
            timeColumnWidthPx = 56f,
            headerHeightPx = 40f,
            hourHeightPx = 50f,
            dayWidthPx = 100f,
            columnGapPx = 4f,
            viewportGridWidthPx = 344f,
            contentGridWidthPx = 300f,
            gridHeightPx = 800f,
            visibleDates = listOf(LocalDate(2026, 8, 21)),
            renderDates = listOf(LocalDate(2026, 8, 21)),
            scrollBufferDays = 0,
            isLtr = false,
        )
        val calculator = EventChipBoundsCalculator(
            layout = layout,
            style = WeekViewStyle(numberOfVisibleDays = 3),
            density = androidx.compose.ui.unit.Density(1f),
        )
        val chip = EventChip(
            event = ResolvedWeekViewEntity.Event(
                id = 1L,
                title = "Test",
                startTime = LocalDate(2026, 8, 21).atTime(9, 0),
                endTime = LocalDate(2026, 8, 21).atTime(10, 0),
                subtitle = null,
                isAllDay = false,
                style = ResolvedWeekViewEntity.Style(),
                data = null,
            ),
            index = 0,
            startTime = LocalDate(2026, 8, 21).atTime(9, 0),
            endTime = LocalDate(2026, 8, 21).atTime(10, 0),
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
        }
        val bounds = calculator.calculateSingleEvent(chip, dayStartX = 0f)
        assertEquals(4f, bounds.left, absoluteTolerance = 0.01f)
    }
}
