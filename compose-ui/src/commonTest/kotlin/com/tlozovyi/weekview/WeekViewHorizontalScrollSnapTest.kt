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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class WeekViewHorizontalScrollSnapTest {

    @Test
    fun horizontalPageOriginDateAlignsSevenDayViewToFirstDayOfWeek() {
        val origin = horizontalPageOriginDate(
            firstVisibleDate = LocalDate(2026, 8, 20),
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 17), origin)
    }

    @Test
    fun horizontalPageOriginDateKeepsUserDateForThreeDayView() {
        val origin = horizontalPageOriginDate(
            firstVisibleDate = LocalDate(2026, 8, 20),
            numberOfVisibleDays = 3,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 20), origin)
    }

    @Test
    fun horizontalSnapThresholdDaysIsLowerThanHalfPageForSevenDays() {
        assertEquals(2, horizontalSnapThresholdDays(numberOfVisibleDays = 7))
    }

    @Test
    fun snapToNearestDayColumnRoundsTowardPastWhenMoreThanHalfDay() {
        val target = snapToNearestDayColumn(
            anchorDate = LocalDate(2026, 8, 20),
            scrollOffsetPx = 80f,
            dayWidthPx = 100f,
        )

        assertEquals(LocalDate(2026, 8, 19), target.anchorDate)
        assertEquals(0f, target.scrollOffsetPx)
    }

    @Test
    fun snapToVisibleDaysPageKeepsCurrentPageAfterSmallFutureScroll() {
        val target = snapToVisibleDaysPage(
            gesturePageStart = LocalDate(2026, 8, 17),
            anchorDate = LocalDate(2026, 8, 19),
            scrollOffsetPx = -50f,
            dayWidthPx = 100f,
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 17), target.anchorDate)
        assertEquals(0f, target.scrollOffsetPx)
    }

    @Test
    fun snapToVisibleDaysPageAdvancesToNextWeekAfterThreeDayFutureScroll() {
        val target = snapToVisibleDaysPage(
            gesturePageStart = LocalDate(2026, 8, 17),
            anchorDate = LocalDate(2026, 8, 20),
            scrollOffsetPx = 0f,
            dayWidthPx = 100f,
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 24), target.anchorDate)
        assertEquals(0f, target.scrollOffsetPx)
    }

    @Test
    fun snapToVisibleDaysPageAdvancesThreeDayViewAfterSingleDayScroll() {
        val target = snapToVisibleDaysPage(
            gesturePageStart = LocalDate(2026, 8, 20),
            anchorDate = LocalDate(2026, 8, 21),
            scrollOffsetPx = 0f,
            dayWidthPx = 100f,
            numberOfVisibleDays = 3,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 23), target.anchorDate)
        assertEquals(0f, target.scrollOffsetPx)
    }

    @Test
    fun snapHorizontalScrollTargetUsesVisibleDaysPageSnap() {
        val target = snapHorizontalScrollTarget(
            gesturePageStart = LocalDate(2026, 8, 17),
            anchorDate = LocalDate(2026, 8, 19),
            scrollOffsetPx = -50f,
            dayWidthPx = 100f,
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 17), target.anchorDate)
    }

    @Test
    fun normalizeHorizontalScrollOffsetRollsIntoAdjacentDays() {
        val normalized = normalizeHorizontalScrollOffset(
            anchorDate = LocalDate(2026, 8, 20),
            scrollOffsetPx = 150f,
            dayWidthPx = 100f,
        )

        assertEquals(LocalDate(2026, 8, 19), normalized.anchorDate)
        assertEquals(50f, normalized.scrollOffsetPx)
    }
}
