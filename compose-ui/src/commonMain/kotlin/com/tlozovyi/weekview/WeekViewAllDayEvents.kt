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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

/**
 * Draws all-day chips in content coordinates inside a canvas as wide as
 * [WeekViewLayout.contentGridWidthPx], translated by [horizontalTranslationPx]
 * (same model as the timed-event grid).
 *
 * Bounds and visibility are computed at draw time so scrolling / expand state
 * never relies on stale [EventChip.bounds] or [EventChip.isHidden] mutations.
 */
internal fun DrawScope.drawWeekViewAllDayEventChips(
    style: WeekViewStyle,
    density: Density,
    textMeasurer: TextMeasurer,
    boundsLayout: WeekViewLayout,
    headerLayout: WeekViewLayout,
    renderDates: List<LocalDate>,
    allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    allDayExpandProgress: Float,
    useExpandedAllDayLayout: Boolean,
) {
    val paddingHorizontalPx = style.eventPaddingHorizontalDp.toPx(density)
    val paddingVerticalPx = style.allDayEventPaddingVerticalDp.toPx(density)
    val defaultCornerRadiusPx = style.eventCornerRadiusDp.toPx()
    val textStyle = style.allDayEventTextStyle(style.defaultEventTextColor)
    val calculator = EventChipBoundsCalculator(boundsLayout, style, density)

    renderDates.forEachIndexed { dateIndex, date ->
        val dayStartX = boundsLayout.dayStartX(dateIndex)
        val modifiedDayStartX = if (style.numberOfVisibleDays == 1) {
            dayStartX + style.singleDayHorizontalPaddingDp.toPx(density)
        } else {
            dayStartX
        }

        val visibleChips = visibleAllDayChipsForDate(
            chips = allDayChipsByDate[date].orEmpty(),
            useExpandedAllDayLayout = useExpandedAllDayLayout,
            arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
        )

        visibleChips.forEachIndexed { rowIndex, eventChip ->
            val bounds = calculator.calculateAllDayEvent(
                rowIndex = rowIndex,
                eventChip = eventChip,
                dayStartX = modifiedDayStartX,
            )
            drawAllDayEventChip(
                eventChip = eventChip,
                bounds = bounds,
                style = style,
                density = density,
                textMeasurer = textMeasurer,
                paddingHorizontalPx = paddingHorizontalPx,
                paddingVerticalPx = paddingVerticalPx,
                defaultCornerRadiusPx = defaultCornerRadiusPx,
                textStyle = textStyle,
                isLtr = boundsLayout.isLtr,
            )
        }
    }

    drawAllDayExpandInfo(
        boundsLayout = boundsLayout,
        headerLayout = headerLayout,
        style = style,
        density = density,
        renderDates = renderDates,
        allDayChipsByDate = allDayChipsByDate,
        allDayExpandProgress = allDayExpandProgress,
        useExpandedAllDayLayout = useExpandedAllDayLayout,
        textMeasurer = textMeasurer,
        calculator = calculator,
    )
}

private fun DrawScope.drawAllDayEventChip(
    eventChip: EventChip,
    bounds: ChipBounds,
    style: WeekViewStyle,
    density: Density,
    textMeasurer: TextMeasurer,
    paddingHorizontalPx: Float,
    paddingVerticalPx: Float,
    defaultCornerRadiusPx: Float,
    textStyle: TextStyle,
    isLtr: Boolean,
) {
    val entity = eventChip.event
    val width = bounds.width()
    val height = bounds.height()
    val rect = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
    val cornerRadiusPx = (entity.style.cornerRadius?.toFloat() ?: defaultCornerRadiusPx)
    val backgroundColor = entity.style.backgroundColor?.toComposeColor()
        ?: style.defaultEventBackgroundColor

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

    val availableWidth = width.roundToInt() - paddingHorizontalPx.roundToInt()
    val availableHeight = height.roundToInt() - (paddingVerticalPx * 2).roundToInt()
    if (availableWidth <= 0 || availableHeight <= 0) {
        return
    }

    val chipTextStyle = entity.style.textColor?.toComposeColor()?.let { style.allDayEventTextStyle(it) }
        ?: textStyle

    val textLayoutResult = fitAllDayEventChipText(
        textMeasurer = textMeasurer,
        entity = entity,
        baseStyle = chipTextStyle,
        maxWidth = availableWidth,
        maxHeight = availableHeight,
        adaptiveEventTextSize = style.adaptiveEventTextSize,
    )

    val textX = eventChipTextX(
        bounds = bounds,
        paddingHorizontalPx = paddingHorizontalPx,
        textWidth = textLayoutResult.size.width.toFloat(),
        isLtr = isLtr,
    )
    val textY = bounds.top + (height - textLayoutResult.size.height) / 2f

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(textX, textY),
    )
}

