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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import kotlinx.datetime.LocalDate

internal fun DrawScope.drawWeekNumberBadge(
    style: WeekViewStyle,
    layout: WeekViewLayout,
    firstVisibleDate: LocalDate,
    textMeasurer: TextMeasurer,
) {
    if (!style.showWeekNumber || style.numberOfVisibleDays <= 1) {
        return
    }

    val weekNumber = firstVisibleDate.weekOfYear().toString()
    val textLayout = textMeasurer.measure(
        text = weekNumber,
        style = style.weekNumberTextStyle(),
    )
    val textHeight = textLayout.size.height.toFloat()
    val textWidth = textLayout.size.width.toFloat()
    val badgeWidth = textWidth * 2.5f
    val badgeHeight = textHeight * 1.5f
    val centerX = size.width / 2f
    val centerY = layout.dateLabelHeightPx / 2f
    val badgeLeft = centerX - badgeWidth / 2f
    val badgeTop = centerY - badgeHeight / 2f

    drawRect(
        color = style.headerBackgroundColor,
        topLeft = Offset(0f, 0f),
        size = Size(size.width, layout.dateLabelHeightPx),
    )

    drawRoundRect(
        color = style.weekNumberBackgroundColor,
        topLeft = Offset(badgeLeft, badgeTop),
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = CornerRadius(
            style.weekNumberBackgroundCornerRadiusDp.toPx(),
            style.weekNumberBackgroundCornerRadiusDp.toPx(),
        ),
    )

    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(
            x = centerX - textWidth / 2f,
            y = centerY - textHeight / 2f,
        ),
    )
}

internal fun DrawScope.drawHeaderBottomDecorations(
    style: WeekViewStyle,
) {
    if (style.showHeaderBottomShadow) {
        val radius = style.headerBottomShadowRadiusDp.toPx()
        val shadowHeight = radius * 2f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    style.headerBottomShadowColor,
                    style.headerBottomShadowColor.copy(alpha = 0f),
                ),
                startY = size.height - shadowHeight,
                endY = size.height,
            ),
            topLeft = Offset(0f, size.height - shadowHeight),
            size = Size(size.width, shadowHeight),
        )
    }

    if (style.showHeaderBottomLine) {
        val stroke = style.headerBottomLineWidthDp.toPx()
        val y = size.height - stroke
        drawLine(
            color = style.headerBottomLineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke,
        )
    }
}
