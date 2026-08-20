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

import kotlinx.datetime.number

/**
 * Formats an hour index (0–23) for the time column.
 *
 * @param hour Hour of day in 24-hour form.
 * @return Localized or custom label shown beside the corresponding grid row.
 */
typealias TimeFormatter = (hour: Int) -> String

/**
 * Formats a visible date for the header row.
 *
 * @param year Calendar year.
 * @param month Month number (1–12).
 * @param dayOfMonth Day of month (1–31).
 * @param dayOfWeekIndex Zero-based index matching ISO weekday order (Monday = 0).
 */
typealias DateFormatter = (year: Int, month: Int, dayOfMonth: Int, dayOfWeekIndex: Int) -> String

/**
 * Default 12-hour time labels for the time column (e.g. `"9 AM"`, `"2 PM"`).
 */
@PublicApi
fun defaultTimeFormatter(hour: Int): String {
    val normalized = hour % 24
    val displayHour = when (normalized) {
        0 -> 12
        in 1..12 -> normalized
        else -> normalized - 12
    }
    val suffix = if (normalized < 12) "AM" else "PM"
    return "$displayHour $suffix"
}

private val FULL_WEEKDAY_NAMES = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
)

private val SHORT_WEEKDAY_NAMES = listOf(
    "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun",
)

/**
 * Default date labels for the header row.
 *
 * Uses full weekday names for single-day mode, short names for 2–6 visible days, and a single
 * letter when seven or more days are shown.
 */
@PublicApi
fun defaultDateFormatter(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    dayOfWeekIndex: Int,
    numberOfVisibleDays: Int,
): String {
    val weekday = when (numberOfVisibleDays) {
        1 -> FULL_WEEKDAY_NAMES[dayOfWeekIndex]
        in 2..6 -> SHORT_WEEKDAY_NAMES[dayOfWeekIndex]
        else -> SHORT_WEEKDAY_NAMES[dayOfWeekIndex].take(1)
    }
    return "$weekday $month/$dayOfMonth"
}

internal fun defaultDateFormatter(
    date: kotlinx.datetime.LocalDate,
    numberOfVisibleDays: Int,
): String {
    val dayOfWeekIndex = date.dayOfWeek.ordinal
    return defaultDateFormatter(
        year = date.year,
        month = date.month.number,
        dayOfMonth = date.dayOfMonth,
        dayOfWeekIndex = dayOfWeekIndex,
        numberOfVisibleDays = numberOfVisibleDays,
    )
}

internal fun dayOfWeekIndex(date: kotlinx.datetime.LocalDate): Int = date.dayOfWeek.ordinal
