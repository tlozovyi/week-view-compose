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
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

class WeekViewAllDayLayoutTest {

    private val style = WeekViewStyle(
        numberOfVisibleDays = 3,
        headerHeightDp = Dp(40f),
        headerPaddingDp = Dp(4f),
        eventPaddingVerticalDp = Dp(2f),
        eventMarginVerticalDp = Dp(2f),
        allDayEventTextSizeSp = 12.sp,
    )
    private val density = FakeDensity(density = 1f)

    @Test
    fun withAllDaySectionExpandsHeaderHeight() {
        val baseLayout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
        )
        val layout = baseLayout.withAllDaySection(
            maxAllDayEventsPerDay = 2,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        )

        assertEquals(40f, layout.dateLabelHeightPx, absoluteTolerance = 0.01f)
        assertTrue(layout.allDaySectionHeightPx > 0f)
        assertEquals(40f + layout.allDaySectionHeightPx, layout.headerHeightPx, absoluteTolerance = 0.01f)
    }

    @Test
    fun calculateAllDayEventPositionsRowsBelowDateLabels() {
        val layout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
        ).withAllDaySection(
            maxAllDayEventsPerDay = 2,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        )

        val date = LocalDate(2026, 8, 20)
        val chip = EventChip(
            event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                id = 1,
                title = "Holiday",
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

        val calculator = EventChipBoundsCalculator(layout, style, density)
        val firstRow = calculator.calculateAllDayEvent(rowIndex = 0, eventChip = chip, dayStartX = 0f)
        val secondRow = calculator.calculateAllDayEvent(rowIndex = 1, eventChip = chip, dayStartX = 0f)

        assertTrue(firstRow.top >= layout.dateLabelHeightPx)
        assertTrue(secondRow.top > firstRow.bottom)
        assertEquals(layout.allDayChipHeightPx, firstRow.height(), absoluteTolerance = 0.01f)
    }

    @Test
    fun collapsedHeaderUsesAtMostTwoRows() {
        val baseLayout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
        )
        val expandedLayout = baseLayout.withAllDaySection(
            maxAllDayEventsPerDay = 5,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        )
        val collapsedLayout = baseLayout.withAllDaySection(
            maxAllDayEventsPerDay = 5,
            allDayEventsExpanded = false,
            style = style,
            density = density,
        )

        assertTrue(expandedLayout.headerHeightPx > collapsedLayout.headerHeightPx)
        assertEquals(2, visibleAllDayRowCount(5, allDayEventsExpanded = false, arrangeAllDayEventsVertically = true))
        assertEquals(5, visibleAllDayRowCount(5, allDayEventsExpanded = true, arrangeAllDayEventsVertically = true))
    }

    @Test
    fun applyAllDayEventVisibilityHidesOverflowWhenCollapsed() {
        val date = LocalDate(2026, 8, 20)
        val chips = (1..4).map { index ->
            EventChip(
                event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                    id = index.toLong(),
                    title = "Event $index",
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
            )
        }
        val chipsByDate = mapOf(date to chips)

        applyAllDayEventVisibility(
            allDayEventChips = chips,
            allDayChipsByDate = chipsByDate,
            renderDates = listOf(date),
            allDayEventsExpanded = false,
            arrangeAllDayEventsVertically = true,
        )

        assertEquals(1, chips.count { !it.isHidden })
        assertEquals(3, chips.count { it.isHidden })

        applyAllDayEventVisibility(
            allDayEventChips = chips,
            allDayChipsByDate = chipsByDate,
            renderDates = listOf(date),
            allDayEventsExpanded = true,
            arrangeAllDayEventsVertically = true,
        )

        assertTrue(chips.none { it.isHidden })
    }

    @Test
    fun animatedAllDaySectionInterpolatesHeaderHeight() {
        val baseLayout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
        )
        val collapsedLayout = baseLayout.withAnimatedAllDaySection(
            maxAllDayEventsPerDay = 5,
            expandProgress = 0f,
            style = style,
            density = density,
        )
        val midLayout = baseLayout.withAnimatedAllDaySection(
            maxAllDayEventsPerDay = 5,
            expandProgress = 0.5f,
            style = style,
            density = density,
        )
        val expandedLayout = baseLayout.withAnimatedAllDaySection(
            maxAllDayEventsPerDay = 5,
            expandProgress = 1f,
            style = style,
            density = density,
        )

        assertTrue(collapsedLayout.headerHeightPx < midLayout.headerHeightPx)
        assertTrue(midLayout.headerHeightPx < expandedLayout.headerHeightPx)
    }

    @Test
    fun showToggleWhenMoreThanTwoAllDayEvents() {
        assertTrue(showAllDayEventsToggleArrow(maxAllDayEventsPerDay = 3, arrangeAllDayEventsVertically = true))
        assertTrue(!showAllDayEventsToggleArrow(maxAllDayEventsPerDay = 2, arrangeAllDayEventsVertically = true))
    }

    @Test
    fun expandAfterSimulatedScrollRestoresBoundsForAllChips() {
        val baseLayout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
            horizontalScrollingEnabled = true,
        )
        val boundsLayout = baseLayout.withAllDaySection(
            maxAllDayEventsPerDay = 4,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        ).copy(contentGridWidthPx = baseLayout.renderDates.size * baseLayout.dayWidthPx)

        val date = LocalDate(2026, 8, 20)
        val chips = (1..4).map { index ->
            EventChip(
                event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                    id = index.toLong(),
                    title = "Event $index",
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
            )
        }
        val chipsByDate = mapOf(date to chips)

        applyAllDayEventVisibility(
            allDayEventChips = chips,
            allDayChipsByDate = chipsByDate,
            renderDates = boundsLayout.renderDates,
            allDayEventsExpanded = false,
            arrangeAllDayEventsVertically = true,
        )
        prepareAllDayEventChipBounds(
            allDayEventChips = chips,
            layout = boundsLayout,
            style = style,
            density = density,
            chipsByDate = chipsByDate,
            useExpandedAllDayLayout = false,
        )

        assertEquals(1, chips.count { chip -> !chip.isHidden && chip.bounds.right > chip.bounds.left })

        applyAllDayEventVisibility(
            allDayEventChips = chips,
            allDayChipsByDate = chipsByDate,
            renderDates = boundsLayout.renderDates,
            allDayEventsExpanded = true,
            arrangeAllDayEventsVertically = true,
        )
        prepareAllDayEventChipBounds(
            allDayEventChips = chips,
            layout = boundsLayout,
            style = style,
            density = density,
            chipsByDate = chipsByDate,
            useExpandedAllDayLayout = true,
        )

        assertEquals(4, chips.count { chip -> !chip.isHidden && chip.bounds.right > chip.bounds.left })
    }

    @Test
    fun allDayBoundsAcceptedInWideContentGrid() {
        val baseScrollLayout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = style,
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = density,
            horizontalScrollingEnabled = true,
        )
        val scrollLayout = baseScrollLayout.withAllDaySection(
            maxAllDayEventsPerDay = 1,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        ).copy(contentGridWidthPx = baseScrollLayout.renderDates.size * baseScrollLayout.dayWidthPx)

        val date = LocalDate(2026, 8, 24)
        val chip = EventChip(
            event = ResolvedWeekViewEntity.Event<WeekViewEvent>(
                id = 1,
                title = "Buffered all-day",
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
        )

        prepareAllDayEventChipBounds(
            allDayEventChips = listOf(chip),
            layout = scrollLayout,
            style = style,
            density = density,
            chipsByDate = mapOf(date to listOf(chip)),
            useExpandedAllDayLayout = true,
        )

        assertTrue(chip.bounds.right > chip.bounds.left)
        assertTrue(chip.bounds.left >= 500f)

        val dayWidthPx = scrollLayout.dayWidthPx
        listOf(-150f, -50f, 0f, 50f, 150f).forEach { offsetPx ->
            val translationPx = horizontalContentTranslationPx(
                scrollOffsetPx = offsetPx,
                dayWidthPx = dayWidthPx,
            )
            val screenLeft = chip.bounds.left + translationPx
            val screenRight = chip.bounds.right + translationPx
            val viewportWidthPx = scrollLayout.viewportGridWidthPx
            val partiallyVisible = screenRight > 0f && screenLeft < viewportWidthPx
            if (partiallyVisible) {
                assertTrue(
                    chip.bounds.right > chip.bounds.left,
                    "Bounds cleared while chip still on screen at offset $offsetPx",
                )
            }
        }
    }
}

private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
    kotlin.test.assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual",
    )
}

private fun ChipBounds.height(): Float = bottom - top

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)
