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
 * Non-interactive time range drawn behind timed events on the day grid.
 *
 * Taps and long-presses on blocked areas fall through to [WeekView]'s empty-slot callbacks,
 * matching the View library's `WeekViewEntity.BlockedTime` behavior.
 *
 * @property id Stable identifier used for layout and chip identity.
 * @property startTime Inclusive start date and time.
 * @property endTime End date and time.
 * @property title Optional label drawn on the blocked range.
 * @property subtitle Optional secondary label.
 * @property style Optional per-range colors and shape; unset fields fall back to [WeekViewStyle].
 */
@PublicApi
data class WeekViewBlockedTime(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val title: String? = null,
    val subtitle: String? = null,
    val style: WeekViewEventStyle? = null,
)
