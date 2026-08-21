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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sqrt

internal fun DrawScope.drawFillPattern(
    pattern: ResolvedWeekViewEntity.FillPattern,
    bounds: Rect,
) {
    when (pattern) {
        is ResolvedWeekViewEntity.FillPattern.Lined -> drawDiagonalLines(
            bounds = bounds,
            spacing = pattern.spacingPx.toFloat(),
            isLtr = layoutDirection == LayoutDirection.Ltr,
            startToEnd = pattern.startToEnd,
            color = pattern.colorArgb.toComposeColor(),
            strokeWidth = pattern.strokeWidthPx.toFloat(),
        )
        is ResolvedWeekViewEntity.FillPattern.Dotted -> drawDots(
            bounds = bounds,
            spacing = pattern.spacingPx.toFloat(),
            color = pattern.colorArgb.toComposeColor(),
            strokeWidth = pattern.strokeWidthPx.toFloat(),
        )
    }
}

private data class DiagonalLine(
    val start: Offset,
    val end: Offset,
)

private fun DrawScope.drawDiagonalLines(
    bounds: Rect,
    spacing: Float,
    isLtr: Boolean,
    startToEnd: Boolean,
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
) {
    if (spacing <= 0f) {
        return
    }

    val mustStartLeft = (isLtr && startToEnd) || (!isLtr && !startToEnd)
    val lines = mutableListOf<DiagonalLine>()
    var startX = if (mustStartLeft) bounds.left else bounds.right

    if (mustStartLeft) {
        while (startX <= bounds.right) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = true,
            )
            startX += spacing
        }
    } else {
        while (startX >= bounds.left) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = false,
            )
            startX -= spacing
        }
    }

    var endX = if (mustStartLeft) bounds.right else bounds.left
    startX = if (mustStartLeft) bounds.left else bounds.right

    if (mustStartLeft) {
        while (endX >= bounds.left) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = true,
            )
            startX -= spacing
            endX -= spacing
        }
    } else {
        while (endX <= bounds.right) {
            lines += calculateDiagonalLine(
                startX = startX,
                startY = bounds.top,
                stopY = bounds.bottom,
                drawLeftToRight = false,
            )
            startX += spacing
            endX += spacing
        }
    }

    for (line in lines) {
        drawLine(
            color = color,
            start = line.start,
            end = line.end,
            strokeWidth = strokeWidth,
        )
    }
}

private fun calculateDiagonalLine(
    startX: Float,
    startY: Float,
    stopY: Float,
    drawLeftToRight: Boolean,
): DiagonalLine {
    val height = stopY - startY
    val width = height / sqrt(2f)
    val stopX = if (drawLeftToRight) startX + width else startX - width
    return DiagonalLine(
        start = Offset(startX, startY),
        end = Offset(stopX, stopY),
    )
}

private fun DrawScope.drawDots(
    bounds: Rect,
    spacing: Float,
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
) {
    val paddedDot = strokeWidth + spacing
    if (paddedDot <= 0f) {
        return
    }

    val horizontalDots = (bounds.width / paddedDot).toInt()
    val verticalDots = (bounds.height / paddedDot).toInt()
    if (horizontalDots <= 0 || verticalDots <= 0) {
        return
    }

    val dotsWidth = horizontalDots * paddedDot
    val dotsHeight = verticalDots * paddedDot
    val horizontalPadding = bounds.width - dotsWidth
    val verticalPadding = bounds.height - dotsHeight
    val left = bounds.left + horizontalPadding / 2f
    val top = bounds.top + verticalPadding / 2f
    val radius = strokeWidth / 2f

    for (horizontalDot in 0 until horizontalDots) {
        for (verticalDot in 0 until verticalDots) {
            val leftBound = left + horizontalDot * paddedDot
            val topBound = top + verticalDot * paddedDot
            val x = leftBound + paddedDot / 2f
            val y = topBound + paddedDot / 2f
            drawCircle(color = color, radius = radius, center = Offset(x, y))
        }
    }
}
