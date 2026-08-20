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
import kotlinx.datetime.LocalDate

internal fun allDayChipHeightPx(
    style: WeekViewStyle,
    density: Density,
): Float {
    return with(density) { style.allDayEventTextSizeSp.toPx() } +
        style.allDayEventPaddingVerticalDp.toPx(density) * 2f
}

internal fun calculateAllDaySectionHeightPx(
    style: WeekViewStyle,
    density: Density,
    visibleRowCount: Int,
): Float {
    if (visibleRowCount <= 0) {
        return 0f
    }

    val chipHeightPx = allDayChipHeightPx(style, density)
    val eventMarginVerticalPx = style.eventMarginVerticalDp.toPx(density)
    val headerPaddingPx = style.headerPaddingDp.toPx(density)
    val rowsHeight = visibleRowCount * chipHeightPx +
        (visibleRowCount - 1) * eventMarginVerticalPx

    return headerPaddingPx + rowsHeight + headerPaddingPx
}

internal fun visibleAllDayRowCount(
    maxAllDayEventsPerDay: Int,
    allDayEventsExpanded: Boolean,
    arrangeAllDayEventsVertically: Boolean,
): Int {
    if (maxAllDayEventsPerDay <= 0) {
        return 0
    }
    if (!arrangeAllDayEventsVertically) {
        return 1
    }
    if (allDayEventsExpanded) {
        return maxAllDayEventsPerDay
    }
    return minOf(maxAllDayEventsPerDay, 2)
}

internal fun showAllDayEventsToggleArrow(
    maxAllDayEventsPerDay: Int,
    arrangeAllDayEventsVertically: Boolean,
): Boolean {
    return arrangeAllDayEventsVertically && maxAllDayEventsPerDay > 2
}

internal fun maxAllDayEventsPerDay(
    allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    renderDates: List<LocalDate>,
): Int {
    return renderDates.maxOfOrNull { date ->
        allDayChipsByDate[date].orEmpty().size
    } ?: 0
}

internal fun visibleAllDayChipsForDate(
    chips: List<EventChip>,
    useExpandedAllDayLayout: Boolean,
    arrangeAllDayEventsVertically: Boolean,
): List<EventChip> {
    val sorted = chips.sortedBy { it.startTime }
    if (!arrangeAllDayEventsVertically || useExpandedAllDayLayout || sorted.size <= 2) {
        return sorted
    }
    return sorted.take(1)
}

internal fun applyAllDayEventVisibility(
    allDayEventChips: List<EventChip>,
    allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    renderDates: List<LocalDate>,
    allDayEventsExpanded: Boolean,
    arrangeAllDayEventsVertically: Boolean,
) {
    allDayEventChips.forEach { it.isHidden = false }

    for (date in renderDates) {
        val events = allDayChipsByDate[date].orEmpty()
        val visible = visibleAllDayChipsForDate(
            chips = events,
            useExpandedAllDayLayout = allDayEventsExpanded,
            arrangeAllDayEventsVertically = arrangeAllDayEventsVertically,
        )
        events.filter { it !in visible }.forEach { it.isHidden = true }
    }
}

internal fun maxAllDayRowsPerDay(
    allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    renderDates: List<LocalDate>,
    arrangeAllDayEventsVertically: Boolean,
): Int {
    val maxEvents = maxAllDayEventsPerDay(allDayChipsByDate, renderDates)
    if (maxEvents == 0) {
        return 0
    }
    return if (arrangeAllDayEventsVertically) {
        maxEvents
    } else {
        1
    }
}

internal fun WeekViewLayout.withAllDaySection(
    maxAllDayEventsPerDay: Int,
    allDayEventsExpanded: Boolean,
    style: WeekViewStyle,
    density: Density,
): WeekViewLayout {
    val dateLabelHeightPx = with(density) { style.headerHeightDp.toPx() }
    val visibleRowCount = visibleAllDayRowCount(
        maxAllDayEventsPerDay = maxAllDayEventsPerDay,
        allDayEventsExpanded = allDayEventsExpanded,
        arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
    )
    val allDaySectionHeightPx = calculateAllDaySectionHeightPx(
        style = style,
        density = density,
        visibleRowCount = visibleRowCount,
    )
    val allDayChipHeightPx = allDayChipHeightPx(style, density)

    return copy(
        dateLabelHeightPx = dateLabelHeightPx,
        allDaySectionHeightPx = allDaySectionHeightPx,
        allDayChipHeightPx = allDayChipHeightPx,
        maxAllDayRowsPerDay = maxAllDayEventsPerDay,
        headerHeightPx = dateLabelHeightPx + allDaySectionHeightPx,
    )
}

internal fun WeekViewLayout.withAnimatedAllDaySection(
    maxAllDayEventsPerDay: Int,
    expandProgress: Float,
    style: WeekViewStyle,
    density: Density,
): WeekViewLayout {
    val dateLabelHeightPx = with(density) { style.headerHeightDp.toPx() }
    val chipHeightPx = allDayChipHeightPx(style, density)
    val collapsedRows = visibleAllDayRowCount(
        maxAllDayEventsPerDay = maxAllDayEventsPerDay,
        allDayEventsExpanded = false,
        arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
    )
    val expandedRows = visibleAllDayRowCount(
        maxAllDayEventsPerDay = maxAllDayEventsPerDay,
        allDayEventsExpanded = true,
        arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
    )
    val collapsedHeight = calculateAllDaySectionHeightPx(style, density, collapsedRows)
    val expandedHeight = calculateAllDaySectionHeightPx(style, density, expandedRows)
    val progress = expandProgress.coerceIn(0f, 1f)
    val animatedSectionHeight = collapsedHeight + (expandedHeight - collapsedHeight) * progress

    return copy(
        dateLabelHeightPx = dateLabelHeightPx,
        allDaySectionHeightPx = animatedSectionHeight,
        allDayChipHeightPx = chipHeightPx,
        maxAllDayRowsPerDay = maxAllDayEventsPerDay,
        headerHeightPx = dateLabelHeightPx + animatedSectionHeight,
    )
}

internal fun shouldUseExpandedAllDayLayout(
    showAllDayToggle: Boolean,
    allDayEventsExpanded: Boolean,
    expandProgress: Float,
): Boolean {
    return !showAllDayToggle || allDayEventsExpanded || expandProgress > 0f
}
