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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.number

@Composable
internal fun WeekViewHeader(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    dateFormatter: DateFormatter,
    horizontalTranslationPx: Float,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val paddingHorizontalPx = with(density) { 4.dp.toPx() }
    val textStyle = TextStyle(
        color = style.headerTextColor,
        fontSize = 12.sp,
    )
    val measuredLabels = remember(
        layout.renderDates,
        layout.dayWidthPx,
        dateFormatter,
        style.headerTextColor,
        textMeasurer,
    ) {
        layout.renderDates.map { date ->
            val columnWidth = layout.dayWidthPx - paddingHorizontalPx * 2f
            val label = dateFormatter(
                date.year,
                date.month.number,
                date.dayOfMonth,
                dayOfWeekIndex(date),
            )
            if (columnWidth <= 0f) {
                null
            } else {
                textMeasurer.measure(
                    text = label,
                    style = textStyle,
                    maxLines = 2,
                    constraints = Constraints(
                        maxWidth = columnWidth.toInt(),
                    ),
                )
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(style.headerHeightDp)
            .background(style.headerBackgroundColor),
    ) {
        Box(
            modifier = Modifier
                .width(style.timeColumnWidthDp)
                .fillMaxHeight()
                .background(style.timeColumnBackgroundColor),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds(),
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val viewportWidthPx = size.width
                measuredLabels.forEachIndexed { index, textLayoutResult ->
                    if (textLayoutResult == null) {
                        return@forEachIndexed
                    }

                    val (screenLeft, screenRight) = dayColumnScreenBounds(
                        columnIndex = index,
                        dayWidthPx = layout.dayWidthPx,
                        horizontalTranslationPx = horizontalTranslationPx,
                    )
                    if (!isDayColumnVisibleOnScreen(
                            screenLeft = screenLeft,
                            screenRight = screenRight,
                            viewportWidthPx = viewportWidthPx,
                        )
                    ) {
                        return@forEachIndexed
                    }

                    val textX = headerLabelCenteredScreenX(
                        screenLeft = screenLeft,
                        screenRight = screenRight,
                        textWidth = textLayoutResult.size.width.toFloat(),
                        paddingHorizontalPx = paddingHorizontalPx,
                    )
                    val textY = (size.height - textLayoutResult.size.height) / 2f
                    val clipLeft = maxOf(screenLeft, 0f)
                    val clipRight = minOf(screenRight, viewportWidthPx)

                    clipRect(
                        left = clipLeft,
                        top = 0f,
                        right = clipRight,
                        bottom = size.height,
                    ) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = textX,
                                y = textY,
                            ),
                        )
                    }
                }
            }
        }
    }
}
