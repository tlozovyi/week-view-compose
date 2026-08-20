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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

class WeekViewEventHitTestTest {

    @Test
    fun findEventAtReturnsTopmostChipInDrawOrder() {
        val date = LocalDate(2026, 8, 20)
        val eventA = sampleEvent(id = 1, date = date, hour = 10)
        val eventB = sampleEvent(id = 2, date = date, hour = 10)
        val chips = listOf(
            eventChip(eventA, left = 0f, top = 100f, right = 50f, bottom = 200f),
            eventChip(eventB, left = 60f, top = 100f, right = 120f, bottom = 200f),
        )

        assertEquals(eventB, chips.findEventAt(x = 90f, y = 150f))
        assertEquals(eventA, chips.findEventAt(x = 25f, y = 150f))
        assertNull(chips.findEventAt(x = 200f, y = 150f))
    }

    @Test
    fun pageByDaysMovesByVisibleDayCount() {
        val date = LocalDate(2026, 8, 20)
        assertEquals(LocalDate(2026, 8, 23), date.pageByDays(3))
        assertEquals(LocalDate(2026, 8, 17), date.pageByDays(-3))
    }
}

private fun sampleEvent(id: Long, date: LocalDate, hour: Int): WeekViewEvent {
    return WeekViewEvent(
        id = id,
        title = "Event $id",
        startTime = date.atTime(hour, 0),
        endTime = date.atTime(hour + 1, 0),
    )
}

private fun eventChip(
    event: WeekViewEvent,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): EventChip {
    return EventChip(
        event = ResolvedWeekViewEntity.Event(
            id = event.id,
            title = event.title,
            startTime = event.startTime,
            endTime = event.endTime,
            subtitle = null,
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = event,
        ),
        index = 0,
        startTime = event.startTime,
        endTime = event.endTime,
    ).apply {
        bounds = ChipBounds(left = left, top = top, right = right, bottom = bottom)
    }
}
