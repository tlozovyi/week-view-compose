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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

class WeekViewTextFitterTest {

    @Test
    fun combineEventChipTextUsesNewlineForTimedEvents() {
        assertEquals(
            "Meeting\nRoom 4B",
            combineEventChipText(
                title = "Meeting",
                subtitle = "Room 4B",
                isAllDay = false,
            ),
        )
    }

    @Test
    fun combineEventChipTextUsesSpaceForAllDayEvents() {
        assertEquals(
            "Holiday Office closed",
            combineEventChipText(
                title = "Holiday",
                subtitle = "Office closed",
                isAllDay = true,
            ),
        )
    }

    @Test
    fun combineEventChipTextReturnsTitleWhenSubtitleBlank() {
        assertEquals(
            "Standup",
            combineEventChipText(
                title = "Standup",
                subtitle = "  ",
                isAllDay = false,
            ),
        )
    }

    @Test
    fun eventChipTextOmitsSubtitleWhenDisabled() {
        val entity = ResolvedWeekViewEntity.Event(
            id = 1,
            title = "Demo",
            subtitle = "Details",
            startTime = LocalDate(2026, 8, 20).atTime(9, 0),
            endTime = LocalDate(2026, 8, 20).atTime(10, 0),
            isAllDay = false,
            style = ResolvedWeekViewEntity.Style(),
            data = null,
        )

        assertEquals("Demo", eventChipText(entity, includeSubtitle = false))
        assertEquals("Demo\nDetails", eventChipText(entity, includeSubtitle = true))
    }
}
