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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlozovyi.weekview.WeekView
import com.tlozovyi.weekview.WeekViewEvent
import com.tlozovyi.weekview.WeekViewStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime

@Composable
fun SampleApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WeekView(
                events = sampleEvents(),
                style = WeekViewStyle(
                    numberOfVisibleDays = 3,
                    minHour = 7,
                    maxHour = 22,
                ),
            )
        }
    }
}

private fun sampleEvents(): List<WeekViewEvent> {
    val date = LocalDate(2026, 8, 20)
    return listOf(
        WeekViewEvent(
            id = 1,
            title = "Team standup",
            startTime = date.atTime(9, 0),
            endTime = date.atTime(9, 30),
        ),
        WeekViewEvent(
            id = 2,
            title = "Design review",
            startTime = date.atTime(14, 0),
            endTime = date.atTime(15, 0),
        ),
    )
}
