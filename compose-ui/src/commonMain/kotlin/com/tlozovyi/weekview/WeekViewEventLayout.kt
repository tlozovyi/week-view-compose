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

import androidx.compose.ui.unit.Density
import kotlinx.datetime.LocalDate

internal fun prepareEventChipBounds(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    density: Density,
    chipsByDate: Map<LocalDate, List<EventChip>>,
) {
    layout.renderDates.forEachIndexed { dateIndex, date ->
        chipsByDate[date].orEmpty().calculateBoundsForDate(
            dateIndex = dateIndex,
            layout = layout,
            style = style,
            density = density,
        )
    }
}

internal fun EventChip.toWeekViewEvent(): WeekViewEvent? {
    val entity = event as? ResolvedWeekViewEntity.Event<*> ?: return null
    return entity.data as? WeekViewEvent
}

internal fun List<EventChip>.findEventAt(x: Float, y: Float): WeekViewEvent? {
    return asReversed().firstNotNullOfOrNull { chip ->
        if (chip.isHidden || chip.bounds.isEmpty()) {
            null
        } else if (chip.bounds.isHit(x, y)) {
            chip.toWeekViewEvent()
        } else {
            null
        }
    }
}

internal fun LocalDate.pageByDays(days: Int): LocalDate = plusDays(days)

internal fun LocalDate.plusDays(days: Int): LocalDate {
    return LocalDate.fromEpochDays(toEpochDays() + days)
}

private fun ChipBounds.isEmpty(): Boolean {
    return left == 0f && top == 0f && right == 0f && bottom == 0f
}
