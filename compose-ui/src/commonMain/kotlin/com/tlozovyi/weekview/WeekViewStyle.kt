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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual configuration for [WeekView].
 */
@PublicApi
data class WeekViewStyle(
    val numberOfVisibleDays: Int = 3,
    val minHour: Int = 0,
    val maxHour: Int = 24,
    val hourHeightDp: Dp = 50.dp,
    val headerHeightDp: Dp = 40.dp,
    val timeColumnWidthDp: Dp = 56.dp,
    val timeColumnPaddingDp: Dp = 8.dp,
    val backgroundColor: Color = Color.White,
    val headerBackgroundColor: Color = Color(0xFFF5F5F5),
    val headerTextColor: Color = Color.Black,
    val timeColumnBackgroundColor: Color = Color(0xFFF5F5F5),
    val timeColumnTextColor: Color = Color(0xFF757575),
    val pastDayBackgroundColor: Color = Color(0xFFF7F7F7),
    val futureDayBackgroundColor: Color = Color.White,
    val todayPastBackgroundColor: Color = Color(0xFFF0F0F0),
    val todayFutureBackgroundColor: Color = Color(0xFFFAFAFA),
    val hourSeparatorColor: Color = Color(0xFFE0E0E0),
    val daySeparatorColor: Color = Color(0xFFE0E0E0),
    val timeColumnSeparatorColor: Color = Color(0xFFE0E0E0),
    val nowLineColor: Color = Color(0xFF4285F4),
    val showHourSeparators: Boolean = true,
    val showDaySeparators: Boolean = true,
    val showTimeColumnSeparator: Boolean = true,
    val showNowLine: Boolean = true,
    val showNowLineDot: Boolean = true,
    val hourSeparatorHeightDp: Dp = 1.dp,
    val daySeparatorWidthDp: Dp = 1.dp,
    val nowLineWidthDp: Dp = 2.dp,
    val nowDotRadiusDp: Dp = 5.dp,
) {
    init {
        require(numberOfVisibleDays >= 1) { "numberOfVisibleDays must be at least 1" }
        require(minHour in 0..23) { "minHour must be between 0 and 23" }
        require(maxHour in 1..24) { "maxHour must be between 1 and 24" }
        require(minHour < maxHour) { "minHour must be less than maxHour" }
    }

    internal val hours: IntRange
        get() = minHour until maxHour

    internal val hoursCount: Int
        get() = maxHour - minHour

    @PublicApi
    companion object {
        val Default = WeekViewStyle()
    }
}
