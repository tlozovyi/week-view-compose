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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tlozovyi.weekview.WeekView
import com.tlozovyi.weekview.WeekViewEvent
import com.tlozovyi.weekview.WeekViewEventStyle
import com.tlozovyi.weekview.WeekViewStyle
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn

@Composable
fun SampleApp() {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var firstVisibleDate by remember { mutableStateOf(today) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(padding),
            ) {
                WeekView(
                    events = sampleEvents(today),
                    style = WeekViewStyle(
                        numberOfVisibleDays = 3,
                        minHour = 7,
                        maxHour = 22,
                    ),
                    firstVisibleDate = firstVisibleDate,
                    onFirstVisibleDateChange = { firstVisibleDate = it },
                    onEventClick = { event ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Selected: ${event.title}")
                        }
                    },
                )
            }
        }
    }
}

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() + days)

private fun sampleEvents(today: LocalDate): List<WeekViewEvent> {
    val yesterday = today.plusDays(-1)
    val tomorrow = today.plusDays(1)
    val dayAfterTomorrow = today.plusDays(2)
    val twoDaysAgo = today.plusDays(-2)

    return listOf(
        // Today
        WeekViewEvent(
            id = 1,
            title = "Team standup",
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
            startTime = today.atTime(10, 15),
            endTime = today.atTime(11, 0),
            style = WeekViewEventStyle(backgroundColor = Color(0xFF9575CD)),
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
    )
}
