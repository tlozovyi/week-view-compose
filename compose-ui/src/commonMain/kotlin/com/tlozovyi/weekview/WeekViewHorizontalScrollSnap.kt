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
    isLtr: Boolean = true,
): Float {
    val dayDelta = columnOffsetBetween(anchorDate, referenceDate, isLtr)
    return dayDelta * dayWidthPx + scrollOffsetPx
}

/**
 * Whole-day shift of [referenceDate]'s screen X (see [referenceColumnScreenX]).
 * Truncates toward zero so forward vs backward scroll need the same distance before a day counts.
 */
internal fun dayShiftFromReferenceScreenX(
    referenceScreenX: Float,
    dayWidthPx: Float,
): Int {
    if (dayWidthPx <= 0f) {
        return 0
    }
    return (-referenceScreenX / dayWidthPx).toInt()
}

/** Scroll-origin date relative to [gesturePageStart] (View library's `currentDate`). */
internal fun horizontalScrollLeadingDate(
    gesturePageStart: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    isLtr: Boolean = true,
): LocalDate {
    if (dayWidthPx <= 0f) {
        return gesturePageStart
    }
    val referenceScreenX = referenceColumnScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        referenceDate = gesturePageStart,
        dayWidthPx = dayWidthPx,
        isLtr = isLtr,
    )
    val dayShift = dayShiftFromReferenceScreenX(referenceScreenX, dayWidthPx)
    return dateAtColumnOffset(gesturePageStart, dayShift, isLtr)
}

/** Page start containing the current scroll position. */
internal fun currentPageStartDate(
    pageOriginDate: LocalDate,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
    isLtr: Boolean = true,
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
        isLtr = isLtr,
    )
    val daysFromOrigin = dayShiftFromReferenceScreenX(screenX, dayWidthPx)
    val pageIndex = if (daysFromOrigin >= 0) {
        daysFromOrigin / numberOfVisibleDays
    } else {
        (daysFromOrigin - numberOfVisibleDays + 1) / numberOfVisibleDays
    }
    return dateAtColumnOffset(origin, pageIndex * numberOfVisibleDays, isLtr)
}

/** Visible horizontal page width in pixels (one snap “range”). */
internal fun horizontalPageWidthPx(
    dayWidthPx: Float,
    numberOfVisibleDays: Int,
): Float = dayWidthPx * numberOfVisibleDays

/** Portion of [horizontalPageWidthPx] dragged before release snaps to the adjacent page (22%). */
internal const val HORIZONTAL_PAGE_SNAP_THRESHOLD_FRACTION = 0.22f

internal fun horizontalPageSnapThresholdPx(pageWidthPx: Float): Float {
    if (pageWidthPx <= 0f) {
        return Float.MAX_VALUE
    }
    return pageWidthPx * HORIZONTAL_PAGE_SNAP_THRESHOLD_FRACTION
}

internal fun shouldSnapToAdjacentHorizontalPage(
    gesturePageReferenceScreenX: Float,
    pageWidthPx: Float,
): Boolean {
    return abs(gesturePageReferenceScreenX) > horizontalPageSnapThresholdPx(pageWidthPx)
}

internal fun pageTargetForLeadingDate(
    gesturePageStart: LocalDate,
    leadingDate: LocalDate,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
    isLtr: Boolean = true,
): LocalDate {
    val scrolledToPast = if (isLtr) {
        leadingDate < gesturePageStart
    } else {
        leadingDate > gesturePageStart
    }
    return if (scrolledToPast) {
        if (numberOfVisibleDays >= 7) {
            if (isLtr) {
                leadingDate.previousFirstDayOfWeek(firstDayOfWeek)
            } else {
                leadingDate.nextFirstDayOfWeek(firstDayOfWeek)
            }
        } else {
            dateAtColumnOffset(gesturePageStart, -numberOfVisibleDays, isLtr)
        }
    } else {
        if (numberOfVisibleDays >= 7) {
            if (isLtr) {
                leadingDate.nextFirstDayOfWeek(firstDayOfWeek)
            } else {
                leadingDate.previousFirstDayOfWeek(firstDayOfWeek)
            }
        } else {
            dateAtColumnOffset(gesturePageStart, numberOfVisibleDays, isLtr)
        }
    }
}

/** Snaps to the nearest day column (`goToNearestDay` in the View library). */
internal fun snapToNearestDayColumn(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    isLtr: Boolean = true,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(anchorDate, 0f)
    }
    val roundedDays = (scrollOffsetPx / dayWidthPx).roundToInt()
    return HorizontalScrollSnapTarget(
        anchorDate = dateAtColumnOffset(anchorDate, -roundedDays, isLtr),
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
    isLtr: Boolean = true,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f || numberOfVisibleDays <= 0) {
        return HorizontalScrollSnapTarget(anchorDate, scrollOffsetPx)
    }
    val gesturePageReferenceScreenX = referenceColumnScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        referenceDate = gesturePageStart,
        dayWidthPx = dayWidthPx,
        isLtr = isLtr,
    )
    val pageWidthPx = horizontalPageWidthPx(dayWidthPx, numberOfVisibleDays)
    val targetDate = if (
        shouldSnapToAdjacentHorizontalPage(
            gesturePageReferenceScreenX = gesturePageReferenceScreenX,
            pageWidthPx = pageWidthPx,
        )
    ) {
        val scrolledTowardFuture = if (isLtr) {
            gesturePageReferenceScreenX < 0f
        } else {
            gesturePageReferenceScreenX > 0f
        }
        val leadingDate = if (scrolledTowardFuture) {
            dateAtColumnOffset(gesturePageStart, 1, isLtr)
        } else {
            dateAtColumnOffset(gesturePageStart, -1, isLtr)
        }
        pageTargetForLeadingDate(
            gesturePageStart = gesturePageStart,
            leadingDate = leadingDate,
            numberOfVisibleDays = numberOfVisibleDays,
            firstDayOfWeek = firstDayOfWeek,
            isLtr = isLtr,
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
    isLtr: Boolean = true,
): HorizontalScrollSnapTarget {
    return snapToVisibleDaysPage(
        gesturePageStart = gesturePageStart,
        anchorDate = anchorDate,
        scrollOffsetPx = scrollOffsetPx,
        dayWidthPx = dayWidthPx,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
        isLtr = isLtr,
    )
}

internal fun scrollStateFromReferenceScreenX(
    referenceColumnScreenX: Float,
    referenceDate: LocalDate,
    dayWidthPx: Float,
    isLtr: Boolean = true,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(referenceDate, 0f)
    }
    return normalizeHorizontalScrollOffset(
        anchorDate = referenceDate,
        scrollOffsetPx = referenceColumnScreenX,
        dayWidthPx = dayWidthPx,
        isLtr = isLtr,
    )
}

internal fun normalizeHorizontalScrollOffset(
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    isLtr: Boolean = true,
): HorizontalScrollSnapTarget {
    if (dayWidthPx <= 0f) {
        return HorizontalScrollSnapTarget(anchorDate, 0f)
    }

    var date = anchorDate
    var offset = scrollOffsetPx
    while (offset >= dayWidthPx) {
        offset -= dayWidthPx
        date = dateAtColumnOffset(date, -1, isLtr)
    }
    while (offset <= -dayWidthPx) {
        offset += dayWidthPx
        date = dateAtColumnOffset(date, 1, isLtr)
    }
    return HorizontalScrollSnapTarget(date, offset)
}
