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

/** Extra day columns rendered on each side of the viewport for smooth horizontal scrolling. */
internal const val HORIZONTAL_SCROLL_BUFFER_DAYS = 1

internal fun applyHorizontalScrollDelta(
    offsetPx: Float,
    deltaPx: Float,
    dayWidthPx: Float,
    firstVisibleDate: LocalDate,
    onFirstVisibleDateChange: (LocalDate) -> Unit,
): Float {
    if (dayWidthPx <= 0f) {
        return offsetPx
    }

    var offset = offsetPx + deltaPx
    var date = firstVisibleDate
    var dateChanged = false

    while (offset <= -dayWidthPx) {
        offset += dayWidthPx
        date = date.plusDays(1)
        dateChanged = true
    }
    while (offset >= dayWidthPx) {
        offset -= dayWidthPx
        date = date.plusDays(-1)
        dateChanged = true
    }

    if (dateChanged) {
        onFirstVisibleDateChange(date)
    }
    return offset
}

internal fun horizontalContentTranslationPx(
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    scrollBufferDays: Int = HORIZONTAL_SCROLL_BUFFER_DAYS,
): Float {
    return -scrollBufferDays * dayWidthPx + scrollOffsetPx
}

internal fun dayColumnScreenBounds(
    columnIndex: Int,
    dayWidthPx: Float,
    horizontalTranslationPx: Float,
): Pair<Float, Float> {
    val left = columnIndex * dayWidthPx + horizontalTranslationPx
    return left to (left + dayWidthPx)
}

internal fun isDayColumnVisibleOnScreen(
    screenLeft: Float,
    screenRight: Float,
    viewportWidthPx: Float,
): Boolean {
    return screenRight > 0f && screenLeft < viewportWidthPx
}

/** Content-space X for the now dot: today's divider, pinned to the time-column edge when scrolled off left. */
internal fun nowDotCenterContentX(
    columnLeft: Float,
    horizontalTranslationPx: Float,
): Float {
    return maxOf(columnLeft, -horizontalTranslationPx)
}

/** Screen-space X for [nowDotCenterContentX] (0 = time-column divider). */
internal fun nowDotCenterScreenX(
    columnLeft: Float,
    horizontalTranslationPx: Float,
): Float {
    return nowDotCenterContentX(columnLeft, horizontalTranslationPx) + horizontalTranslationPx
}

/** Whether the now dot should be drawn for today's column at the current horizontal scroll. */
internal fun isNowDotVisibleOnScreen(
    screenLeft: Float,
    screenRight: Float,
    viewportWidthPx: Float,
): Boolean {
    return isDayColumnVisibleOnScreen(screenLeft, screenRight, viewportWidthPx)
}

/** Whether any part of today's column intersects the grid viewport (uses [renderDates], not [WeekViewLayout.visibleDates]). */
internal fun isTodayColumnVisibleOnScreen(
    layout: WeekViewLayout,
    today: LocalDate,
    horizontalTranslationPx: Float,
): Boolean {
    val todayIndex = layout.dateIndex(today)
    if (todayIndex < 0) {
        return false
    }
    val (screenLeft, screenRight) = dayColumnScreenBounds(
        columnIndex = todayIndex,
        dayWidthPx = layout.dayWidthPx,
        horizontalTranslationPx = horizontalTranslationPx,
    )
    return isDayColumnVisibleOnScreen(
        screenLeft = screenLeft,
        screenRight = screenRight,
        viewportWidthPx = layout.viewportGridWidthPx,
    )
}

internal fun headerLabelCenteredScreenX(
    screenLeft: Float,
    screenRight: Float,
    textWidth: Float,
    paddingHorizontalPx: Float,
): Float {
    val columnWidth = (screenRight - screenLeft) - paddingHorizontalPx * 2f
    return screenLeft + paddingHorizontalPx + (columnWidth - textWidth) / 2f
}

internal fun buildRenderDates(
    firstVisibleDate: LocalDate,
    numberOfVisibleDays: Int,
    scrollBufferDays: Int,
): List<LocalDate> {
    if (scrollBufferDays <= 0) {
        return buildList {
            for (index in 0 until numberOfVisibleDays) {
                add(firstVisibleDate.plusDays(index))
            }
        }
    }

    return buildList {
        for (index in -scrollBufferDays until numberOfVisibleDays + scrollBufferDays) {
            add(firstVisibleDate.plusDays(index))
        }
    }
}

internal data class ExternalFirstVisibleDateSync(
    val pageOriginDate: LocalDate,
    val anchorDate: LocalDate,
    val scrollOffsetPx: Float,
    val anchorGenerationBump: Boolean,
)

/**
 * Applies a [firstVisibleDate] update from the caller without disturbing in-progress free scroll.
 *
 * When [firstVisibleDate] already matches the internal anchor (typical scroll commit), scroll
 * offset is preserved. External navigation (date changes from the caller) still resets to the
 * resolved page origin with zero offset.
 */
internal fun syncExternalFirstVisibleDate(
    firstVisibleDate: LocalDate,
    currentAnchorDate: LocalDate,
    currentScrollOffsetPx: Float,
    numberOfVisibleDays: Int,
    firstDayOfWeek: DayOfWeek,
): ExternalFirstVisibleDateSync {
    val pageOriginDate = horizontalPageOriginDate(
        firstVisibleDate = firstVisibleDate,
        numberOfVisibleDays = numberOfVisibleDays,
        firstDayOfWeek = firstDayOfWeek,
    )
    if (firstVisibleDate == currentAnchorDate) {
        return ExternalFirstVisibleDateSync(
            pageOriginDate = pageOriginDate,
            anchorDate = currentAnchorDate,
            scrollOffsetPx = currentScrollOffsetPx,
            anchorGenerationBump = false,
        )
    }

    val needsReset = pageOriginDate != currentAnchorDate || currentScrollOffsetPx != 0f
    return ExternalFirstVisibleDateSync(
        pageOriginDate = pageOriginDate,
        anchorDate = if (needsReset) pageOriginDate else currentAnchorDate,
        scrollOffsetPx = if (needsReset) 0f else currentScrollOffsetPx,
        anchorGenerationBump = needsReset,
    )
}
