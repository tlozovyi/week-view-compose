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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

internal data class FetchRange(
    val previous: Period,
    val current: Period,
    val next: Period,
) {

    val periods: List<Period> = listOf(previous, current, next)

    internal companion object {
        fun create(firstVisibleDay: LocalDateTime): FetchRange {
            val current = Period.fromDate(firstVisibleDay)
            return FetchRange(current.previous, current, current.next)
        }
    }
}

internal data class Period(
    val month: Int,
    val year: Int,
) : Comparable<Period> {

    val previous: Period
        get() {
            val previousYear = if (month == 1) year - 1 else year
            val previousMonth = if (month == 1) 12 else month - 1
            return Period(previousMonth, previousYear)
        }

    val next: Period
        get() {
            val nextYear = if (month == 12) year + 1 else year
            val nextMonth = if (month == 12) 1 else month + 1
            return Period(nextMonth, nextYear)
        }

    val startDate: LocalDateTime = newDate(year, month, dayOfMonth = 1)
    val endDate: LocalDateTime = startDate
        .withDayOfMonth(startDate.date.lengthOfMonth)
        .atEndOfDay

    override fun compareTo(other: Period): Int {
        return when {
            year < other.year -> -1
            year > other.year -> 1
            else -> month.compareTo(other.month)
        }
    }

    internal companion object {
        fun fromDate(date: LocalDateTime): Period {
            return Period(month = date.date.month.number, year = date.year)
        }
    }
}

private fun LocalDateTime.withDayOfMonth(day: Int): LocalDateTime {
    return dateTimeAt(LocalDate(date.year, date.month.number, day), hour, minute, second, nanosecond)
}
