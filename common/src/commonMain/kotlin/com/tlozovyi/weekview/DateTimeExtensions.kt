/*
 * Copyright 2014 Raquib-ul-Alam
 * Copyright 2018 Till Hellmund
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

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

internal fun dateTimeAt(
    date: LocalDate,
    hour: Int,
    minute: Int = 0,
    second: Int = 0,
    nanosecond: Int = 0,
): LocalDateTime = LocalDateTime(date, LocalTime(hour, minute, second, nanosecond))

internal fun LocalDateTime.isEqual(other: LocalDateTime): Boolean = this == other

internal fun LocalDateTime.isNotEqual(other: LocalDateTime): Boolean = !isEqual(other)

internal fun LocalDateTime.plusDays(days: Int): LocalDateTime {
    return dateTimeAt(date.plus(days, DateTimeUnit.DAY), hour, minute, second, nanosecond)
}

internal fun LocalDateTime.addDays(days: Int): LocalDateTime = plusDays(days)

internal fun LocalDateTime.plusHours(hours: Int): LocalDateTime = adjustTime(hours = hours)

internal fun LocalDateTime.plusMinutes(minutes: Int): LocalDateTime = adjustTime(minutes = minutes)

internal fun LocalDateTime.minusMillis(millis: Int): LocalDateTime = adjustTime(millis = -millis)

internal fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other

internal fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other

internal fun LocalDateTime.toEpochDays(): Int = date.toEpochDays()

internal infix fun LocalDateTime.minutesUntil(other: LocalDateTime): Int {
    return (other.totalMinutesOfDay() - totalMinutesOfDay()).toInt()
}

private fun LocalDateTime.totalMinutesOfDay(): Long {
    return date.toEpochDays().toLong() * 1_440 + hour * 60 + minute
}

internal fun LocalDateTime.withTimeAtStartOfPeriod(hour: Int): LocalDateTime {
    return dateTimeAt(date, hour, 0, 0, 0)
}

internal fun LocalDateTime.withTimeAtEndOfPeriod(hour: Int): LocalDateTime {
    return if (hour >= 24) {
        dateTimeAt(date, 23, 59, 59, 0)
    } else {
        dateTimeAt(date, hour - 1, 59, 59, 0)
    }
}

internal fun LocalDateTime.isAtStartOfPeriod(hour: Int): Boolean = isEqual(withTimeAtStartOfPeriod(hour))

internal val LocalDateTime.atStartOfDay: LocalDateTime
    get() = withTimeAtStartOfPeriod(0)

internal val LocalDateTime.atEndOfDay: LocalDateTime
    get() = withTimeAtEndOfPeriod(24)

internal fun LocalDateTime.isSameDate(other: LocalDateTime): Boolean = date == other.date

internal fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

internal fun todayDateTime(): LocalDateTime = today().atStartOfDay()

internal fun firstDayOfYear(): LocalDateTime = LocalDate(today().year, 1, 1).atStartOfDay()

internal fun LocalDateTime.withYear(year: Int): LocalDateTime {
    return dateTimeAt(LocalDate(year, date.month.number, date.dayOfMonth), hour, minute, second, nanosecond)
}

internal fun LocalDateTime.withMonth(month: Int): LocalDateTime {
    return dateTimeAt(LocalDate(date.year, month, date.dayOfMonth), hour, minute, second, nanosecond)
}

internal fun LocalDateTime.withHour(hour: Int): LocalDateTime {
    return dateTimeAt(date, hour, minute, second, nanosecond)
}

internal fun LocalDate.atStartOfDay(): LocalDateTime = atTime(0, 0)

internal fun newDate(year: Int, month: Int, dayOfMonth: Int): LocalDateTime {
    return LocalDate(year, month, dayOfMonth).atStartOfDay()
}

internal val LocalDate.lengthOfMonth: Int
    get() {
        val nextMonth = if (month.number == 12) {
            LocalDate(year + 1, 1, 1)
        } else {
            LocalDate(year, month.number + 1, 1)
        }
        return nextMonth.toEpochDays() - toEpochDays()
    }

internal val LocalDateTime.monthNumber: Int
    get() = date.month.number

internal val LocalDateTime.year: Int
    get() = date.year

internal val LocalDateTime.dayOfWeek
    get() = date.dayOfWeek

private fun LocalDateTime.adjustTime(
    hours: Int = 0,
    minutes: Int = 0,
    millis: Int = 0,
): LocalDateTime {
    var totalMinutes = hour * 60L + minute + hours * 60L + minutes + millis / 60_000L
    var dayOffset = 0L

    while (totalMinutes >= 1_440) {
        totalMinutes -= 1_440
        dayOffset += 1
    }
    while (totalMinutes < 0) {
        totalMinutes += 1_440
        dayOffset -= 1
    }

    val newDate = if (dayOffset == 0L) {
        date
    } else {
        date.plus(dayOffset.toInt(), DateTimeUnit.DAY)
    }

    val newHour = (totalMinutes / 60).toInt()
    val newMinute = (totalMinutes % 60).toInt()
    return dateTimeAt(newDate, newHour, newMinute, second, nanosecond)
}
