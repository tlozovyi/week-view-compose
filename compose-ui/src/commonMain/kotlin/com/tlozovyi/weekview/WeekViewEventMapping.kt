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

internal fun List<WeekViewEvent>.toResolvedEntities(
    style: WeekViewStyle,
    density: Density,
    visibleDates: List<LocalDate>,
): List<ResolvedWeekViewEntity> {
    if (isEmpty() || visibleDates.isEmpty()) {
        return emptyList()
    }

    val firstVisibleDate = visibleDates.first()
    val lastVisibleDate = visibleDates.last()

    return mapNotNull { event ->
        if (event.endTime.date < firstVisibleDate || event.startTime.date > lastVisibleDate) {
            null
        } else {
            event.toResolvedEntity(style, density)
        }
    }
}

internal fun WeekViewEvent.toResolvedEntity(
    style: WeekViewStyle,
    density: Density,
): ResolvedWeekViewEntity.Event<WeekViewEvent> {
    return ResolvedWeekViewEntity.Event(
        id = id,
        title = title,
        startTime = startTime,
        endTime = endTime,
        subtitle = subtitle,
        isAllDay = isAllDay,
        style = this.style.toEntityStyle(style, density),
        data = this,
    )
}
