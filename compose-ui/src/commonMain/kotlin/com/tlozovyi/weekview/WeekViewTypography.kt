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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

internal fun WeekViewStyle.eventTextStyle(textColor: Color): TextStyle {
    return TextStyle(
        color = textColor,
        fontSize = eventTextSizeSp,
        fontFamily = fontFamily,
    )
}

internal fun WeekViewStyle.allDayEventTextStyle(textColor: Color): TextStyle {
    return TextStyle(
        color = textColor,
        fontSize = allDayEventTextSizeSp,
        fontFamily = fontFamily,
    )
}

internal fun WeekViewStyle.headerDateTextStyle(textColor: Color): TextStyle {
    return TextStyle(
        color = textColor,
        fontSize = headerTextSizeSp,
        fontFamily = fontFamily,
        fontWeight = headerFontWeight,
    )
}

internal fun WeekViewStyle.timeColumnTextStyle(): TextStyle {
    return TextStyle(
        color = timeColumnTextColor,
        fontSize = timeColumnTextSizeSp,
        fontFamily = fontFamily,
    )
}

internal fun WeekViewStyle.weekNumberTextStyle(): TextStyle {
    return TextStyle(
        color = weekNumberTextColor,
        fontSize = weekNumberTextSizeSp,
        fontFamily = fontFamily,
    )
}

internal fun WeekViewStyle.headerDateColor(date: kotlinx.datetime.LocalDate): Color {
    return if (date.isWeekend()) {
        weekendHeaderTextColor ?: headerTextColor
    } else {
        headerTextColor
    }
}

internal fun WeekViewStyle.pastDayBackground(date: kotlinx.datetime.LocalDate): Color {
    return if (date.isWeekend()) {
        pastWeekendBackgroundColor ?: pastDayBackgroundColor
    } else {
        pastDayBackgroundColor
    }
}

internal fun WeekViewStyle.futureDayBackground(date: kotlinx.datetime.LocalDate): Color {
    return if (date.isWeekend()) {
        futureWeekendBackgroundColor ?: futureDayBackgroundColor
    } else {
        futureDayBackgroundColor
    }
}
