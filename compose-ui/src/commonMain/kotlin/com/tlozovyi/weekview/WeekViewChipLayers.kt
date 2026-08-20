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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import kotlinx.datetime.LocalDate

/** Timed and all-day [EventChip] lists grouped by date for rendering. */
internal data class WeekViewChipLayers(
    val eventChips: List<EventChip>,
    val allDayEventChips: List<EventChip>,
    val chipsByDate: Map<LocalDate, List<EventChip>>,
    val allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    val maxAllDayEventsPerDay: Int,
)

@Composable
internal fun rememberWeekViewChipLayers(
    events: List<WeekViewEvent>,
    eventDateRange: List<LocalDate>,
    anchorGeneration: Int,
    style: WeekViewStyle,
    layoutConfig: WeekViewLayoutConfig,
    layoutEngine: WeekViewLayoutEngine,
    renderDates: List<LocalDate>,
    density: Density,
): WeekViewChipLayers {
    val allEventChips = remember(events, eventDateRange, anchorGeneration, style, layoutConfig, density) {
        val resolvedEvents = events.toResolvedEntities(style, density, eventDateRange)
        layoutEngine.createEventChips(resolvedEvents, layoutConfig)
    }

    val eventChips = remember(allEventChips) {
        allEventChips.filter { !it.event.isAllDay }
    }

    val allDayEventChips = remember(allEventChips) {
        allEventChips.filter { it.event.isAllDay }
    }

    val chipsByDate = remember(eventChips) {
        eventChips.groupBy { it.startTime.date }
    }

    val allDayChipsByDate = remember(allDayEventChips) {
        allDayEventChips.groupBy { it.startTime.date }
    }

    val maxAllDayEventsPerDay = remember(allDayChipsByDate, renderDates) {
        maxAllDayEventsPerDay(
            allDayChipsByDate = allDayChipsByDate,
            renderDates = renderDates,
        )
    }

    return WeekViewChipLayers(
        eventChips = eventChips,
        allDayEventChips = allDayEventChips,
        chipsByDate = chipsByDate,
        allDayChipsByDate = allDayChipsByDate,
        maxAllDayEventsPerDay = maxAllDayEventsPerDay,
    )
}