internal fun DrawScope.drawAllDayExpandInfo(
    boundsLayout: WeekViewLayout,
    headerLayout: WeekViewLayout,
    style: WeekViewStyle,
    density: Density,
    renderDates: List<LocalDate>,
    allDayChipsByDate: Map<LocalDate, List<EventChip>>,
    allDayExpandProgress: Float,
    useExpandedAllDayLayout: Boolean,
    textMeasurer: TextMeasurer,
    calculator: EventChipBoundsCalculator,
) {
    if (allDayExpandProgress != 0f || !style.arrangeAllDayEventsVertically || useExpandedAllDayLayout) {
        return
    }

    val expandTextStyle = TextStyle(
        color = style.headerTextColor,
        fontSize = style.allDayEventTextSizeSp,
    )
    val paddingHorizontalPx = style.eventPaddingHorizontalDp.toPx(density)
    val paddingVerticalPx = style.allDayEventPaddingVerticalDp.toPx(density)
    val eventMarginVerticalPx = style.eventMarginVerticalDp.toPx(density)

    renderDates.forEachIndexed { index, date ->
        val events = allDayChipsByDate[date].orEmpty()
        if (events.size <= 2) {
            return@forEachIndexed
        }

        val visibleChips = visibleAllDayChipsForDate(
            chips = events,
            useExpandedAllDayLayout = false,
            arrangeAllDayEventsVertically = true,
        )
        val firstChip = visibleChips.firstOrNull() ?: return@forEachIndexed

        val dayStartX = boundsLayout.dayStartX(index)
        val modifiedDayStartX = if (style.numberOfVisibleDays == 1) {
            dayStartX + style.singleDayHorizontalPaddingDp.toPx(density)
        } else {
            dayStartX
        }
        val firstBounds = calculator.calculateAllDayEvent(
            rowIndex = 0,
            eventChip = firstChip,
            dayStartX = modifiedDayStartX,
        )

        val columnLeft = dayStartX
        val columnRight = columnLeft + boundsLayout.dayWidthPx

        val hiddenCount = events.size - 1
        val textLayoutResult = textMeasurer.measure(
            text = "+$hiddenCount",
            style = expandTextStyle,
            maxLines = 1,
        )

        val textX = if (boundsLayout.isLtr) {
            firstBounds.left + paddingHorizontalPx
        } else {
            firstBounds.right - paddingHorizontalPx - textLayoutResult.size.width
        }
        val textY = firstBounds.bottom +
            eventMarginVerticalPx +
            paddingVerticalPx

        clipRect(
            left = columnLeft,
            top = textY,
            right = columnRight,
            bottom = headerLayout.headerHeightPx,
        ) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(textX, textY),
            )
        }
    }
}

internal fun DrawScope.drawAllDayToggleArrow(
    layout: WeekViewLayout,
    style: WeekViewStyle,
    allDayExpandProgress: Float,
) {
    val headerPaddingPx = style.headerPaddingDp.toPx()
    val arrowAreaHeight = layout.allDayChipHeightPx
    val bottom = layout.headerHeightPx - headerPaddingPx
    val top = bottom - arrowAreaHeight
    val centerX = size.width / 2f
    val centerY = (top + bottom) / 2f
    val halfWidth = arrowAreaHeight * 0.25f
    val halfHeight = arrowAreaHeight * 0.15f

    val path = Path()
    if (allDayExpandProgress > 0.5f) {
        path.moveTo(centerX - halfWidth, centerY + halfHeight)
        path.lineTo(centerX, centerY - halfHeight)
        path.lineTo(centerX + halfWidth, centerY + halfHeight)
    } else {
        path.moveTo(centerX - halfWidth, centerY - halfHeight)
        path.lineTo(centerX, centerY + halfHeight)
        path.lineTo(centerX + halfWidth, centerY - halfHeight)
    }

    drawPath(
        path = path,
        color = style.headerTextColor,
        style = Stroke(width = 2f),
    )
}
