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

import kotlinx.datetime.LocalDateTime

internal fun ResolvedWeekViewEntity.split(config: WeekViewLayoutConfig): List<ResolvedWeekViewEntity> {
    if (startTime >= endTime) {
        return emptyList()
    }

    val entities = if (isMultiDay) {
        splitByDates(minHour = config.minHour, maxHour = config.maxHour)
    } else {
        listOf(limitTo(minHour = config.minHour, maxHour = config.maxHour))
    }

    return entities.filter { it.startTime < it.endTime }
}

private fun ResolvedWeekViewEntity.splitByDates(
    minHour: Int,
    maxHour: Int,
): List<ResolvedWeekViewEntity> {
    val firstEvent = createCopy(
        startTime = startTime.limitToMinHour(minHour),
        endTime = startTime.atEndOfDay.limitToMaxHour(maxHour),
    )

    val results = mutableListOf<ResolvedWeekViewEntity>()
    results += firstEvent

    val daysInBetween = endTime.toEpochDays() - startTime.toEpochDays() - 1

    if (daysInBetween > 0) {
        var currentDate = startTime.atStartOfDay.plusDays(1)
        while (currentDate.toEpochDays() < endTime.toEpochDays()) {
            val intermediateStart = currentDate.withTimeAtStartOfPeriod(minHour)
            val intermediateEnd = currentDate.withTimeAtEndOfPeriod(maxHour)
            results += createCopy(startTime = intermediateStart, endTime = intermediateEnd)
            currentDate = currentDate.plusDays(1)
        }
    }

    val lastEvent = createCopy(
        startTime = endTime.atStartOfDay.limitToMinHour(minHour),
        endTime = endTime.limitToMaxHour(maxHour),
    )
    results += lastEvent

    return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
}

private fun ResolvedWeekViewEntity.limitTo(minHour: Int, maxHour: Int) = createCopy(
    startTime = startTime.limitToMinHour(minHour),
    endTime = endTime.limitToMaxHour(maxHour),
)

private fun LocalDateTime.limitToMinHour(minHour: Int): LocalDateTime {
    return if (hour < minHour) {
        withTimeAtStartOfPeriod(hour = minHour)
    } else {
        this
    }
}

private fun LocalDateTime.limitToMaxHour(maxHour: Int): LocalDateTime {
    return if (hour >= maxHour) {
        withTimeAtEndOfPeriod(hour = maxHour)
    } else {
        this
    }
}
