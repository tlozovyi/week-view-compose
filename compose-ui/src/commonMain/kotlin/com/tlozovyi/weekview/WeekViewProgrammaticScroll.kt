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

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.math.abs

/** Clamps a programmatic horizontal scroll target to optional [minDate] / [maxDate] bounds. */
internal fun clampProgrammaticScrollDate(
    date: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    numberOfVisibleDays: Int,
): LocalDate {
    val min = minDate
    val max = maxDate
    return when {
        min != null && date < min -> min
        max != null && date > max -> max.plusDays(-(numberOfVisibleDays - 1).coerceAtLeast(0))
        else -> date
    }
}

/** Resolves anchor date + zero offset so [targetDate] is visible (View `getStartDateInAllowedRange`). */
internal fun horizontalScrollTargetForDate(
    targetDate: LocalDate,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): HorizontalScrollSnapTarget {
    val anchorDate = horizontalPageOriginDate(
        firstVisibleDate = targetDate,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
    )
    return HorizontalScrollSnapTarget(anchorDate = anchorDate, scrollOffsetPx = 0f)
}

internal fun firstVisibleDateFromScrollState(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): LocalDate {
    if (dayWidthPx <= 0f) {
        return anchorDate
    }
    val pageOrigin = horizontalPageOriginDate(
        firstVisibleDate = anchorDate,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
    )
    return horizontalScrollLeadingDate(
        gesturePageStart = pageOrigin,
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        dayWidthPx = dayWidthPx,
    )
}

/**
 * Vertical scroll offset for a time on the grid.
 *
 * Ported from View `WeekView.scrollToTime` — one hour of padding above the target when possible.
 */
internal fun scrollOffsetForTime(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    hour: Int,
    minute: Int,
    gridViewportHeightPx: Float,
    maxGridScrollOffsetPx: Float,
): Float {
    val sanitizedHour = hour.coerceIn(style.minHour, style.maxHour - 1)
    val sanitizedMinute = minute.coerceIn(0, 59)

    val scrollHour: Int
    val scrollMinute: Int
    if (sanitizedHour > style.minHour) {
        scrollHour = sanitizedHour - 1
        scrollMinute = 0
    } else {
        scrollHour = sanitizedHour
        scrollMinute = 0
    }

    val y = layout.hourY(scrollHour, style.minHour) +
        (scrollMinute / 60f) * layout.hourHeightPx
    return y.coerceIn(0f, maxGridScrollOffsetPx)
}

internal fun isSameHorizontalScrollTarget(
    currentAnchorDate: LocalDate,
    currentScrollOffsetPx: Float,
    target: HorizontalScrollSnapTarget,
): Boolean {
    return currentAnchorDate == target.anchorDate &&
        abs(currentScrollOffsetPx - target.scrollOffsetPx) < 0.5f
}

internal fun targetDateScreenX(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    targetDate: LocalDate,
    dayWidthPx: Float,
): Float {
    return referenceColumnScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        referenceDate = targetDate,
        dayWidthPx = dayWidthPx,
    )
}
