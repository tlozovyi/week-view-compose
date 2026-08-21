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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/** Diagonal line direction for [WeekViewFillPattern.Lined]. */
@PublicApi
enum class WeekViewLinedPatternDirection {
    StartToEnd,
    EndToStart,
}

/**
 * Optional overlay pattern drawn on top of an event or blocked-time chip background.
 *
 * Ported from View `WeekViewEntity.Style.Pattern`.
 */
@PublicApi
sealed class WeekViewFillPattern {
    abstract val color: Color
    abstract val strokeWidthDp: Dp

    /** Diagonal hatch lines. */
    @PublicApi
    data class Lined(
        override val color: Color,
        override val strokeWidthDp: Dp = 1.dp,
        val spacingDp: Dp = 8.dp,
        val direction: WeekViewLinedPatternDirection = WeekViewLinedPatternDirection.StartToEnd,
    ) : WeekViewFillPattern()

    /** Grid of dots. */
    @PublicApi
    data class Dotted(
        override val color: Color,
        override val strokeWidthDp: Dp = 1.dp,
        val spacingDp: Dp = 8.dp,
    ) : WeekViewFillPattern()
}

internal fun WeekViewFillPattern.toEntityPattern(density: Density): ResolvedWeekViewEntity.FillPattern {
    return when (this) {
        is WeekViewFillPattern.Lined -> ResolvedWeekViewEntity.FillPattern.Lined(
            colorArgb = color.toArgbLong(),
            strokeWidthPx = with(density) { strokeWidthDp.roundToPx() },
            spacingPx = with(density) { spacingDp.roundToPx() },
            startToEnd = direction == WeekViewLinedPatternDirection.StartToEnd,
        )
        is WeekViewFillPattern.Dotted -> ResolvedWeekViewEntity.FillPattern.Dotted(
            colorArgb = color.toArgbLong(),
            strokeWidthPx = with(density) { strokeWidthDp.roundToPx() },
            spacingPx = with(density) { spacingDp.roundToPx() },
        )
    }
}
