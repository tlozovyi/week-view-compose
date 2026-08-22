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
import kotlinx.datetime.LocalDateTime

/**
 * Month-based paging cache and fetch orchestration.
 *
 * Used by Compose [WeekViewPagingState] and mirrors View `WeekView.PagingAdapter`.
 */
@PublicApi
class WeekViewPagingController<T>(
    startTime: (T) -> LocalDateTime,
    endTime: (T) -> LocalDateTime,
    onLoadMore: (startDate: LocalDate, endDate: LocalDate, submit: (List<T>) -> Unit) -> Unit,
) {
    private var onLoadMore: (
        startDate: LocalDate,
        endDate: LocalDate,
        submit: (List<T>) -> Unit,
    ) -> Unit = onLoadMore
    private val cache = PaginatedEventsCache(startTime = startTime, endTime = endTime)
    private var fetchAnchorDate: LocalDate? = null
    private var displayedItems: List<T> = emptyList()

    val items: List<T>
        get() = displayedItems

    fun updateCallbacks(
        onLoadMore: (startDate: LocalDate, endDate: LocalDate, submit: (List<T>) -> Unit) -> Unit,
    ) {
        this.onLoadMore = onLoadMore
    }

    fun submit(items: List<T>) {
        cache.update(items)
        refreshDisplayedItems()
    }

    fun refresh() {
        cache.clear()
        fetchAnchorDate?.let(::dispatchLoadRequest)
    }

    fun onScrollSettled(firstVisibleDate: LocalDate) {
        dispatchLoadRequest(firstVisibleDate)
    }

    private fun dispatchLoadRequest(firstVisibleDate: LocalDate) {
        fetchAnchorDate = firstVisibleDate
        val fetchRange = FetchRange.create(firstVisibleDate)
        if (fetchRange in cache) {
            refreshDisplayedItems()
            return
        }

        val periodsToFetch = cache.determinePeriodsToFetch(fetchRange)
        if (periodsToFetch.isEmpty()) {
            refreshDisplayedItems()
            return
        }

        for (period in periodsToFetch) {
            cache.reserve(period)
        }
        refreshDisplayedItems()

        for (group in periodsToFetch.groupConsecutivePeriods()) {
            val first = group.first()
            val last = group.last()
            onLoadMore(first.startDate.date, last.endDate.date, ::submit)
        }
    }

    private fun refreshDisplayedItems() {
        displayedItems = fetchAnchorDate?.let { anchor ->
            cache.eventsFor(FetchRange.create(anchor))
        } ?: cache.allEvents
    }
}
