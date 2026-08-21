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

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Minimum font size when [WeekViewStyle.adaptiveEventTextSize] shrinks chip labels. */
internal val MinAdaptiveEventTextSizeSp = 6.sp

/**
 * Builds chip label text matching the View library's [TextFitter] conventions.
 *
 * Timed events use a newline between title and subtitle; all-day events use a space separator.
 */
internal fun combineEventChipText(
    title: String,
    subtitle: String?,
    isAllDay: Boolean,
): String {
    val trimmedSubtitle = subtitle?.takeIf { it.isNotBlank() } ?: return title
    return if (isAllDay) {
        "$title $trimmedSubtitle"
    } else {
        "$title\n$trimmedSubtitle"
    }
}

internal fun eventChipText(
    entity: ResolvedWeekViewEntity,
    includeSubtitle: Boolean = true,
): String {
    val subtitle = if (includeSubtitle) entity.subtitle else null
    return combineEventChipText(
        title = entity.title,
        subtitle = subtitle,
        isAllDay = entity.isAllDay,
    )
}

/**
 * Fits timed-event chip text into [maxWidth] × [maxHeight], trimming lines then optionally shrinking
 * font size (View library `TextFitter.fitSingleEvent`).
 */
internal fun fitTimedEventChipText(
    textMeasurer: TextMeasurer,
    entity: ResolvedWeekViewEntity,
    baseStyle: TextStyle,
    maxWidth: Int,
    maxHeight: Int,
    adaptiveEventTextSize: Boolean,
): TextLayoutResult {
    return fitEventChipText(
        textMeasurer = textMeasurer,
        text = eventChipText(entity, includeSubtitle = true),
        baseStyle = baseStyle,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        adaptiveEventTextSize = adaptiveEventTextSize,
        maxLineCount = 2,
    )
}

/** Fits a single-line all-day chip label (optionally shrinking to fit). */
internal fun fitAllDayEventChipText(
    textMeasurer: TextMeasurer,
    entity: ResolvedWeekViewEntity,
    baseStyle: TextStyle,
    maxWidth: Int,
    maxHeight: Int,
    adaptiveEventTextSize: Boolean,
): TextLayoutResult {
    return fitEventChipText(
        textMeasurer = textMeasurer,
        text = eventChipText(entity, includeSubtitle = true),
        baseStyle = baseStyle,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        adaptiveEventTextSize = adaptiveEventTextSize,
        maxLineCount = 1,
    )
}

internal fun fitEventChipText(
    textMeasurer: TextMeasurer,
    text: String,
    baseStyle: TextStyle,
    maxWidth: Int,
    maxHeight: Int,
    adaptiveEventTextSize: Boolean,
    maxLineCount: Int,
): TextLayoutResult {
    if (text.isEmpty() || maxWidth <= 0 || maxHeight <= 0) {
        return measureChipText(
            textMeasurer = textMeasurer,
            text = text,
            style = baseStyle,
            maxWidth = maxWidth.coerceAtLeast(0),
            maxLines = 1,
        )
    }

    var textToFit = text
    var style = baseStyle
    var layout = measureChipText(
        textMeasurer = textMeasurer,
        text = textToFit,
        style = style,
        maxWidth = maxWidth,
        maxLines = maxLineCount,
    )

    if (layout.size.height <= maxHeight) {
        return layout
    }

    while (layout.size.height > maxHeight && layout.lineCount > 1) {
        val startOfLastLine = layout.getLineStart(layout.lineCount - 1)
        if (startOfLastLine <= 0) {
            break
        }
        textToFit = textToFit.substring(0, startOfLastLine).trimEnd()
        if (textToFit.isEmpty()) {
            break
        }
        layout = measureChipText(
            textMeasurer = textMeasurer,
            text = textToFit,
            style = style,
            maxWidth = maxWidth,
            maxLines = maxLineCount,
        )
    }

    while (layout.size.height > maxHeight && adaptiveEventTextSize) {
        val nextFontSize = shrinkFontSize(style.fontSize) ?: break
        style = style.copy(fontSize = nextFontSize)
        layout = measureChipText(
            textMeasurer = textMeasurer,
            text = textToFit,
            style = style,
            maxWidth = maxWidth,
            maxLines = 1,
        )
    }

    return layout
}

/** Measures chip text without a height cap so [TextLayoutResult.size.height] reflects true line height. */
private fun measureChipText(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidth: Int,
    maxLines: Int,
): TextLayoutResult {
    return textMeasurer.measure(
        text = text,
        style = style,
        maxLines = maxLines,
        constraints = Constraints(
            maxWidth = maxWidth.coerceAtLeast(0),
            maxHeight = Constraints.Infinity,
        ),
    )
}

private fun shrinkFontSize(current: TextUnit): TextUnit? {
    if (current == TextUnit.Unspecified) {
        return null
    }
    val nextValue = current.value - 1f
    return if (nextValue >= MinAdaptiveEventTextSizeSp.value) {
        nextValue.sp
    } else {
        null
    }
}
