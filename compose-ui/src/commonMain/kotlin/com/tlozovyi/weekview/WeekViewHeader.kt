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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import kotlinx.datetime.number

@Composable
internal fun WeekViewHeader(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    today: kotlinx.datetime.LocalDate,
    dateFormatter: DateFormatter,
    horizontalTranslationPx: Float,
    textMeasurer: TextMeasurer,
    allDayEventChips: List<EventChip>,
    allDayChipsByDate: Map<kotlinx.datetime.LocalDate, List<EventChip>>,
    allDayTextMeasurer: TextMeasurer,
    allDayExpandProgress: Float,
    useExpandedAllDayLayout: Boolean,
    allDayChipBoundsLayout: WeekViewLayout,
    showAllDayToggle: Boolean,
    onAllDayToggle: () -> Unit,
    onAllDayEventClick: ((WeekViewEvent) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val paddingHorizontalPx = with(density) { style.headerPaddingDp.toPx() }
    val measuredLabels = remember(
        layout.renderDates,
        layout.dayWidthPx,
        dateFormatter,
        style.headerTextColor,
        style.todayHeaderTextColor,
        style.weekendHeaderTextColor,
        style.headerTextSizeSp,
        style.fontFamily,
        style.headerFontWeight,
        textMeasurer,
        today,
    ) {
        layout.renderDates.map { date ->
            val columnWidth = layout.dayWidthPx - paddingHorizontalPx * 2f
            val label = dateFormatter(
                date.year,
                date.month.number,
                date.dayOfMonth,
                dayOfWeekIndex(date),
            )
            val labelColor = style.headerDateColor(date, today)
            val dateLabelTextStyle = style.headerDateTextStyle(labelColor)
            if (columnWidth <= 0f) {
                null
            } else {
                textMeasurer.measure(
                    text = label,
                    style = dateLabelTextStyle,
                    maxLines = 2,
                    constraints = Constraints(
                        maxWidth = columnWidth.toInt(),
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { layout.headerHeightPx.toDp() }),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(style.headerBackgroundColor),
        ) {
        Box(
            modifier = Modifier
                .width(style.timeColumnWidthDp)
                .fillMaxHeight()
                .background(style.timeColumnBackgroundColor)
                .weekViewAllDayToggleClick(
                    enabled = showAllDayToggle,
                    toggleAreaTopPx = layout.dateLabelHeightPx,
                    onToggle = onAllDayToggle,
                ),
        ) {
            if (showAllDayToggle) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawAllDayToggleArrow(
                        layout = layout,
                        style = style,
                        allDayExpandProgress = allDayExpandProgress,
                    )
                }
            }
            if (style.showWeekNumber && style.numberOfVisibleDays > 1) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { layout.dateLabelHeightPx.toDp() }),
                ) {
                    drawWeekNumberBadge(
                        style = style,
                        layout = layout,
                        firstVisibleDate = layout.renderDates.first(),
                        textMeasurer = textMeasurer,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds(),
        ) {
            Canvas(
                modifier = Modifier
                    .width(with(density) { layout.contentGridWidthPx.toDp() })
                    .height(with(density) { layout.headerHeightPx.toDp() })
                    .weekViewAllDayEventClick(
                        enabled = onAllDayEventClick != null,
                        eventChips = allDayEventChips,
                        horizontalTranslationPx = horizontalTranslationPx,
                        onEventClick = onAllDayEventClick ?: {},
                    ),
            ) {
                translate(left = horizontalTranslationPx) {
                    drawWeekViewAllDayEventChips(
                        style = style,
                        density = density,
                        textMeasurer = allDayTextMeasurer,
                        boundsLayout = allDayChipBoundsLayout,
                        headerLayout = layout,
                        renderDates = layout.renderDates,
                        allDayChipsByDate = allDayChipsByDate,
                        allDayExpandProgress = allDayExpandProgress,
                        useExpandedAllDayLayout = useExpandedAllDayLayout,
                    )

                    measuredLabels.forEachIndexed { index, textLayoutResult ->
                        if (textLayoutResult == null) {
                            return@forEachIndexed
                        }

                        val columnLeft = layout.dayStartX(index)
                        val columnRight = columnLeft + layout.dayWidthPx
                        val textX = headerLabelCenteredScreenX(
                            screenLeft = columnLeft,
                            screenRight = columnRight,
                            textWidth = textLayoutResult.size.width.toFloat(),
                            paddingHorizontalPx = paddingHorizontalPx,
                        )
                        val textY = (layout.dateLabelHeightPx - textLayoutResult.size.height) / 2f

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
        if (style.showHeaderBottomLine || style.showHeaderBottomShadow) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawHeaderBottomDecorations(style = style)
            }
        }
    }
}
