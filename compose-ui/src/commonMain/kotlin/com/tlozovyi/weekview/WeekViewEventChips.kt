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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

internal fun DrawScope.drawWeekViewEventChips(
    eventChips: List<EventChip>,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    density: Density,
    textMeasurer: TextMeasurer,
) {
    val paddingHorizontalPx = style.eventPaddingHorizontalDp.toPx()
    val paddingVerticalPx = style.eventPaddingVerticalDp.toPx()
    val defaultCornerRadiusPx = style.eventCornerRadiusDp.toPx()
    val textStyle = TextStyle(
        color = style.defaultEventTextColor,
        fontSize = style.eventTextSizeSp,
    )

    for (eventChip in eventChips) {
        if (eventChip.isHidden || eventChip.bounds.isEmpty()) {
            continue
        }

        val entity = eventChip.event
        val bounds = eventChip.bounds
        val rect = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
        val cornerRadiusPx = (entity.style.cornerRadius?.toFloat() ?: defaultCornerRadiusPx)
        val backgroundColor = entity.style.backgroundColor?.toComposeColor()
            ?: style.defaultEventBackgroundColor

        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        )

        val borderWidth = entity.style.borderWidth?.toFloat()
        if (borderWidth != null && borderWidth > 0f) {
            val borderColor = entity.style.borderColor?.toComposeColor()
                ?: style.defaultEventBorderColor
                ?: backgroundColor
            val inset = borderWidth / 2f
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(rect.left + inset, rect.top + inset),
                size = Size(rect.width - borderWidth, rect.height - borderWidth),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = borderWidth),
            )
        }

        val availableWidth = bounds.width().roundToInt() - paddingHorizontalPx.roundToInt()
        val availableHeight = bounds.height().roundToInt() - (paddingVerticalPx * 2).roundToInt()
        if (availableWidth <= 0 || availableHeight <= 0) {
            continue
        }

        val chipTextStyle = entity.style.textColor?.toComposeColor()?.let { textStyle.copy(color = it) }
            ?: textStyle

        val textLayoutResult = textMeasurer.measure(
            text = entity.title,
            style = chipTextStyle,
            maxLines = 2,
            constraints = androidx.compose.ui.unit.Constraints(
                maxWidth = availableWidth.coerceAtLeast(0),
                maxHeight = availableHeight.coerceAtLeast(0),
            ),
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = bounds.left + paddingHorizontalPx,
                y = bounds.top + paddingVerticalPx,
            ),
        )
    }
}

private fun ChipBounds.isEmpty(): Boolean {
    return left == 0f && top == 0f && right == 0f && bottom == 0f
}
