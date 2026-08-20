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

/**
 * Computes overlapping [EventChip] layout slots for timed events.
 *
 * Ported from the original Android Week View layout engine. [WeekView] uses an internal instance;
 * expose this type when building custom views that reuse the same overlap algorithm.
 */
@PublicApi
class WeekViewLayoutEngine {

    private val eventChipsFactory = EventChipsFactory()

    /**
     * Splits [events] into per-day [EventChip] rows with relative horizontal positions for overlaps.
     *
     * @param events Resolved entities covering the visible (or render-buffer) date range.
     * @param config Hour range and all-day flags from [WeekViewLayoutConfig.of].
     * @return Chips ready for bounds calculation and drawing; all-day entities are included and
     *   filtered by the caller.
     */
    @PublicApi
    fun createEventChips(
        events: List<ResolvedWeekViewEntity>,
        config: WeekViewLayoutConfig,
    ): List<EventChip> {
        return eventChipsFactory.create(events, config)
    }
}
