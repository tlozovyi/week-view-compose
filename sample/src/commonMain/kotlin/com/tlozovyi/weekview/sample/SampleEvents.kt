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

package com.tlozovyi.weekview.sample

import androidx.compose.ui.graphics.Color
import com.tlozovyi.weekview.WeekViewBlockedTime
import com.tlozovyi.weekview.WeekViewEvent
import com.tlozovyi.weekview.WeekViewEventStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

internal fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)

internal fun sampleEvents(today: LocalDate): List<WeekViewEvent> {
    val yesterday = today.plusDays(-1)
    val tomorrow = today.plusDays(1)
    val dayAfterTomorrow = today.plusDays(2)
    val twoDaysAgo = today.plusDays(-2)
    val threeDaysAgo = today.plusDays(-3)
    val fourDaysAgo = today.plusDays(-4)
    val fiveDaysAgo = today.plusDays(-5)
    val sixDaysAgo = today.plusDays(-6)
    val sevenDaysAgo = today.plusDays(-7)
    val threeDaysAhead = today.plusDays(3)
    val fourDaysAhead = today.plusDays(4)
    val fiveDaysAhead = today.plusDays(5)
    val sixDaysAhead = today.plusDays(6)
    val sevenDaysAhead = today.plusDays(7)
    val tenDaysAhead = today.plusDays(10)
    val fourteenDaysAhead = today.plusDays(14)

    return listOf(
        // Today
        WeekViewEvent(
            id = 1,
            title = "Team standup",
            subtitle = "Standup room",
            startTime = today.atTime(9, 0),
            endTime = today.atTime(9, 30),
        ),
        WeekViewEvent(
            id = 2,
            title = "Design review",
            startTime = today.atTime(14, 0),
            endTime = today.atTime(15, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF81C784)),
        ),
        WeekViewEvent(
            id = 3,
            title = "Overlapping A",
            startTime = today.atTime(10, 0),
            endTime = today.atTime(11, 30),
        ),
        WeekViewEvent(
            id = 4,
            title = "Overlapping B",
            startTime = today.atTime(10, 30),
            endTime = today.atTime(12, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFFFB74D)),
        ),
        WeekViewEvent(
            id = 5,
            title = "1:1",
            subtitle = "Safe space room",
            startTime = today.atTime(10, 15),
            endTime = today.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF9575CD)),
        ),
        WeekViewEvent(
            id = 501,
            title = "Quarterly planning review",
            subtitle = "Building A · Room 12",
            startTime = today.atTime(10, 45),
            endTime = today.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFE57373)),
        ),
        WeekViewEvent(
            id = 6,
            title = "Morning workout",
            startTime = today.atTime(7, 30),
            endTime = today.atTime(8, 15),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF4DD0E1)),
        ),
        WeekViewEvent(
            id = 7,
            title = "Product sync",
            subtitle = "Roadmap Q4",
            startTime = today.atTime(11, 30),
            endTime = today.atTime(12, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF64B5F6)),
        ),
        WeekViewEvent(
            id = 8,
            title = "Lunch",
            startTime = today.atTime(12, 30),
            endTime = today.atTime(13, 15),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFAED581)),
        ),
        WeekViewEvent(
            id = 9,
            title = "Focus time",
            startTime = today.atTime(13, 30),
            endTime = today.atTime(15, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF90A4AE)),
        ),
        WeekViewEvent(
            id = 10,
            title = "Code review",
            startTime = today.atTime(15, 30),
            endTime = today.atTime(16, 15),
        ),
        WeekViewEvent(
            id = 11,
            title = "Demo prep",
            startTime = today.atTime(16, 0),
            endTime = today.atTime(17, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFFF8A65)),
        ),
        WeekViewEvent(
            id = 12,
            title = "Team retro",
            startTime = today.atTime(17, 0),
            endTime = today.atTime(18, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFBA68C8)),
        ),
        WeekViewEvent(
            id = 101,
            title = "Sprint demo",
            subtitle = "All hands",
            startTime = today.atTime(0, 0),
            endTime = today.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFFEF5350)),
        ),
        WeekViewEvent(
            id = 102,
            title = "Focus day",
            startTime = today.atTime(0, 0),
            endTime = today.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFF26A69A)),
        ),
        WeekViewEvent(
            id = 104,
            title = "Company offsite",
            startTime = today.atTime(0, 0),
            endTime = today.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFFAB47BC)),
        ),
        WeekViewEvent(
            id = 105,
            title = "Submit expenses",
            startTime = today.atTime(0, 0),
            endTime = today.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFFFFCA28)),
        ),
        WeekViewEvent(
            id = 106,
            title = "Birthday",
            startTime = today.atTime(0, 0),
            endTime = today.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFFEC407A)),
        ),

        // Tomorrow
        WeekViewEvent(
            id = 13,
            title = "Sprint planning",
            startTime = tomorrow.atTime(9, 0),
            endTime = tomorrow.atTime(10, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF7986CB)),
        ),
        WeekViewEvent(
            id = 14,
            title = "Client call",
            subtitle = "Acme Corp",
            startTime = tomorrow.atTime(11, 0),
            endTime = tomorrow.atTime(12, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF4DB6AC)),
        ),
        WeekViewEvent(
            id = 15,
            title = "Workshop",
            startTime = tomorrow.atTime(13, 0),
            endTime = tomorrow.atTime(15, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF9575CD)),
        ),
        WeekViewEvent(
            id = 16,
            title = "QA handoff",
            startTime = tomorrow.atTime(15, 0),
            endTime = tomorrow.atTime(15, 45),
        ),
        WeekViewEvent(
            id = 17,
            title = "Release checklist",
            startTime = tomorrow.atTime(16, 30),
            endTime = tomorrow.atTime(17, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFE57373)),
        ),
        WeekViewEvent(
            id = 103,
            title = "Conference",
            startTime = tomorrow.atTime(0, 0),
            endTime = dayAfterTomorrow.plusDays(1).atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFF5C6BC0)),
        ),

        // Day after tomorrow
        WeekViewEvent(
            id = 18,
            title = "Dentist",
            startTime = dayAfterTomorrow.atTime(8, 0),
            endTime = dayAfterTomorrow.atTime(9, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF4FC3F7)),
        ),
        WeekViewEvent(
            id = 19,
            title = "Architecture review",
            startTime = dayAfterTomorrow.atTime(10, 0),
            endTime = dayAfterTomorrow.atTime(11, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF81C784)),
        ),
        WeekViewEvent(
            id = 20,
            title = "Pair programming",
            startTime = dayAfterTomorrow.atTime(13, 0),
            endTime = dayAfterTomorrow.atTime(14, 30),
        ),
        WeekViewEvent(
            id = 21,
            title = "Dinner with friends",
            startTime = dayAfterTomorrow.atTime(19, 0),
            endTime = dayAfterTomorrow.atTime(21, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFFFB74D)),
        ),

        // Yesterday
        WeekViewEvent(
            id = 22,
            title = "Budget review",
            startTime = yesterday.atTime(10, 0),
            endTime = yesterday.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF90CAF9)),
        ),
        WeekViewEvent(
            id = 23,
            title = "UX critique",
            startTime = yesterday.atTime(14, 0),
            endTime = yesterday.atTime(15, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFCE93D8)),
        ),
        WeekViewEvent(
            id = 24,
            title = "Write-up",
            startTime = yesterday.atTime(16, 0),
            endTime = yesterday.atTime(17, 30),
        ),

        // Two days ago
        WeekViewEvent(
            id = 25,
            title = "All-hands",
            startTime = twoDaysAgo.atTime(9, 0),
            endTime = twoDaysAgo.atTime(10, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF64B5F6)),
        ),
        WeekViewEvent(
            id = 26,
            title = "Interview",
            startTime = twoDaysAgo.atTime(11, 0),
            endTime = twoDaysAgo.atTime(12, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFA1887F)),
        ),
        WeekViewEvent(
            id = 27,
            title = "Bug bash",
            startTime = twoDaysAgo.atTime(14, 0),
            endTime = twoDaysAgo.atTime(16, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFE57373)),
        ),

        // Previous week
        WeekViewEvent(
            id = 28,
            title = "Planning poker",
            startTime = threeDaysAgo.atTime(10, 0),
            endTime = threeDaysAgo.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF7986CB)),
        ),
        WeekViewEvent(
            id = 29,
            title = "Stakeholder sync",
            startTime = fourDaysAgo.atTime(13, 0),
            endTime = fourDaysAgo.atTime(14, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF4DB6AC)),
        ),
        WeekViewEvent(
            id = 30,
            title = "Tech talk",
            startTime = fiveDaysAgo.atTime(16, 0),
            endTime = fiveDaysAgo.atTime(17, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF9575CD)),
        ),
        WeekViewEvent(
            id = 31,
            title = "Grooming",
            startTime = sixDaysAgo.atTime(9, 30),
            endTime = sixDaysAgo.atTime(10, 30),
        ),
        WeekViewEvent(
            id = 32,
            title = "Release retro",
            startTime = sevenDaysAgo.atTime(15, 0),
            endTime = sevenDaysAgo.atTime(16, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFBA68C8)),
        ),
        WeekViewEvent(
            id = 107,
            title = "Holiday",
            startTime = sevenDaysAgo.atTime(0, 0),
            endTime = sixDaysAgo.atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFF26C6DA)),
        ),

        // Next week
        WeekViewEvent(
            id = 33,
            title = "Vendor demo",
            startTime = threeDaysAhead.atTime(11, 0),
            endTime = threeDaysAhead.atTime(12, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF64B5F6)),
        ),
        WeekViewEvent(
            id = 34,
            title = "Security review",
            startTime = fourDaysAhead.atTime(9, 0),
            endTime = fourDaysAhead.atTime(10, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFE57373)),
        ),
        WeekViewEvent(
            id = 35,
            title = "Training session",
            startTime = fiveDaysAhead.atTime(13, 0),
            endTime = fiveDaysAhead.atTime(15, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF81C784)),
        ),
        WeekViewEvent(
            id = 36,
            title = "Happy hour",
            startTime = fiveDaysAhead.atTime(18, 0),
            endTime = fiveDaysAhead.atTime(19, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFFFFB74D)),
        ),
        WeekViewEvent(
            id = 37,
            title = "Ops handover",
            startTime = sixDaysAhead.atTime(10, 0),
            endTime = sixDaysAhead.atTime(10, 45),
        ),
        WeekViewEvent(
            id = 38,
            title = "Quarterly review",
            startTime = sevenDaysAhead.atTime(14, 0),
            endTime = sevenDaysAhead.atTime(15, 30),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF7986CB)),
        ),
        WeekViewEvent(
            id = 108,
            title = "Team offsite",
            startTime = sevenDaysAhead.atTime(0, 0),
            endTime = tenDaysAhead.atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFF5C6BC0)),
        ),

        // Two weeks ahead
        WeekViewEvent(
            id = 39,
            title = "Board prep",
            startTime = tenDaysAhead.atTime(9, 0),
            endTime = tenDaysAhead.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF90A4AE)),
        ),
        WeekViewEvent(
            id = 40,
            title = "Investor call",
            startTime = fourteenDaysAhead.atTime(16, 0),
            endTime = fourteenDaysAhead.atTime(17, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF4FC3F7)),
        ),
        WeekViewEvent(
            id = 109,
            title = "Hackathon",
            startTime = tenDaysAhead.atTime(0, 0),
            endTime = fourteenDaysAhead.atTime(0, 0),
            isAllDay = true,
            style = WeekViewEventStyle(backgroundColor = Color(0xFFAB47BC)),
        ),
    )
}

internal fun sampleBlockedTimes(today: LocalDate): List<WeekViewBlockedTime> {
    return listOf(
        WeekViewBlockedTime(
            id = 9001,
            startTime = today.atTime(12, 0),
            endTime = today.atTime(13, 0),
            title = "Lunch break",
        ),
        WeekViewBlockedTime(
            id = 9002,
            startTime = today.atTime(18, 0),
            endTime = today.atTime(19, 0),
        ),
    )
}
