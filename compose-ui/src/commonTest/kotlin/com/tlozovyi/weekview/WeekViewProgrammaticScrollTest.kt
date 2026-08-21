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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class WeekViewProgrammaticScrollTest {

    @Test
    fun clampProgrammaticScrollDateRespectsMinAndMax() {
        val min = LocalDate(2026, 8, 1)
        val max = LocalDate(2026, 8, 31)

        assertEquals(min, clampProgrammaticScrollDate(
            date = LocalDate(2026, 7, 15),
            minDate = min,
            maxDate = max,
            numberOfVisibleDays = 3,
        ))
        assertEquals(
            LocalDate(2026, 8, 29),
            clampProgrammaticScrollDate(
                date = LocalDate(2026, 9, 10),
                minDate = min,
                maxDate = max,
                numberOfVisibleDays = 3,
            ),
        )
    }

    @Test
    fun horizontalScrollTargetUsesWeekOriginForSevenDayView() {
        val wednesday = LocalDate(2026, 8, 19)
        val target = horizontalScrollTargetForDate(
            targetDate = wednesday,
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 17), target.anchorDate)
        assertEquals(0f, target.scrollOffsetPx)
    }

    @Test
    fun scrollOffsetForTimeAddsHourPaddingAboveTarget() {
        val layout = sampleGridLayout()
        val style = WeekViewStyle.Default.copy(minHour = 7, maxHour = 22)
        val offset = scrollOffsetForTime(
            layout = layout,
            style = style,
            hour = 10,
            minute = 30,
            gridViewportHeightPx = 400f,
            maxGridScrollOffsetPx = 800f,
        )

        val nineAmY = layout.hourY(9, style.minHour)
        assertTrue(offset >= nineAmY - 1f)
        assertTrue(offset < layout.hourY(10, style.minHour))
    }

    @Test
    fun firstVisibleDateFromScrollStateUsesLeadingDate() {
        val anchor = LocalDate(2026, 8, 17)
        val leading = firstVisibleDateFromScrollState(
            anchorDate = anchor,
            scrollOffsetPx = -120f,
            dayWidthPx = 120f,
            numberOfVisibleDays = 7,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate(2026, 8, 18), leading)
    }
}

private fun sampleGridLayout(): WeekViewLayout {
    val date = LocalDate(2026, 8, 20)
    return WeekViewLayout(
        viewportWidthPx = 400f,
        viewportHeightPx = 800f,
        timeColumnWidthPx = 40f,
        headerHeightPx = 80f,
        hourHeightPx = 50f,
        dayWidthPx = 120f,
        columnGapPx = 20f,
        viewportGridWidthPx = 360f,
        contentGridWidthPx = 360f,
        gridHeightPx = 750f,
        visibleDates = listOf(date),
        renderDates = listOf(date),
        scrollBufferDays = 0,
    )
}
