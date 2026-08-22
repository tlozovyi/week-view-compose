/*
 * Copyright 2014 Raquib-ul-Alam
 * Copyright 2018 Till Hellmund
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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

internal const val DRAG_SCROLL_THRESHOLD_PX = 80f

internal enum class DragScrollEdge {
    None,
    Top,
    Bottom,
    Left,
    Right,
}

internal data class WeekViewDragState(
    val eventId: Long,
    val event: WeekViewEvent,
    val sourceChip: EventChip,
    val draggedEventStartTime: LocalDateTime,
    val dragStartTime: LocalDateTime,
    val currentStartTime: LocalDateTime,
    val currentEndTime: LocalDateTime,
) {
    val durationInMinutes: Int
        get() = sourceChip.durationInMinutes
}

/**
 * Snaps [rawEventStart] to the nearest 15-minute increment.
 *
 * Ported from [com.alamkanak.weekview.DragHandler.sanitizeEventStart].
 */
internal fun sanitizeEventStartToQuarterHour(rawEventStart: LocalDateTime): LocalDateTime {
    val minutesBeyondQuarterHour = rawEventStart.minute % 15
    val minutesUntilNextQuarterHour = 15 - minutesBeyondQuarterHour

    return if (minutesBeyondQuarterHour >= 8) {
        rawEventStart.addMinutes(minutesUntilNextQuarterHour)
    } else {
        rawEventStart.addMinutes(-minutesBeyondQuarterHour)
    }
}

internal fun calculateNewEventStart(
    dragState: WeekViewDragState,
    currentDragLocation: LocalDateTime,
): LocalDateTime {
    val delta = dragMinutesBetween(dragState.dragStartTime, currentDragLocation)
    return dragState.draggedEventStartTime.addMinutes(delta)
}

internal fun eventTimesForDraggedStart(
    originalStartTime: LocalDateTime,
    originalEndTime: LocalDateTime,
    sanitizedStartTime: LocalDateTime,
): Pair<LocalDateTime, LocalDateTime> {
    val durationMinutes = dragMinutesBetween(originalStartTime, originalEndTime)
    return sanitizedStartTime to sanitizedStartTime.addMinutes(durationMinutes)
}

/**
 * Maps a touch point in grid-canvas coordinates to a date/time.
 *
 * Ported from [com.alamkanak.weekview.WeekViewTouchHandler.calculateTimeFromPoint].
 */
internal fun calculateTimeFromPoint(
    touchX: Float,
    touchY: Float,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    horizontalTranslationPx: Float,
): LocalDateTime? {
    val contentX = touchX - horizontalTranslationPx

    layout.renderDates.forEachIndexed { dateIndex, date ->
        val dayStartX = layout.dayStartX(dateIndex)
        val dayEndX = dayStartX + layout.dayWidthPx
        if (contentX !in dayStartX..dayEndX) {
            return@forEachIndexed
        }

        val hourHeightPx = layout.hourHeightPx
        if (hourHeightPx <= 0f) {
            return null
        }

        val hourOffset = (touchY / hourHeightPx).toInt()
        val pixelsFromFullHour = touchY - hourOffset * hourHeightPx
        val minutes = ((pixelsFromFullHour / hourHeightPx) * 60).toInt()

        return LocalDateTime(
            date = date,
            time = LocalTime(style.minHour + hourOffset, minutes),
        )
    }

    return null
}

internal fun detectDragScrollEdge(
    touchXInCanvasPx: Float,
    touchYInCanvasPx: Float,
    gridScrollOffsetPx: Float,
    gridViewportWidthPx: Float,
    gridViewportHeightPx: Float,
    scrollThresholdPx: Float = DRAG_SCROLL_THRESHOLD_PX,
): DragScrollEdge {
    val viewportRelativeY = touchYInCanvasPx - gridScrollOffsetPx

    return when {
        viewportRelativeY < scrollThresholdPx -> DragScrollEdge.Top
        gridViewportHeightPx - viewportRelativeY < scrollThresholdPx -> DragScrollEdge.Bottom
        touchXInCanvasPx < scrollThresholdPx -> DragScrollEdge.Left
        gridViewportWidthPx - touchXInCanvasPx < scrollThresholdPx -> DragScrollEdge.Right
        else -> DragScrollEdge.None
    }
}

