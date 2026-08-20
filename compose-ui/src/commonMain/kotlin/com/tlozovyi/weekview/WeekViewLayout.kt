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

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate

internal data class WeekViewLayout(
    val viewportWidthPx: Float,
    val viewportHeightPx: Float,
    val timeColumnWidthPx: Float,
    val headerHeightPx: Float,
    val hourHeightPx: Float,
    val dayWidthPx: Float,
    val gridWidthPx: Float,
    val gridHeightPx: Float,
    val visibleDates: List<LocalDate>,
) {
    fun dayStartX(dayIndex: Int): Float = dayIndex * dayWidthPx

    fun hourY(hour: Int, minHour: Int): Float = (hour - minHour) * hourHeightPx
}

internal fun calculateWeekViewLayout(
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    style: WeekViewStyle,
    firstVisibleDate: LocalDate,
    density: Density,
): WeekViewLayout {
    val timeColumnWidthPx = with(density) { style.timeColumnWidthDp.toPx() }
    val headerHeightPx = with(density) { style.headerHeightDp.toPx() }
    val hourHeightPx = with(density) { style.hourHeightDp.toPx() }
    val gridWidthPx = (viewportWidthPx - timeColumnWidthPx).coerceAtLeast(0f)
    val dayWidthPx = gridWidthPx / style.numberOfVisibleDays
    val gridHeightPx = style.hoursCount * hourHeightPx
    val visibleDates = buildList {
        for (index in 0 until style.numberOfVisibleDays) {
            add(firstVisibleDate.plusDays(index))
        }
    }

    return WeekViewLayout(
        viewportWidthPx = viewportWidthPx,
        viewportHeightPx = viewportHeightPx,
        timeColumnWidthPx = timeColumnWidthPx,
        headerHeightPx = headerHeightPx,
        hourHeightPx = hourHeightPx,
        dayWidthPx = dayWidthPx,
        gridWidthPx = gridWidthPx,
        gridHeightPx = gridHeightPx,
        visibleDates = visibleDates,
    )
}

private fun LocalDate.plusDays(days: Int): LocalDate {
    return LocalDate.fromEpochDays(toEpochDays() + days)
}

internal fun Dp.toPx(density: Density): Float = with(density) { toPx() }
