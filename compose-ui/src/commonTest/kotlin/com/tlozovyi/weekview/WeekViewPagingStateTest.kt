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
import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals

class WeekViewPagingStateTest {

    @Test
    fun visibleDateRange_ordersDatesChronologicallyInRtl() {
        val range = visibleDateRange(
            firstVisibleDate = LocalDate(2026, 8, 21),
            numberOfVisibleDays = 3,
            isLtr = false,
        )
        assertEquals(LocalDate(2026, 8, 19), range.first)
        assertEquals(LocalDate(2026, 8, 21), range.second)
    }

    @Test
    fun ensureLoaded_requestsMissingMonthsWithoutWaitingForScrollSettlement() {
        val requestedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()
        val state = WeekViewPagingState()
        state.updateCallbacks(
            onLoadMore = { start, end, submit ->
                requestedRanges += start to end
                submit(emptyList())
            },
            onRangeChanged = null,
        )

        state.ensureLoaded(
            firstVisibleDate = LocalDate(2026, 8, 21),
            numberOfVisibleDays = 3,
            isLtr = true,
        )

        assertEquals(1, requestedRanges.size)
        assertEquals(LocalDate(2026, 7, 1), requestedRanges.single().first)
        assertEquals(LocalDate(2026, 9, 30), requestedRanges.single().second)
    }

    @Test
    fun submitCallback_publishesEventsWithoutWaitingForScroll() {
        val state = WeekViewPagingState()
        state.updateCallbacks(
            onLoadMore = { _, _, submit ->
                submit(
                    listOf(
                        WeekViewEvent(
                            id = 1L,
                            title = "Task",
                            startTime = LocalDate(2026, 8, 21).atTime(10, 0),
                            endTime = LocalDate(2026, 8, 21).atTime(11, 0),
                        ),
                    ),
                )
            },
            onRangeChanged = null,
        )

        state.ensureLoaded(
            firstVisibleDate = LocalDate(2026, 8, 21),
            numberOfVisibleDays = 3,
            isLtr = true,
        )

        assertEquals(1, state.events.size)
        assertEquals("Task", state.events.single().title)
    }

    @Test
    fun asyncSubmitCallback_publishesEventsWhenLoaderCompletes() {
        val state = WeekViewPagingState()
        var capturedSubmit: WeekViewPagingSubmit? = null
        state.updateCallbacks(
            onLoadMore = { _, _, submit ->
                capturedSubmit = submit
            },
            onRangeChanged = null,
        )

        state.ensureLoaded(
            firstVisibleDate = LocalDate(2026, 8, 21),
            numberOfVisibleDays = 3,
            isLtr = true,
        )

        assertEquals(0, state.events.size)

        capturedSubmit?.invoke(
            listOf(
                WeekViewEvent(
                    id = 2L,
                    title = "Async task",
                    startTime = LocalDate(2026, 8, 21).atTime(14, 0),
                    endTime = LocalDate(2026, 8, 21).atTime(15, 0),
                ),
            ),
        )

        assertEquals(1, state.events.size)
        assertEquals("Async task", state.events.single().title)
    }
}
