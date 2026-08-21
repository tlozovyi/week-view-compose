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

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal fun LocalDate.isWeekend(): Boolean {
    return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
}

/** ISO-8601 week number (matches typical `Calendar.WEEK_OF_YEAR` badge usage). */
internal fun LocalDate.weekOfYear(): Int {
    val daysFromMonday = (dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val mondayOfWeek = plusDays(-daysFromMonday)
    val jan4 = LocalDate(year, 1, 4)
    val jan4DaysFromMonday = (jan4.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val firstWeekMonday = jan4.plusDays(-jan4DaysFromMonday)
    return ((mondayOfWeek.toEpochDays() - firstWeekMonday.toEpochDays()) / 7).toInt() + 1
}

internal fun ResolvedWeekViewEntity.shouldFlattenMultiDayCorners(): Boolean {
    return !isAllDay && startTime.date != endTime.date
}
