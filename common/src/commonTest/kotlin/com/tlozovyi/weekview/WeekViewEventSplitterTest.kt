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

import com.tlozovyi.weekview.util.Event
import com.tlozovyi.weekview.util.createResolvedWeekViewEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class WeekViewEventSplitterTest {

    private val config = WeekViewLayoutConfig()

    @Test
    fun singleDayEventIsNotSplit() {
        val layoutConfig = config.copy(minHour = 0, maxHour = 24)

        val startTime = todayDateTime().withHour(11)
        val endTime = startTime.plusHours(2)
        val event = createResolvedWeekViewEvent(startTime, endTime)

        val results = event.split(layoutConfig)
        assertEquals(listOf(event), results)
    }

    @Test
    fun singleDayEventBeforeRangeIsIgnored() {
        val layoutConfig = config.copy(minHour = 7, maxHour = 21)

        val event = createResolvedWeekViewEvent(
            startTime = todayDateTime().withHour(1),
            endTime = todayDateTime().withHour(2),
        )
        val results = event.split(layoutConfig)

        assertEquals(emptyList<ResolvedWeekViewEntity>(), results)
    }

    @Test
    fun singleDayEventAfterRangeIsIgnored() {
        val layoutConfig = config.copy(minHour = 7, maxHour = 21)

        val event = createResolvedWeekViewEvent(
            startTime = todayDateTime().withHour(22),
            endTime = todayDateTime().plusDays(1).withHour(6),
        )

        val results = event.split(layoutConfig)
        assertEquals(emptyList<ResolvedWeekViewEntity>(), results)
    }

    @Test
    fun earlySingleDayEventPartiallyOutOfRangeIsAdjustedCorrectly() {
        val layoutConfig = config.copy(minHour = 10, maxHour = 20)

        val startTime = todayDateTime().withHour(8)
        val endTime = todayDateTime().withHour(12)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val expected = listOf(
            Event(todayDateTime().withHour(10), todayDateTime().withHour(12)),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun lateSingleDayEventPartiallyOutOfRangeIsAdjustedCorrectly() {
        val layoutConfig = config.copy(minHour = 10, maxHour = 20)

        val startTime = todayDateTime().withHour(18)
        val endTime = todayDateTime().withHour(23)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val expected = listOf(
            Event(startTime, todayDateTime().withTimeAtEndOfPeriod(20)),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun twoDayEventInRangeIsSplitCorrectly() {
        val layoutConfig = config.copy(minHour = 0, maxHour = 24)

        val startTime = todayDateTime().withHour(11)
        val endTime = startTime.plusDays(1).withHour(2)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val expected = listOf(
            Event(startTime, startTime.atEndOfDay),
            Event(endTime.atStartOfDay, endTime),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun twoDayEventOutOfRangeIsSplitCorrectly() {
        val minHour = 7
        val maxHour = 21
        val layoutConfig = config.copy(minHour = minHour, maxHour = maxHour)

        val startTime = todayDateTime().withHour(5)
        val endTime = startTime.plusDays(2).withHour(23)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val tomorrow = todayDateTime().plusDays(1)
        val expected = listOf(
            Event(startTime.withHour(minHour), startTime.withTimeAtEndOfPeriod(maxHour)),
            Event(tomorrow.withHour(minHour), tomorrow.withTimeAtEndOfPeriod(maxHour)),
            Event(endTime.withHour(minHour), endTime.withTimeAtEndOfPeriod(maxHour)),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun twoDayEventEndingBeforeRangeStartIsSplitCorrectly() {
        val minHour = 7
        val maxHour = 21
        val layoutConfig = config.copy(minHour = minHour, maxHour = maxHour)

        val startTime = todayDateTime().withHour(8)
        val endTime = startTime.plusDays(1).withHour(5)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val expected = listOf(
            Event(startTime, startTime.withTimeAtEndOfPeriod(maxHour)),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun twoDayEventStartingAfterRangeEndIsSplitCorrectly() {
        val minHour = 7
        val maxHour = 21
        val layoutConfig = config.copy(minHour = minHour, maxHour = maxHour)

        val startTime = todayDateTime().withHour(22)
        val endTime = startTime.plusDays(1).withHour(9)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val expected = listOf(
            Event(endTime.withHour(minHour), endTime),
        )

        assertTimeRanges(expected, results)
    }

    @Test
    fun threeDayEventIsSplitCorrectly() {
        val layoutConfig = config.copy(minHour = 0, maxHour = 24)

        val startTime = todayDateTime().withHour(11)
        val endTime = startTime.plusDays(2).withHour(2)

        val event = createResolvedWeekViewEvent(startTime, endTime)
        val results = event.split(layoutConfig)

        val intermediateDate = startTime.plusDays(1)
        val expected = listOf(
            Event(startTime, startTime.atEndOfDay),
            Event(intermediateDate.atStartOfDay, intermediateDate.atEndOfDay),
            Event(endTime.atStartOfDay, endTime),
        )

        assertTimeRanges(expected, results)
    }

    private fun assertTimeRanges(expected: List<Event>, results: List<ResolvedWeekViewEntity>) {
        val expectedTimes = expected.map { it.startTime to it.endTime }
        val resultTimes = results.map { it.startTime to it.endTime }
        assertEquals(expectedTimes, resultTimes)
    }
}
