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
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class WeekViewFormattersTest {

    @Test
    fun defaultTimeFormatterUsesTwelveHourClock() {
        assertEquals("12 AM", defaultTimeFormatter(0))
        assertEquals("9 AM", defaultTimeFormatter(9))
        assertEquals("12 PM", defaultTimeFormatter(12))
        assertEquals("3 PM", defaultTimeFormatter(15))
    }

    @Test
    fun defaultDateFormatterAdaptsToVisibleDayCount() {
        val date = LocalDate(2026, 8, 20)
        assertEquals("Thursday 8/20", defaultDateFormatter(date, numberOfVisibleDays = 1))
        assertEquals("Thu 8/20", defaultDateFormatter(date, numberOfVisibleDays = 3))
        assertEquals("T 8/20", defaultDateFormatter(date, numberOfVisibleDays = 7))
    }
}

class WeekViewLayoutTest {

    @Test
    fun calculateWeekViewLayoutSplitsDaysEvenly() {
        val layout = calculateWeekViewLayout(
            viewportWidthPx = 400f,
            viewportHeightPx = 600f,
            style = WeekViewStyle(numberOfVisibleDays = 4, hourHeightDp = androidx.compose.ui.unit.Dp(50f)),
            firstVisibleDate = LocalDate(2026, 8, 20),
            density = FakeDensity(density = 1f),
        )

        assertEquals(4, layout.visibleDates.size)
        assertFloatEquals(344f, layout.gridWidthPx, absoluteTolerance = 0.01f)
        assertFloatEquals(86f, layout.dayWidthPx, absoluteTolerance = 0.01f)
        assertFloatEquals(1200f, layout.gridHeightPx, absoluteTolerance = 0.01f)
        assertTrue(layout.visibleDates.first() == LocalDate(2026, 8, 20))
        assertTrue(layout.visibleDates.last() == LocalDate(2026, 8, 23))
    }
}
