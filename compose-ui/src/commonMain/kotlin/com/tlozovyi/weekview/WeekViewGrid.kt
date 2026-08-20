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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun DrawScope.drawWeekViewGrid(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    today: LocalDate,
) {
    drawDayBackgrounds(layout, style, today)
    if (style.showHourSeparators) {
        drawHourSeparators(layout, style)
    }
    if (style.showDaySeparators) {
        drawDaySeparators(layout, style)
    }
    if (style.showNowLine) {
        drawNowLine(layout, style, today)
    }
}

private fun DrawScope.drawDayBackgrounds(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    today: LocalDate,
) {
    layout.visibleDates.forEachIndexed { index, date ->
        val left = layout.dayStartX(index)
        val top = 0f
        val size = Size(layout.dayWidthPx, layout.gridHeightPx)

        when {
            date == today -> drawTodayBackground(left, top, size, layout, style)
            date < today -> drawRect(style.pastDayBackgroundColor, Offset(left, top), size)
            else -> drawRect(style.futureDayBackgroundColor, Offset(left, top), size)
        }
    }
}

private fun DrawScope.drawTodayBackground(
    left: Float,
    top: Float,
    size: Size,
    layout: WeekViewLayout,
    style: WeekViewStyle,
) {
    val now = currentDateTime()
    val minutesFromStart = (now.hour - style.minHour) * 60 + now.minute
    val totalMinutes = style.hoursCount * 60
    val pastFraction = (minutesFromStart.toFloat() / totalMinutes).coerceIn(0f, 1f)
    val pastHeight = layout.gridHeightPx * pastFraction

    drawRect(
        color = style.todayPastBackgroundColor,
        topLeft = Offset(left, top),
        size = Size(size.width, pastHeight),
    )
    drawRect(
        color = style.todayFutureBackgroundColor,
        topLeft = Offset(left, top + pastHeight),
        size = Size(size.width, size.height - pastHeight),
    )
}

private fun DrawScope.drawHourSeparators(
    layout: WeekViewLayout,
    style: WeekViewStyle,
) {
    val stroke = style.hourSeparatorHeightDp.toPx()
    for (hour in style.hours) {
        val y = layout.hourY(hour, style.minHour)
        drawLine(
            color = style.hourSeparatorColor,
            start = Offset(0f, y),
            end = Offset(layout.gridWidthPx, y),
            strokeWidth = stroke,
        )
    }
}

private fun DrawScope.drawDaySeparators(
    layout: WeekViewLayout,
    style: WeekViewStyle,
) {
    val stroke = style.daySeparatorWidthDp.toPx()
    for (index in 0..layout.visibleDates.size) {
        val x = layout.dayStartX(index)
        drawLine(
            color = style.daySeparatorColor,
            start = Offset(x, 0f),
            end = Offset(x, layout.gridHeightPx),
            strokeWidth = stroke,
        )
    }
}

private fun DrawScope.drawNowLine(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    today: LocalDate,
) {
    val todayIndex = layout.visibleDates.indexOf(today)
    if (todayIndex < 0) {
        return
    }

    val now = currentDateTime()
    if (now.hour < style.minHour || now.hour >= style.maxHour) {
        return
    }

    val y = layout.hourY(now.hour, style.minHour) + (now.minute / 60f) * layout.hourHeightPx
    val left = layout.dayStartX(todayIndex)
    val right = left + layout.dayWidthPx
    val stroke = style.nowLineWidthDp.toPx()

    drawLine(
        color = style.nowLineColor,
        start = Offset(left, y),
        end = Offset(right, y),
        strokeWidth = stroke,
    )

    if (style.showNowLineDot) {
        drawCircle(
            color = style.nowLineColor,
            radius = style.nowDotRadiusDp.toPx(),
            center = Offset(left, y),
        )
    }
}

private fun currentDateTime(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

private fun DrawScope.drawRect(color: Color, topLeft: Offset, size: Size) {
    drawRect(color = color, topLeft = topLeft, size = size)
}
