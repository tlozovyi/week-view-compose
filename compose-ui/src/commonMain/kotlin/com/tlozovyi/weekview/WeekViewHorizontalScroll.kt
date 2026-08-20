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

import kotlinx.datetime.LocalDate

/**
 * Extra day columns rendered on each side of the viewport.
 *
 * Needs to be at least 2 so one full column sits off-screen on each edge at rest
 * (with 1 buffer day the edge column starts exactly at the viewport boundary).
 */
internal const val HORIZONTAL_SCROLL_BUFFER_DAYS = 2

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
