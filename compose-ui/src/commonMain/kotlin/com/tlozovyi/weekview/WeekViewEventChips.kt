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

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt

internal fun DrawScope.drawWeekViewEventChips(
    eventChips: List<EventChip>,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    density: Density,
    textMeasurer: TextMeasurer,
    draggingEventId: Long? = null,
    ghostChip: EventChip? = null,
) {
    val paddingHorizontalPx = style.eventPaddingHorizontalDp.toPx(density)
    val paddingVerticalPx = style.eventPaddingVerticalDp.toPx(density)
    val defaultCornerRadiusPx = style.eventCornerRadiusDp.toPx()
    val textStyle = style.eventTextStyle(style.defaultEventTextColor)

    for (eventChip in eventChips) {
        if (eventChip.isHidden || eventChip.bounds.isEmpty()) {
            continue
        }
        if (eventChip.eventId == draggingEventId) {
            continue
        }
        if (eventChip.event !is ResolvedWeekViewEntity.BlockedTime) {
            continue
        }

        drawEventChip(
            eventChip = eventChip,
            style = style,
            density = density,
            textMeasurer = textMeasurer,
            paddingHorizontalPx = paddingHorizontalPx,
            paddingVerticalPx = paddingVerticalPx,
            defaultCornerRadiusPx = defaultCornerRadiusPx,
            textStyle = textStyle,
        )
    }

    for (eventChip in eventChips) {
        if (eventChip.isHidden || eventChip.bounds.isEmpty()) {
            continue
        }
        if (eventChip.eventId == draggingEventId) {
            continue
        }
        if (eventChip.event !is ResolvedWeekViewEntity.Event<*>) {
            continue
        }

        drawEventChip(
            eventChip = eventChip,
            style = style,
            density = density,
            textMeasurer = textMeasurer,
            paddingHorizontalPx = paddingHorizontalPx,
            paddingVerticalPx = paddingVerticalPx,
            defaultCornerRadiusPx = defaultCornerRadiusPx,
            textStyle = textStyle,
        )
    }

    if (ghostChip != null && !ghostChip.bounds.isEmpty()) {
        drawEventChip(
            eventChip = ghostChip,
            style = style,
            density = density,
            textMeasurer = textMeasurer,
            paddingHorizontalPx = paddingHorizontalPx,
            paddingVerticalPx = paddingVerticalPx,
            defaultCornerRadiusPx = defaultCornerRadiusPx,
            textStyle = textStyle,
            isDragging = true,
        )
    }
}

private fun DrawScope.drawEventChip(
    eventChip: EventChip,
    style: WeekViewStyle,
    density: Density,
    textMeasurer: TextMeasurer,
    paddingHorizontalPx: Float,
    paddingVerticalPx: Float,
    defaultCornerRadiusPx: Float,
    textStyle: TextStyle,
    isDragging: Boolean = false,
) {
        val entity = eventChip.event
        val bounds = eventChip.bounds
        val rect = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
        val isBlockedTime = entity is ResolvedWeekViewEntity.BlockedTime
        val cornerRadiusPx = (entity.style.cornerRadius?.toFloat() ?: defaultCornerRadiusPx)
        val backgroundColor = when {
            isDragging -> Color(0xFF757575)
            isBlockedTime -> entity.style.backgroundColor?.toComposeColor()
                ?: style.defaultBlockedTimeBackgroundColor
            else -> entity.style.backgroundColor?.toComposeColor()
                ?: style.defaultEventBackgroundColor
        }.eventChipDrawColor(isDragging)

        drawEventChipBackground(
            bounds = rect,
            cornerRadiusPx = cornerRadiusPx,
            backgroundColor = backgroundColor,
            pattern = entity.style.pattern,
            eventChip = eventChip,
            entity = entity,
        )

        val borderWidth = entity.style.borderWidth?.toFloat()
        if (borderWidth != null && borderWidth > 0f) {
            val borderColor = entity.style.borderColor?.toComposeColor()
                ?: style.defaultEventBorderColor
                ?: backgroundColor
            drawEventChipBorder(
                bounds = rect,
                cornerRadiusPx = cornerRadiusPx,
                borderWidth = borderWidth,
                borderColor = borderColor,
                eventChip = eventChip,
                entity = entity,
            )
        }

        val availableWidth = bounds.width().roundToInt() - paddingHorizontalPx.roundToInt()
        val availableHeight = bounds.height().roundToInt() - (paddingVerticalPx * 2).roundToInt()
        if (availableWidth <= 0 || availableHeight <= 0) {
            return
        }

        val chipTextStyle = when {
            isBlockedTime -> entity.style.textColor?.toComposeColor()?.let { style.eventTextStyle(it) }
                ?: style.eventTextStyle(style.defaultBlockedTimeTextColor)
            else -> entity.style.textColor?.toComposeColor()?.let { style.eventTextStyle(it) }
                ?: textStyle
        }

        if (entity.title.isEmpty()) {
            return
        }

        val textLayoutResult = fitTimedEventChipText(
            textMeasurer = textMeasurer,
            entity = entity,
            baseStyle = chipTextStyle,
            maxWidth = availableWidth,
            maxHeight = availableHeight,
            adaptiveEventTextSize = style.adaptiveEventTextSize,
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = bounds.left + paddingHorizontalPx,
                y = bounds.top + paddingVerticalPx,
            ),
        )
}

private fun ChipBounds.isEmpty(): Boolean {
    return left == 0f && top == 0f && right == 0f && bottom == 0f
}
