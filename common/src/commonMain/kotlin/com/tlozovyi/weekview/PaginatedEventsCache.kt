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

import kotlinx.datetime.LocalDateTime

/**
 * Month-bucketed cache used by the Compose paging state.
 *
 * Ported from View `PaginatedEventsCache`.
 */
internal class PaginatedEventsCache<T>(
    private val startTime: (T) -> LocalDateTime,
    private val endTime: (T) -> LocalDateTime,
) {

    private val eventsByPeriod = mutableMapOf<Period, MutableList<T>>()

    val allEvents: List<T>
        get() = eventsByPeriod.values.flatten()

    fun eventsFor(fetchRange: FetchRange): List<T> {
        val rangeStart = fetchRange.previous.startDate
        val rangeEnd = fetchRange.next.endDate
        return allEvents.filter { item ->
            endTime(item) >= rangeStart && startTime(item) <= rangeEnd
        }
    }

    fun update(events: List<T>) {
        val groupedEvents = events.groupBy { Period.fromDate(startTime(it)) }
        for ((period, periodEvents) in groupedEvents) {
            eventsByPeriod[period] = periodEvents.toMutableList()
        }
    }

    fun determinePeriodsToFetch(range: FetchRange): List<Period> {
        return range.periods.filter { period -> period !in this }
    }

    operator fun contains(period: Period): Boolean = eventsByPeriod.containsKey(period)

    operator fun contains(range: FetchRange): Boolean = range.periods.all { it in this }

    fun reserve(period: Period) {
        eventsByPeriod[period] = mutableListOf()
    }

    fun clear() {
        eventsByPeriod.clear()
    }
}

internal fun List<Period>.groupConsecutivePeriods(): List<List<Period>> {
    if (isEmpty()) {
        return emptyList()
    }
    val groups = mutableListOf<MutableList<Period>>()
    for (period in this) {
        val lastPeriodInGroup = groups.lastOrNull()?.lastOrNull()
        val isConsecutive = lastPeriodInGroup?.next == period
        if (groups.isEmpty() || !isConsecutive) {
            groups += mutableListOf(period)
        } else {
            groups.last().add(period)
        }
    }
    return groups
}
