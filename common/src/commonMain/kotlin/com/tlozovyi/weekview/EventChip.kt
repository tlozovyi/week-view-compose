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

/**
 * Encapsulates a [ResolvedWeekViewEntity] and its layout slot within a day column.
 */
@PublicApi
data class EventChip(
    val event: ResolvedWeekViewEntity,
    val index: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {

    val id: String = "${event.id}-$index"
    val eventId: Long = event.id

    @PublicApi
    var bounds: ChipBounds = ChipBounds()

    val durationInMinutes: Int by lazy {
        startTime minutesUntil endTime
    }

    var relativeStart: Float = 0f
    var relativeWidth: Float = 0f
    var isHidden: Boolean = false
    var minutesFromStartHour: Int = 0

    val startsOnEarlierDay: Boolean
        get() = event.startTime < startTime

    val endsOnLaterDay: Boolean
        get() = event.endTime > endTime
}

@PublicApi
data class ChipBounds(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f,
) {
    fun setEmpty() {
        left = 0f
        top = 0f
        right = 0f
        bottom = 0f
    }

    fun isHit(x: Float, y: Float): Boolean {
        return x > left && x < right && y > top && y < bottom
    }
}
