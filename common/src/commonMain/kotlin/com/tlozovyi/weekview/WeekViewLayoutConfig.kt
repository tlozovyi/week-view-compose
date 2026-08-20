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

import kotlinx.datetime.LocalDateTime

/**
 * Layout configuration used by calendar algorithms.
 */
@PublicApi
data class WeekViewLayoutConfig(
    val minHour: Int = 0,
    val maxHour: Int = 24,
    val arrangeAllDayEventsVertically: Boolean = false,
) {
    fun minutesFromStart(eventStartTime: LocalDateTime): Int {
        val hoursFromStart = eventStartTime.hour - minHour
        return hoursFromStart * 60 + eventStartTime.minute
    }

    @PublicApi
    companion object {
        fun of(
            minHour: Int,
            maxHour: Int,
            arrangeAllDayEventsVertically: Boolean = false,
        ): WeekViewLayoutConfig {
            return WeekViewLayoutConfig(
                minHour = minHour,
                maxHour = maxHour,
                arrangeAllDayEventsVertically = arrangeAllDayEventsVertically,
            )
        }
    }
}
