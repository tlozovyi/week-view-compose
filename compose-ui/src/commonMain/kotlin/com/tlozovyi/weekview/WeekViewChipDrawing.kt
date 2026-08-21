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
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun DrawScope.drawEventChipBackground(
    bounds: Rect,
    cornerRadiusPx: Float,
    backgroundColor: Color,
    pattern: ResolvedWeekViewEntity.FillPattern?,
    eventChip: EventChip,
    entity: ResolvedWeekViewEntity,
) {
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(bounds.left, bounds.top),
        size = Size(bounds.width, bounds.height),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
    )

    if (pattern != null) {
        drawFillPattern(pattern = pattern, bounds = bounds)
    }

    if (entity.shouldFlattenMultiDayCorners()) {
        flattenMultiDayCorners(
            eventChip = eventChip,
            bounds = bounds,
            cornerRadiusPx = cornerRadiusPx,
            backgroundColor = backgroundColor,
        )
    }
}

internal fun DrawScope.drawEventChipBorder(
    bounds: Rect,
    cornerRadiusPx: Float,
    borderWidth: Float,
    borderColor: Color,
    eventChip: EventChip,
    entity: ResolvedWeekViewEntity,
) {
    val inset = borderWidth / 2f
    drawRoundRect(
        color = borderColor,
        topLeft = Offset(bounds.left + inset, bounds.top + inset),
        size = Size(bounds.width - borderWidth, bounds.height - borderWidth),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        style = Stroke(width = borderWidth),
    )

    if (entity.shouldFlattenMultiDayCorners()) {
        redrawMultiDayBorderSegments(
            eventChip = eventChip,
            bounds = bounds,
            cornerRadiusPx = cornerRadiusPx,
            borderWidth = borderWidth,
            borderColor = borderColor,
        )
    }
}

private fun DrawScope.flattenMultiDayCorners(
    eventChip: EventChip,
    bounds: Rect,
    cornerRadiusPx: Float,
    backgroundColor: Color,
) {
    if (eventChip.startsOnEarlierDay) {
        drawRect(
            color = backgroundColor,
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(bounds.width, cornerRadiusPx),
        )
    }
    if (eventChip.endsOnLaterDay) {
        drawRect(
            color = backgroundColor,
            topLeft = Offset(bounds.left, bounds.bottom - cornerRadiusPx),
            size = Size(bounds.width, cornerRadiusPx),
        )
    }
}

private fun DrawScope.redrawMultiDayBorderSegments(
    eventChip: EventChip,
    bounds: Rect,
    cornerRadiusPx: Float,
    borderWidth: Float,
    borderColor: Color,
) {
    val borderStart = bounds.left + borderWidth / 2f
    val borderEnd = bounds.right - borderWidth / 2f

    if (eventChip.startsOnEarlierDay) {
        drawLine(
            color = borderColor,
            start = Offset(borderStart, bounds.top),
            end = Offset(borderStart, bounds.top + cornerRadiusPx),
            strokeWidth = borderWidth,
        )
        drawLine(
            color = borderColor,
            start = Offset(borderEnd, bounds.top),
            end = Offset(borderEnd, bounds.top + cornerRadiusPx),
            strokeWidth = borderWidth,
        )
    }

    if (eventChip.endsOnLaterDay) {
        drawLine(
            color = borderColor,
            start = Offset(borderStart, bounds.bottom - cornerRadiusPx),
            end = Offset(borderStart, bounds.bottom),
            strokeWidth = borderWidth,
        )
        drawLine(
            color = borderColor,
            start = Offset(borderEnd, bounds.bottom - cornerRadiusPx),
            end = Offset(borderEnd, bounds.bottom),
            strokeWidth = borderWidth,
        )
    }
}
