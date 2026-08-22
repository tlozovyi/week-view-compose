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
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlin.test.Test
import kotlin.test.assertEquals

class WeekViewPagingControllerTest {

    @Test
    fun paginatedCache_replacesEventsByMonth() {
        val controller = WeekViewPagingController<TestEvent>(
            startTime = { it.startTime },
            endTime = { it.endTime },
            onLoadMore = { _, _, _ -> },
        )
        controller.submit(
            listOf(
                TestEvent(LocalDate(2026, 8, 15).atTime(10, 0)),
            ),
        )
        controller.submit(
            listOf(
                TestEvent(LocalDate(2026, 9, 1).atTime(10, 0)),
            ),
        )
        assertEquals(2, controller.items.size)
        assertEquals(
            setOf(8, 9),
            controller.items.map { it.startTime.month.number }.toSet(),
        )
    }

    @Test
    fun controller_requestsMissingMonthsOnScrollSettled() {
        val requestedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()
        val controller = WeekViewPagingController<TestEvent>(
            startTime = { it.startTime },
            endTime = { it.endTime },
            onLoadMore = { start, end, submit ->
                requestedRanges += start to end
                submit(listOf(TestEvent(LocalDate(2026, 8, 10).atTime(10, 0))))
            },
        )

        controller.onScrollSettled(LocalDate(2026, 8, 21))

        assertEquals(1, requestedRanges.size)
        assertEquals(LocalDate(2026, 7, 1), requestedRanges.single().first)
        assertEquals(LocalDate(2026, 9, 30), requestedRanges.single().second)
        assertEquals(1, controller.items.size)
    }

    @Test
    fun groupConsecutivePeriods_mergesAdjacentMonths() {
        val august = Period.fromDate(LocalDate(2026, 8, 1).atTime(0, 0))
        val september = Period.fromDate(LocalDate(2026, 9, 1).atTime(0, 0))
        val november = Period.fromDate(LocalDate(2026, 11, 1).atTime(0, 0))

        val groups = listOf(august, september, november).groupConsecutivePeriods()
        assertEquals(2, groups.size)
        assertEquals(listOf(august, september), groups[0])
        assertEquals(listOf(november), groups[1])
    }

    private data class TestEvent(val startTime: LocalDateTime) {
        val endTime: LocalDateTime = startTime
    }
}
