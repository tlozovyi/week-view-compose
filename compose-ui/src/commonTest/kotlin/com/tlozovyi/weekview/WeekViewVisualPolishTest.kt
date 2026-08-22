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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeekViewVisualPolishTest {

    @Test
    fun isWeekend_detectsSaturdayAndSunday() {
        assertTrue(LocalDate(2026, 8, 22).isWeekend())
        assertTrue(LocalDate(2026, 8, 23).isWeekend())
        assertFalse(LocalDate(2026, 8, 21).isWeekend())
    }

    @Test
    fun weekOfYear_matchesIsoWeekForAugust2026() {
        assertEquals(34, LocalDate(2026, 8, 21).weekOfYear())
        assertEquals(35, LocalDate(2026, 8, 24).weekOfYear())
    }

    @Test
    fun weekendBackgroundColors_fallbackToWeekdayDefaults() {
        val style = WeekViewStyle(
            pastDayBackgroundColor = Color.Red,
            futureDayBackgroundColor = Color.Blue,
        )
        val saturday = LocalDate(2026, 8, 22)
        assertEquals(style.pastDayBackgroundColor, style.pastDayBackground(saturday))
        assertEquals(style.futureDayBackgroundColor, style.futureDayBackground(saturday))
    }

    @Test
    fun weekendBackgroundColors_useOverridesWhenSet() {
        val pastWeekend = Color(0xFFFFE0E0)
        val futureWeekend = Color(0xFFE0E0FF)
        val style = WeekViewStyle(
            pastWeekendBackgroundColor = pastWeekend,
            futureWeekendBackgroundColor = futureWeekend,
        )
        val saturday = LocalDate(2026, 8, 22)
        assertEquals(pastWeekend, style.pastDayBackground(saturday))
        assertEquals(futureWeekend, style.futureDayBackground(saturday))
    }

    @Test
    fun fillPattern_mapsToEntityPattern() {
        val density = Density(1f)
        val pattern = WeekViewFillPattern.Lined(
            color = Color.Green,
            strokeWidthDp = 2.dp,
            spacingDp = 6.dp,
            direction = WeekViewLinedPatternDirection.EndToStart,
        )
        val entityPattern = pattern.toEntityPattern(density)
        assertTrue(entityPattern is ResolvedWeekViewEntity.FillPattern.Lined)
        assertEquals(2, entityPattern.strokeWidthPx)
        assertEquals(6, entityPattern.spacingPx)
        assertFalse(entityPattern.startToEnd)
    }

    @Test
    fun eventStyle_includesPatternInResolvedStyle() {
        val density = Density(1f)
        val style = WeekViewStyle.Default
        val eventStyle = WeekViewEventStyle(
            pattern = WeekViewFillPattern.Dotted(
                color = Color.Black,
                strokeWidthDp = 1.dp,
                spacingDp = 4.dp,
            ),
        )
        val resolved = eventStyle.toEntityStyle(style, density)
        assertTrue(resolved.pattern is ResolvedWeekViewEntity.FillPattern.Dotted)
    }

    @Test
    fun buildDiagonalLines_addsExtraLinesForTallChips() {
        val bounds = Rect(0f, 0f, 100f, 400f)
        val shortLines = buildDiagonalLines(
            bounds = Rect(0f, 0f, 100f, 100f),
            spacing = 10f,
            isLtr = true,
            startToEnd = true,
        )
        val tallLines = buildDiagonalLines(
            bounds = bounds,
            spacing = 10f,
            isLtr = true,
            startToEnd = true,
        )
        assertTrue(tallLines.size > shortLines.size)
    }
}
