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

import com.tlozovyi.weekview.WeekViewStyle
import kotlinx.datetime.DayOfWeek

internal enum class SampleViewMode(
    val title: String,
    val description: String,
    val style: WeekViewStyle,
    val showsStaticNavigation: Boolean = false,
) {
    ThreeDaySnapping(
        title = "3 days · snap",
        description = "Default scrollable view with 3-day page snap and adaptive chip text",
        style = WeekViewStyle(
            numberOfVisibleDays = 3,
            minHour = 7,
            maxHour = 22,
            horizontalScrollSnapEnabled = true,
            adaptiveEventTextSize = true,
        ),
    ),
    SevenDaySnapping(
        title = "7 days · snap",
        description = "Full week with calendar-week snap on release",
        style = WeekViewStyle(
            numberOfVisibleDays = 7,
            minHour = 7,
            maxHour = 22,
            horizontalScrollSnapEnabled = true,
            adaptiveEventTextSize = true,
            firstDayOfWeek = DayOfWeek.MONDAY,
        ),
    ),
    SevenDayFreeScroll(
        title = "7 days · free scroll",
        description = "Full week without snap-on-release",
        style = WeekViewStyle(
            numberOfVisibleDays = 7,
            minHour = 7,
            maxHour = 22,
            horizontalScrollSnapEnabled = false,
        ),
    ),
    StaticWeek(
        title = "Static week",
        description = "7-day view without horizontal scrolling",
        style = WeekViewStyle(
            numberOfVisibleDays = 7,
            minHour = 7,
            maxHour = 22,
            horizontalScrollingEnabled = false,
            horizontalScrollSnapEnabled = false,
            firstDayOfWeek = DayOfWeek.MONDAY,
        ),
        showsStaticNavigation = true,
    ),
    LimitedHours(
        title = "Limited hours",
        description = "3-day view limited to 8:00–20:00",
        style = WeekViewStyle(
            numberOfVisibleDays = 3,
            minHour = 8,
            maxHour = 20,
            horizontalScrollSnapEnabled = true,
            adaptiveEventTextSize = true,
        ),
    ),
    HorizontalAllDay(
        title = "Horizontal all-day",
        description = "3-day view with overlapping all-day chips",
        style = WeekViewStyle(
            numberOfVisibleDays = 3,
            minHour = 7,
            maxHour = 22,
            arrangeAllDayEventsVertically = false,
            horizontalScrollSnapEnabled = true,
            adaptiveEventTextSize = true,
        ),
    ),
    FixedEventText(
        title = "Fixed text size",
        description = "3-day view with adaptiveEventTextSize disabled (compare chip labels)",
        style = WeekViewStyle(
            numberOfVisibleDays = 3,
            minHour = 7,
            maxHour = 22,
            horizontalScrollSnapEnabled = true,
            adaptiveEventTextSize = false,
        ),
    ),
}
