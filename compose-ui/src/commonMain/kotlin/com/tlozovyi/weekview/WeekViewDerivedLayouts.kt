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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density

/** Header and grid [WeekViewLayout] values derived from viewport size and all-day rows. */
internal data class WeekViewDerivedLayouts(
    val baseLayout: WeekViewLayout,
    val layout: WeekViewLayout,
    val gridLayout: WeekViewLayout,
    val allDayChipBoundsLayout: WeekViewLayout,
    val showAllDayToggle: Boolean,
)

@Composable
internal fun rememberWeekViewDerivedLayouts(
    baseLayout: WeekViewLayout,
    maxAllDayEventsPerDay: Int,
    expandProgress: Float,
    arrangeAllDayEventsVertically: Boolean,
    style: WeekViewStyle,
    density: Density,
): WeekViewDerivedLayouts {
    val showAllDayToggle = remember(maxAllDayEventsPerDay, arrangeAllDayEventsVertically) {
        showAllDayEventsToggleArrow(
            maxAllDayEventsPerDay = maxAllDayEventsPerDay,
            arrangeAllDayEventsVertically = arrangeAllDayEventsVertically,
        )
    }

    val allDayBoundsLayout = remember(baseLayout, maxAllDayEventsPerDay, style, density) {
        baseLayout.withAllDaySection(
            maxAllDayEventsPerDay = maxAllDayEventsPerDay,
            allDayEventsExpanded = true,
            style = style,
            density = density,
        ).copy(contentGridWidthPx = baseLayout.renderDates.size * baseLayout.dayWidthPx)
    }

    val layout = remember(
        baseLayout,
        maxAllDayEventsPerDay,
        expandProgress,
        showAllDayToggle,
        style,
        density,
    ) {
        val allDayLayout = if (showAllDayToggle) {
            baseLayout.withAnimatedAllDaySection(
                maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                expandProgress = expandProgress,
                style = style,
                density = density,
            )
        } else {
            baseLayout.withAllDaySection(
                maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                allDayEventsExpanded = false,
                style = style,
                density = density,
            )
        }
        allDayLayout.copy(contentGridWidthPx = baseLayout.renderDates.size * baseLayout.dayWidthPx)
    }

    val gridLayout = remember(layout) {
        layout.copy(contentGridWidthPx = layout.renderDates.size * layout.dayWidthPx)
    }

    val allDayChipBoundsLayout = if (showAllDayToggle) {
        allDayBoundsLayout
    } else {
        layout
    }

    return WeekViewDerivedLayouts(
        baseLayout = baseLayout,
        layout = layout,
        gridLayout = gridLayout,
        allDayChipBoundsLayout = allDayChipBoundsLayout,
        showAllDayToggle = showAllDayToggle,
    )
}