internal fun verticalDragScrollDeltaPx(hourHeightPx: Float): Float = hourHeightPx / 4f

internal fun canScrollGridUp(gridScrollOffsetPx: Float): Boolean = gridScrollOffsetPx > 0f

internal fun canScrollGridDown(
    gridScrollOffsetPx: Float,
    maxGridScrollOffsetPx: Float,
): Boolean = gridScrollOffsetPx < maxGridScrollOffsetPx

internal fun dragScrollDelayMillis(edge: DragScrollEdge): Long {
    return when (edge) {
        DragScrollEdge.Left, DragScrollEdge.Right -> 600L
        DragScrollEdge.Top, DragScrollEdge.Bottom -> 100L
        DragScrollEdge.None -> 0L
    }
}

internal fun applyVerticalDragAutoScroll(
    edge: DragScrollEdge,
    gridScrollOffsetPx: Float,
    maxGridScrollOffsetPx: Float,
    hourHeightPx: Float,
    currentStartTime: LocalDateTime,
    durationInMinutes: Int,
): Triple<Float, LocalDateTime, LocalDateTime>? {
    val scrollDeltaPx = verticalDragScrollDeltaPx(hourHeightPx)

    return when (edge) {
        DragScrollEdge.Top -> {
            if (!canScrollGridUp(gridScrollOffsetPx)) {
                return null
            }
            val newStart = currentStartTime.addMinutes(-15)
            val newEnd = newStart.addMinutes(durationInMinutes)
            Triple(gridScrollOffsetPx - scrollDeltaPx, newStart, newEnd)
        }
        DragScrollEdge.Bottom -> {
            if (!canScrollGridDown(gridScrollOffsetPx, maxGridScrollOffsetPx)) {
                return null
            }
            val newStart = currentStartTime.addMinutes(15)
            val newEnd = newStart.addMinutes(durationInMinutes)
            Triple(gridScrollOffsetPx + scrollDeltaPx, newStart, newEnd)
        }
        else -> null
    }
}

internal fun applyHorizontalDragAutoScroll(
    edge: DragScrollEdge,
    currentStartTime: LocalDateTime,
    durationInMinutes: Int,
    isLtr: Boolean = true,
): Pair<LocalDateTime, LocalDateTime>? {
    val dayDelta = when (edge) {
        DragScrollEdge.Left -> if (isLtr) -1 else 1
        DragScrollEdge.Right -> if (isLtr) 1 else -1
        else -> return null
    }
    val newStart = currentStartTime.addDays(dayDelta)
    return newStart to newStart.addMinutes(durationInMinutes)
}

private fun dragMinutesBetween(earlier: LocalDateTime, later: LocalDateTime): Int {
    val dayDelta = later.date.toEpochDays() - earlier.date.toEpochDays()
    val earlierMinutes = earlier.hour * 60 + earlier.minute
    val laterMinutes = later.hour * 60 + later.minute
    return (dayDelta * 1_440 + (laterMinutes - earlierMinutes)).toInt()
}

private fun LocalDateTime.addMinutes(minutes: Int): LocalDateTime {
    if (minutes == 0) {
        return this
    }

    var totalMinutes = hour * 60L + minute + minutes
    var dayOffset = 0L

    while (totalMinutes >= 1_440) {
        totalMinutes -= 1_440
        dayOffset += 1
    }
    while (totalMinutes < 0) {
        totalMinutes += 1_440
        dayOffset -= 1
    }

    val newDate = if (dayOffset == 0L) {
        date
    } else {
        LocalDate.fromEpochDays(date.toEpochDays() + dayOffset.toInt())
    }

    return LocalDateTime(
        date = newDate,
        time = LocalTime(
            hour = (totalMinutes / 60).toInt(),
            minute = (totalMinutes % 60).toInt(),
        ),
    )
}

private fun LocalDateTime.addDays(days: Int): LocalDateTime {
    if (days == 0) {
        return this
    }
    return LocalDateTime(
        date = LocalDate.fromEpochDays(date.toEpochDays() + days),
        time = time,
    )
}
