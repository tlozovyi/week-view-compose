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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class WeekViewTypographyTest {

    @Test
    fun headerDateColor_usesTodayHeaderTextColorForToday() {
        val today = LocalDate(2026, 8, 22)
        val style = WeekViewStyle(
            headerTextColor = Color.Black,
            todayHeaderTextColor = Color.Red,
            weekendHeaderTextColor = Color.Blue,
        )

        assertEquals(Color.Red, style.headerDateColor(today, today))
        assertEquals(Color.Blue, style.headerDateColor(LocalDate(2026, 8, 23), today))
        assertEquals(Color.Black, style.headerDateColor(LocalDate(2026, 8, 21), today))
    }

    @Test
    fun headerDateColor_fallsBackToHeaderTextColorWhenTodayColorUnset() {
        val today = LocalDate(2026, 8, 22)
        val style = WeekViewStyle(headerTextColor = Color.Green)

        assertEquals(Color.Green, style.headerDateColor(today, today))
    }
}

class WeekViewDisplayGridLayoutTest {

    @Test
    fun resolveDisplayGridLayout_adjustsHourHeightAfterDpRoundTrip() {
        val density = Density(density = 1.75f)
        val style = WeekViewStyle(minHour = 0, maxHour = 10)
        val gridLayout = WeekViewLayout(
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
            visibleDates = listOf(LocalDate(2026, 8, 22)),
            renderDates = listOf(LocalDate(2026, 8, 22)),
            scrollBufferDays = 0,
        )

        val resolved = with(density) { resolveDisplayGridLayout(gridLayout, style) }

        assertEquals(with(density) { layoutHeightPx(gridLayout.gridHeightPx) }, resolved.gridHeightPx)
        assertEquals(resolved.gridHeightPx / style.hoursCount, resolved.hourHeightPx)
    }
}
