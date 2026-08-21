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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * Optional visual overrides for a single [WeekViewEvent].
 *
 * Any property left `null` inherits the matching default from [WeekViewStyle].
 *
 * @property backgroundColor Fill color of the chip background.
 * @property textColor Color of the event title text.
 * @property borderColor Stroke color when [borderWidthDp] is set.
 * @property borderWidthDp Stroke width; no border when `null` or zero.
 * @property cornerRadiusDp Corner radius of the rounded rectangle.
 */
@PublicApi
data class WeekViewEventStyle(
    val backgroundColor: Color? = null,
    val textColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidthDp: Dp? = null,
    val cornerRadiusDp: Dp? = null,
)

internal fun WeekViewEventStyle?.toEntityStyle(
    style: WeekViewStyle,
    density: Density,
): ResolvedWeekViewEntity.Style {
    val eventStyle = this
    return ResolvedWeekViewEntity.Style(
        textColor = (eventStyle?.textColor ?: style.defaultEventTextColor).toArgbLong(),
        backgroundColor = (eventStyle?.backgroundColor ?: style.defaultEventBackgroundColor).toArgbLong(),
        borderColor = eventStyle?.borderColor?.toArgbLong() ?: style.defaultEventBorderColor?.toArgbLong(),
        borderWidth = eventStyle?.borderWidthDp?.let { with(density) { it.roundToPx() } },
        cornerRadius = eventStyle?.cornerRadiusDp?.let { with(density) { it.roundToPx() } },
    )
}

internal fun WeekViewEventStyle?.toBlockedTimeEntityStyle(
    style: WeekViewStyle,
    density: Density,
): ResolvedWeekViewEntity.Style {
    val blockedStyle = this
    return ResolvedWeekViewEntity.Style(
        textColor = (blockedStyle?.textColor ?: style.defaultBlockedTimeTextColor).toArgbLong(),
        backgroundColor = (blockedStyle?.backgroundColor ?: style.defaultBlockedTimeBackgroundColor).toArgbLong(),
        borderColor = blockedStyle?.borderColor?.toArgbLong() ?: style.defaultBlockedTimeBorderColor?.toArgbLong(),
        borderWidth = blockedStyle?.borderWidthDp?.let { with(density) { it.roundToPx() } },
        cornerRadius = blockedStyle?.cornerRadiusDp?.let { with(density) { it.roundToPx() } },
    )
}

internal fun WeekViewStyle.defaultEntityStyle(density: Density): ResolvedWeekViewEntity.Style {
    return WeekViewEventStyle().toEntityStyle(this, density)
}

internal fun Color.toArgbLong(): Long {
    val alpha = (this.alpha * 255f).toInt() and 0xFF
    val red = (this.red * 255f).toInt() and 0xFF
    val green = (this.green * 255f).toInt() and 0xFF
    val blue = (this.blue * 255f).toInt() and 0xFF
    return ((alpha shl 24) or (red shl 16) or (green shl 8) or blue).toLong() and 0xFFFFFFFFL
}

internal fun Long.toComposeColor(): Color {
    val argb = toInt()
    return Color(
        alpha = ((argb ushr 24) and 0xFF) / 255f,
        red = ((argb ushr 16) and 0xFF) / 255f,
        green = ((argb ushr 8) and 0xFF) / 255f,
        blue = (argb and 0xFF) / 255f,
    )
}

/** Applies drag dimming without replacing a transparent chip's own alpha. */
internal fun Color.eventChipDrawColor(isDragging: Boolean): Color {
    val dragAlphaFactor = if (isDragging) 0.85f else 1f
    return copy(alpha = alpha * dragAlphaFactor)
}
