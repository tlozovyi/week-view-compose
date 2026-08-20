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
import kotlin.math.roundToInt

internal sealed class ResolvedWeekViewEntity {

    internal abstract val id: Long
    internal abstract val title: String
    internal abstract val subtitle: String?
    internal abstract val startTime: LocalDateTime
    internal abstract val endTime: LocalDateTime
    internal abstract val isAllDay: Boolean
    internal abstract val style: Style

    internal val period: Period by lazy {
        Period.fromDate(startTime)
    }

    data class Event<T>(
        override val id: Long,
        override val title: String,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val subtitle: String?,
        override val isAllDay: Boolean,
        override val style: Style,
        val data: T?,
    ) : ResolvedWeekViewEntity()

    data class BlockedTime(
        override val id: Long,
        override val title: String,
        override val subtitle: String?,
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override val style: Style,
    ) : ResolvedWeekViewEntity() {
        override val isAllDay: Boolean = false
    }

    data class Style(
        val textColor: Long? = null,
        val backgroundColor: Long? = null,
        val borderColor: Long? = null,
        val borderWidth: Int? = null,
        val cornerRadius: Int? = null,
    )

    internal val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal val durationInMinutes: Int
        get() = startTime minutesUntil endTime

    internal val isMultiDay: Boolean
        get() = startTime.isSameDate(endTime).not()

    internal fun collidesWith(other: ResolvedWeekViewEntity): Boolean {
        if (isAllDay != other.isAllDay) {
            return false
        }

        if (startTime.isEqual(other.startTime) && endTime.isEqual(other.endTime)) {
            return true
        }

        val thisEnd = if (endTime.isEqual(other.startTime)) {
            endTime.minusMillis(1)
        } else {
            endTime
        }

        val otherEnd = if (other.startTime.isEqual(this.endTime)) {
            other.endTime.minusMillis(1)
        } else {
            other.endTime
        }

        if (thisEnd != endTime || otherEnd != other.endTime) {
            return false
        }

        return !startTime.isAfter(otherEnd) && !endTime.isBefore(other.startTime)
    }

    internal fun createCopy(
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
    ): ResolvedWeekViewEntity = when (this) {
        is Event<*> -> copy(startTime = startTime, endTime = endTime)
        is BlockedTime -> copy(startTime = startTime, endTime = endTime)
    }
}
