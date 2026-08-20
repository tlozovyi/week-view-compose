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
 * A laid-out time slot for one [ResolvedWeekViewEntity] within a day column.
 *
 * Produced by [WeekViewLayoutEngine.createEventChips] and updated with pixel [bounds] before
 * rendering. Library consumers typically receive [WeekViewEvent] via callbacks rather than
 * manipulating [EventChip] directly.
 *
 * @property event Resolved entity including styling and optional payload.
 * @property index Layout index when an entity spans or repeats across days.
 * @property startTime Start of this chip's visible slice (may differ from [event.startTime] on
 *   multi-day events).
 * @property endTime End of this chip's visible slice.
 */
@PublicApi
data class EventChip(
    val event: ResolvedWeekViewEntity,
    val index: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {

    /** Unique key combining [event] id and layout [index]. */
    val id: String = "${event.id}-$index"

    /** Original entity id from the source [WeekViewEvent]. */
    val eventId: Long = event.id

    /** Pixel bounds relative to the day grid canvas origin. Updated internally before draw. */
    @PublicApi
    var bounds: ChipBounds = ChipBounds()

    /** Duration of this chip slice in minutes. */
    val durationInMinutes: Int by lazy {
        startTime minutesUntil endTime
    }

    /** Horizontal start fraction within the day column (0–1) for overlapping layout. */
    var relativeStart: Float = 0f

    /** Horizontal width fraction within the day column (0–1) for overlapping layout. */
    var relativeWidth: Float = 0f

    /** When `true`, the chip is skipped during rendering (e.g. collapsed all-day overflow). */
    var isHidden: Boolean = false

    /** Minutes from [WeekViewLayoutConfig.minHour] to [startTime] on this slice. */
    var minutesFromStartHour: Int = 0

    /** `true` when the full event started before this chip's [startTime]. */
    val startsOnEarlierDay: Boolean
        get() = event.startTime < startTime

    /** `true` when the full event ends after this chip's [endTime]. */
    val endsOnLaterDay: Boolean
        get() = event.endTime > endTime
}

/**
 * Axis-aligned rectangle for an [EventChip] in grid coordinates.
 *
 * @property left Left edge in pixels.
 * @property top Top edge in pixels.
 * @property right Right edge in pixels.
 * @property bottom Bottom edge in pixels.
 */
@PublicApi
data class ChipBounds(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f,
) {
    /** Resets all edges to zero (empty bounds). */
    fun setEmpty() {
        left = 0f
        top = 0f
        right = 0f
        bottom = 0f
    }

    /**
     * Returns whether the point `(x, y)` lies strictly inside the rectangle.
     *
     * Used for tap and long-press hit-testing on event chips.
     */
    fun isHit(x: Float, y: Float): Boolean {
        return x > left && x < right && y > top && y < bottom
    }
}
