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
 * User-supplied event displayed by [WeekView].
 *
 * @property id Stable identifier used for hit-testing, drag-and-drop, and chip layout.
 * @property title Primary label drawn on the event chip.
 * @property startTime Inclusive start date and time.
 * @property endTime Exclusive end date and time for timed events; for all-day events, typically
 *   midnight on the day after the last visible day.
 * @property subtitle Optional secondary line shown below the title on timed chips (or inline on all-day chips).
 * @property isAllDay When `true`, the event is drawn in the header all-day row instead of the grid.
 * @property style Optional per-event colors and shape; unset fields fall back to [WeekViewStyle].
 */
@PublicApi
data class WeekViewEvent(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val subtitle: String? = null,
    val isAllDay: Boolean = false,
    val style: WeekViewEventStyle? = null,
)
