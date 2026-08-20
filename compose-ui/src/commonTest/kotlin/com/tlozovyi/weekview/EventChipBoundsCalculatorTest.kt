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

import androidx.compose.ui.unit.Dp
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

class EventChipBoundsCalculatorTest {

    private val style = WeekViewStyle(
        numberOfVisibleDays = 1,
        minHour = 0,
        maxHour = 24,
        hourHeightDp = Dp(60f),
        eventMarginVerticalDp = Dp(0f),
        overlappingEventGapDp = Dp(0f),
        columnGapDp = Dp(0f),
    )

    private val visibleDate = LocalDate(2026, 8, 20)

    private val layout = WeekViewLayout(
        viewportWidthPx = 400f,
        viewportHeightPx = 600f,
        timeColumnWidthPx = 56f,
        headerHeightPx = 40f,
        hourHeightPx = 60f,
        dayWidthPx = 344f,
        columnGapPx = 0f,
        viewportGridWidthPx = 344f,
        contentGridWidthPx = 344f,
        gridHeightPx = 1440f,
        visibleDates = listOf(visibleDate),
        renderDates = listOf(visibleDate),
        scrollBufferDays = 0,
    )

    private val density = FakeDensity(density = 1f)

    @Test
    fun singleEventFillsMorningSlot() {
        val date = LocalDate(2026, 8, 20)
        val event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
            id = 1,
            title = "Morning",
            startTime = date.atTime(9, 0),
            endTime = date.atTime(10, 0),
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = null,
        )
        val chip = EventChip(
            event = event,
            index = 0,
            startTime = event.startTime,
            endTime = event.endTime,
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
            minutesFromStartHour = 9 * 60
        }

        val calculator = EventChipBoundsCalculator(layout, style, density)
        val bounds = calculator.calculateSingleEvent(chip, dayStartX = 0f)

        assertTrue(bounds.top == 540f, "Expected top at 9 AM but was ${bounds.top}")
        assertTrue(bounds.bottom == 600f, "Expected bottom at 10 AM but was ${bounds.bottom}")
        assertTrue(bounds.left == 0f)
        assertTrue(bounds.right == 344f)
    }

    @Test
    fun bufferColumnBoundsAreAcceptedInWideContentGrid() {
        val scrollLayout = layout.copy(
            dayWidthPx = 100f,
            viewportGridWidthPx = 300f,
            contentGridWidthPx = 700f,
            scrollBufferDays = 2,
        )
        val date = LocalDate(2026, 8, 24)
        val event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
            id = 2,
            title = "Buffered",
            startTime = date.atTime(10, 0),
            endTime = date.atTime(11, 0),
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = null,
        )
        val chip = EventChip(
            event = event,
            index = 0,
            startTime = event.startTime,
            endTime = event.endTime,
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
            minutesFromStartHour = 10 * 60
        }

        listOf(chip).calculateBoundsForDate(
            dateIndex = 5,
            layout = scrollLayout,
            style = style.copy(numberOfVisibleDays = 3),
            density = density,
        )

        assertTrue(chip.bounds.right > chip.bounds.left)
        assertTrue(chip.bounds.left >= 500f)
    }

    @Test
    fun overlappingEventsSplitHorizontally() {
        val date = LocalDate(2026, 8, 20)
        val event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
            id = 1,
            title = "Half width",
            startTime = date.atTime(11, 0),
            endTime = date.atTime(12, 0),
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = null,
        )
        val chip = EventChip(
            event = event,
            index = 0,
            startTime = event.startTime,
            endTime = event.endTime,
        ).apply {
            relativeStart = 0f
            relativeWidth = 0.5f
            minutesFromStartHour = 11 * 60
        }

        val calculator = EventChipBoundsCalculator(layout, style, density)
        val bounds = calculator.calculateSingleEvent(chip, dayStartX = 0f)

        assertTrue(bounds.left == 0f)
        assertTrue(bounds.right == 172f, "Expected half-day width but was ${bounds.right}")
    }

    @Test
    fun columnGapInsetsEventsFromDayColumnEdge() {
        val gapStyle = style.copy(columnGapDp = Dp(8f))
        val gapLayout = layout.copy(columnGapPx = 8f)
        val date = LocalDate(2026, 8, 20)
        val timedChip = EventChip(
            event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                id = 1,
                title = "Timed",
                startTime = date.atTime(9, 0),
                endTime = date.atTime(10, 0),
                subtitle = null,
                isAllDay = false,
                style = ResolvedWeekViewEntity.Style(),
                data = null,
            ),
            index = 0,
            startTime = date.atTime(9, 0),
            endTime = date.atTime(10, 0),
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
            minutesFromStartHour = 9 * 60
        }
        val allDayChip = EventChip(
            event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                id = 2,
                title = "All day",
                startTime = date.atTime(0, 0),
                endTime = date.plusDays(1).atTime(0, 0),
                subtitle = null,
                isAllDay = true,
                style = ResolvedWeekViewEntity.Style(),
                data = null,
            ),
            index = 0,
            startTime = date.atTime(0, 0),
            endTime = date.plusDays(1).atTime(0, 0),
        ).apply {
            relativeStart = 0f
            relativeWidth = 1f
        }

        val calculator = EventChipBoundsCalculator(gapLayout, gapStyle, density)
        val timedBounds = calculator.calculateSingleEvent(timedChip, dayStartX = 0f)
        val allDayBounds = calculator.calculateAllDayEvent(
            rowIndex = 0,
            eventChip = allDayChip,
            dayStartX = 0f,
        )

        assertTrue(timedBounds.right == 336f, "Timed event right was ${timedBounds.right}")
        assertTrue(allDayBounds.right == 336f, "All-day event right was ${allDayBounds.right}")
    }
}

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)
