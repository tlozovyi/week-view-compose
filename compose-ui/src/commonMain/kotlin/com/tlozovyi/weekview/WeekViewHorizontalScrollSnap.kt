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
import kotlin.math.roundToInt

internal data class HorizontalScrollSnapTarget(
    val anchorDate: LocalDate,
    val scrollOffsetPx: Float,
)

/**
 * Aligns [date] to the first day of its calendar week.
 */
internal fun alignStartDateToWeek(
    date: LocalDate,
    firstDayOfWeek: DayOfWeek,
): LocalDate {
    return date.plusDays(-date.differenceWithFirstDayOfWeek(firstDayOfWeek))
}

internal fun LocalDate.differenceWithFirstDayOfWeek(firstDayOfWeek: DayOfWeek): Int {
    return if (firstDayOfWeek == DayOfWeek.MONDAY && dayOfWeek == DayOfWeek.SUNDAY) {
        6
    } else {
        dayOfWeek.ordinal - firstDayOfWeek.ordinal
    }
}

internal fun LocalDate.previousFirstDayOfWeek(firstDayOfWeek: DayOfWeek): LocalDate {
    var result = plusDays(-1)
    while (result.dayOfWeek != firstDayOfWeek) {
        result = result.plusDays(-1)
    }
    return result
}

internal fun LocalDate.nextFirstDayOfWeek(firstDayOfWeek: DayOfWeek): LocalDate {
    var result = plusDays(1)
    while (result.dayOfWeek != firstDayOfWeek) {
        result = result.plusDays(1)
    }
    return result
}

/** Resolves the page-grid origin (week-aligned when showing 7+ days). */
internal fun horizontalPageOriginDate(
    firstVisibleDate: LocalDate,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): LocalDate {
    return if (numberOfVisibleDays >= 7) {
        alignStartDateToWeek(firstVisibleDate, firstDayOfWeek)
    } else {
        firstVisibleDate
    }
}

/**
 * Screen-space X of [referenceDate]'s leading edge in the grid viewport (0 = time-column divider).
 */
internal fun referenceColumnScreenX(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    referenceDate: LocalDate,
    dayWidthPx: Float,
): Float {
    val dayDelta = referenceDate.toEpochDays() - anchorDate.toEpochDays()
    return dayDelta * dayWidthPx + scrollOffsetPx
}

/** Scroll-origin date relative to [gesturePageStart] (View library's `currentDate`). */
internal fun horizontalScrollLeadingDate(
    gesturePageStart: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
): LocalDate {
    if (dayWidthPx <= 0f) {
        return gesturePageStart
    }
    val referenceScreenX = referenceColumnScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        referenceDate = gesturePageStart,
        dayWidthPx = dayWidthPx,
    )
    val dayShift = kotlin.math.floor(-referenceScreenX / dayWidthPx + 1e-4f).toInt()
    return gesturePageStart.plusDays(dayShift)
}

/** Page start containing the current scroll position. */
internal fun currentPageStartDate(
    pageOriginDate: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): LocalDate {
    val origin = horizontalPageOriginDate(
        firstVisibleDate = pageOriginDate,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
    )
    if (dayWidthPx <= 0f || numberOfVisibleDays <= 0) {
        return origin
    }
    val screenX = referenceColumnScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        referenceDate = origin,
        dayWidthPx = dayWidthPx,
    )
    val daysFromOrigin = kotlin.math.floor(-screenX / dayWidthPx + 1e-4f).toInt()
    val pageIndex = if (daysFromOrigin >= 0) {
        daysFromOrigin / numberOfVisibleDays
    } else {
        (daysFromOrigin - numberOfVisibleDays + 1) / numberOfVisibleDays
    }
    return origin.plusDays(pageIndex * numberOfVisibleDays)
}

/** Lower threshold than half-page rounding so paging feels closer to the View library. */
internal fun horizontalSnapThresholdDays(numberOfVisibleDays: Int): Int {
    return ((numberOfVisibleDays / 2) - 1).coerceAtLeast(0)
}

internal fun pageTargetForLeadingDate(
    gesturePageStart: LocalDate,
    leadingDate: LocalDate,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): LocalDate {
    return if (leadingDate < gesturePageStart) {
        if (numberOfVisibleDays >= 7) {
            leadingDate.previousFirstDayOfWeek(firstDayOfWeek)
        } else {
            gesturePageStart.plusDays(-numberOfVisibleDays)
        }
    } else {
        if (numberOfVisibleDays >= 7) {
            leadingDate.nextFirstDayOfWeek(firstDayOfWeek)
        } else {
            gesturePageStart.plusDays(numberOfVisibleDays)
        }
    }
}

/** Snaps to the nearest day column (`goToNearestDay` in the View library). */
internal fun snapToNearestDayColumn(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(anchorDate, 0f)
    }
    val roundedDays = (scrollOffsetPx / dayWidthPx).roundToInt()
    return HorizontalScrollSnapTarget(
        anchorDate = anchorDate.plusDays(-roundedDays),
        scrollOffsetPx = 0f,
    )
}

/**
 * Snaps to an adjacent [numberOfVisibleDays] page when scrolled far enough.
 *
 * Uses the gesture-start page as reference (View library's stale `firstVisibleDate` on release).
 */
internal fun snapToVisibleDaysPage(
    gesturePageStart: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f || numberOfVisibleDays <= 0) {
        return HorizontalScrollSnapTarget(anchorDate, scrollOffsetPx)
    }
    val leadingDate = horizontalScrollLeadingDate(
        gesturePageStart = gesturePageStart,
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        dayWidthPx = dayWidthPx,
    )
    val daysScrolled = abs(leadingDate.toEpochDays() - gesturePageStart.toEpochDays())
    val threshold = horizontalSnapThresholdDays(numberOfVisibleDays)
    val targetDate = if (daysScrolled > threshold) {
        pageTargetForLeadingDate(
            gesturePageStart = gesturePageStart,
            leadingDate = leadingDate,
            numberOfVisibleDays = numberOfVisibleDays,
            firstDayOfWeek = firstDayOfWeek,
        )
    } else {
        gesturePageStart
    }
    return HorizontalScrollSnapTarget(targetDate, 0f)
}

internal fun snapHorizontalScrollTarget(
    gesturePageStart: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): HorizontalScrollSnapTarget {
    return snapToVisibleDaysPage(
        gesturePageStart = gesturePageStart,
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        dayWidthPx = dayWidthPx,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
    )
}

internal fun scrollStateFromReferenceScreenX(
    referenceColumnScreenX: Float,
    referenceDate: LocalDate,
    dayWidthPx: Float,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(referenceDate, 0f)
    }
    return normalizeHorizontalScrollOffset(
        anchorDate = referenceDate,
        scrollOffsetPx = referenceColumnScreenX,
        dayWidthPx = dayWidthPx,
    )
}

internal fun normalizeHorizontalScrollOffset(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(anchorDate, 0f)
    }

    var date = anchorDate
    var offset = scrollOffsetPx
    while (offset >= dayWidthPx) {
        offset -= dayWidthPx
        date = date.plusDays(-1)
    }
    while (offset <= -dayWidthPx) {
        offset += dayWidthPx
        date = date.plusDays(1)
    }
    return HorizontalScrollSnapTarget(date, offset)
}
