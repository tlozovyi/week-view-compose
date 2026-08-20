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

import androidx.compose.ui.unit.Density

/**
 * Computes pixel bounds for [EventChip] instances within the day grid.
 *
 * Coordinates are relative to the grid canvas (excluding the date header).
 */
internal class EventChipBoundsCalculator(
    private val layout: WeekViewLayout,
    private val style: WeekViewStyle,
    private val density: Density,
) {
    private val hourHeightPx = layout.hourHeightPx
    private val hoursPerDay = style.hoursCount
    private val minutesPerDay = hoursPerDay * 60
    private val drawableDayWidth = layout.drawableDayWidthPx
    private val overlappingEventGapPx = style.overlappingEventGapDp.toPx(density)
    private val eventMarginVerticalPx = style.eventMarginVerticalDp.toPx(density)
    private val singleDayHorizontalPaddingPx = style.singleDayHorizontalPaddingDp.toPx(density)
    private val isSingleDay = style.numberOfVisibleDays == 1

    fun calculateSingleEvent(
        eventChip: EventChip,
        dayStartX: Float,
    ): ChipBounds {
        val minutesFromStart = eventChip.minutesFromStartHour
        val top = calculateDistanceFromTop(minutesFromStart)

        val bottomMinutesFromStart = minutesFromStart + eventChip.durationInMinutes
        var bottom = calculateDistanceFromTop(bottomMinutesFromStart)

        val partialEventEndsAtEndOfDay = eventChip.endTime.isAtEndOfPeriod(hour = style.maxHour)
        val fullEventContinuesOnNextDay = eventChip.endsOnLaterDay

        if (!(partialEventEndsAtEndOfDay && fullEventContinuesOnNextDay)) {
            bottom -= eventMarginVerticalPx
        }

        var left = dayStartX + eventChip.relativeStart * drawableDayWidth
        var right = left + eventChip.relativeWidth * drawableDayWidth

        if (left > dayStartX) {
            left += overlappingEventGapPx / 2f
        }

        if (right < dayStartX + drawableDayWidth) {
            right -= overlappingEventGapPx / 2f
        }

        val hasNoOverlaps = right == dayStartX + drawableDayWidth
        if (isSingleDay && hasNoOverlaps) {
            right -= singleDayHorizontalPaddingPx * 2f
        }

        return ChipBounds(left = left, top = top, right = right, bottom = bottom)
    }

    fun calculateAllDayEvent(
        rowIndex: Int,
        eventChip: EventChip,
        dayStartX: Float,
    ): ChipBounds {
        val headerPaddingPx = style.headerPaddingDp.toPx(density)
        val chipHeightPx = layout.allDayChipHeightPx
        val eventMarginVerticalPx = style.eventMarginVerticalDp.toPx(density)
        val arrangeVertically = style.arrangeAllDayEventsVertically

        val top = if (arrangeVertically) {
            layout.dateLabelHeightPx + headerPaddingPx +
                rowIndex * (chipHeightPx + eventMarginVerticalPx)
        } else {
            layout.dateLabelHeightPx + headerPaddingPx
        }

        var left = if (arrangeVertically) {
            dayStartX
        } else {
            dayStartX + eventChip.relativeStart * drawableDayWidth
        }

        var right = if (arrangeVertically) {
            left + drawableDayWidth
        } else {
            left + eventChip.relativeWidth * drawableDayWidth
        }

        val isLeftMostColumn = left == dayStartX
        val isRightMostColumn = right == dayStartX + drawableDayWidth

        if (!isLeftMostColumn) {
            left += overlappingEventGapPx / 2f
        }

        if (!isRightMostColumn) {
            right -= overlappingEventGapPx / 2f
        }

        val bottom = top + chipHeightPx

        if (isSingleDay && isRightMostColumn) {
            return ChipBounds(
                left = left,
                top = top,
                right = right - singleDayHorizontalPaddingPx * 2f,
                bottom = bottom,
            )
        }

        return ChipBounds(left = left, top = top, right = right, bottom = bottom)
    }

    private fun calculateDistanceFromTop(minutesFromStart: Int): Float {
        val portionOfDay = minutesFromStart.toFloat() / minutesPerDay
        return hourHeightPx * hoursPerDay * portionOfDay
    }
}

internal fun ChipBounds.isValid(gridWidthPx: Float, gridHeightPx: Float): Boolean {
    return right > 0f && left < gridWidthPx && bottom > 0f && top < gridHeightPx
}

internal fun ChipBounds.isValidHorizontally(contentWidthPx: Float): Boolean {
    return right > 0f && left < contentWidthPx
}

internal fun ChipBounds.width(): Float = right - left

internal fun ChipBounds.height(): Float = bottom - top

internal fun List<EventChip>.calculateBoundsForDate(
    dateIndex: Int,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    density: Density,
) {
    val calculator = EventChipBoundsCalculator(layout, style, density)
    val dayStartX = layout.dayStartX(dateIndex)
    val modifiedDayStartX = if (style.numberOfVisibleDays == 1) {
        dayStartX + style.singleDayHorizontalPaddingDp.toPx(density)
    } else {
        dayStartX
    }

    for (eventChip in this) {
        val bounds = calculator.calculateSingleEvent(eventChip, modifiedDayStartX)
        if (bounds.isValid(layout.contentGridWidthPx, layout.gridHeightPx)) {
            eventChip.bounds = bounds
        } else {
            eventChip.bounds.setEmpty()
        }
    }
}
