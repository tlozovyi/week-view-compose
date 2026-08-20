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
    val dateLabelHeightPx: Float = headerHeightPx,
    val allDaySectionHeightPx: Float = 0f,
    val allDayChipHeightPx: Float = 0f,
    val maxAllDayRowsPerDay: Int = 0,
    val hourHeightPx: Float,
    val dayWidthPx: Float,
    val columnGapPx: Float,
    val viewportGridWidthPx: Float,
    val contentGridWidthPx: Float,
    val gridHeightPx: Float,
    val visibleDates: List<LocalDate>,
    val renderDates: List<LocalDate>,
    val scrollBufferDays: Int,
) {
    /** @deprecated Use [viewportGridWidthPx] for the clipped viewport width. */
    val gridWidthPx: Float
        get() = viewportGridWidthPx

    /** Width available for event chips within a day column (excludes [columnGapPx]). */
    val drawableDayWidthPx: Float
        get() = (dayWidthPx - columnGapPx).coerceAtLeast(0f)

    fun dayStartX(dayIndex: Int): Float = dayIndex * dayWidthPx

    fun hourY(hour: Int, minHour: Int): Float = (hour - minHour) * hourHeightPx

    fun dateIndex(date: LocalDate): Int = renderDates.indexOf(date)
}

internal fun calculateWeekViewLayout(
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    style: WeekViewStyle,
    firstVisibleDate: LocalDate,
    density: Density,
    horizontalScrollingEnabled: Boolean = false,
): WeekViewLayout {
    val timeColumnWidthPx = with(density) { style.timeColumnWidthDp.toPx() }
    val headerHeightPx = with(density) { style.headerHeightDp.toPx() }
    val hourHeightPx = with(density) { style.hourHeightDp.toPx() }
    val viewportGridWidthPx = (viewportWidthPx - timeColumnWidthPx).coerceAtLeast(0f)
    val dayWidthPx = viewportGridWidthPx / style.numberOfVisibleDays
    val columnGapPx = with(density) { style.columnGapDp.toPx() }
    val gridHeightPx = style.hoursCount * hourHeightPx
    val scrollBufferDays = if (horizontalScrollingEnabled) HORIZONTAL_SCROLL_BUFFER_DAYS else 0
    val visibleDates = buildList {
        for (index in 0 until style.numberOfVisibleDays) {
            add(firstVisibleDate.plusDays(index))
        }
    }
    val renderDates = buildRenderDates(
        firstVisibleDate = firstVisibleDate,
        numberOfVisibleDays = style.numberOfVisibleDays,
        scrollBufferDays = scrollBufferDays,
    )
    val contentGridWidthPx = renderDates.size * dayWidthPx

    return WeekViewLayout(
        viewportWidthPx = viewportWidthPx,
        viewportHeightPx = viewportHeightPx,
        timeColumnWidthPx = timeColumnWidthPx,
        headerHeightPx = headerHeightPx,
        hourHeightPx = hourHeightPx,
        dayWidthPx = dayWidthPx,
        columnGapPx = columnGapPx,
        viewportGridWidthPx = viewportGridWidthPx,
        contentGridWidthPx = contentGridWidthPx,
        gridHeightPx = gridHeightPx,
        visibleDates = visibleDates,
        renderDates = renderDates,
        scrollBufferDays = scrollBufferDays,
    )
}

internal fun Dp.toPx(density: Density): Float = with(density) { toPx() }
