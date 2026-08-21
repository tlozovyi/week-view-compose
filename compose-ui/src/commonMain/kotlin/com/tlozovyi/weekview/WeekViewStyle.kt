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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DayOfWeek

/**
 * Visual and behavioral configuration for [WeekView].
 *
 * Pass an instance to [WeekView] or use [Default] for library defaults. Most color and dimension
 * properties have sensible defaults and only need overriding for branding.
 */
@PublicApi
data class WeekViewStyle(
    /** Number of day columns visible at once (minimum 1). */
    val numberOfVisibleDays: Int = 3,
    /** First hour row drawn in the grid (0–23). */
    val minHour: Int = 0,
    /** Exclusive end hour of the grid (1–24). */
    val maxHour: Int = 24,
    /** Default height of one hour row before pinch-to-zoom. */
    val hourHeightDp: Dp = 50.dp,
    /** Minimum hour row height when pinching out. */
    val minHourHeightDp: Dp = 20.dp,
    /** Maximum hour row height when pinching in. */
    val maxHourHeightDp: Dp = 400.dp,
    /** Enables two-finger pinch-to-zoom on the day grid. */
    val pinchToZoomEnabled: Boolean = true,
    /** Height of the date label row at the top of the header. */
    val headerHeightDp: Dp = 40.dp,
    /** Padding above and below all-day chips inside the header. */
    val headerPaddingDp: Dp = 4.dp,
    /** Text size of all-day event titles. */
    val allDayEventTextSizeSp: TextUnit = 12.sp,
    /** Vertical padding inside each all-day chip (combined with text size sets chip height). */
    val allDayEventPaddingVerticalDp: Dp = 5.dp,
    /** Stacks overlapping all-day events in separate rows when `true`; otherwise overlaps horizontally. */
    val arrangeAllDayEventsVertically: Boolean = true,
    /** Width of the hour-label column on the left. */
    val timeColumnWidthDp: Dp = 56.dp,
    /** Horizontal padding inside the time column labels. */
    val timeColumnPaddingDp: Dp = 8.dp,
    /** Background behind the entire view. */
    val backgroundColor: Color = Color.White,
    /** Background of the date header row. */
    val headerBackgroundColor: Color = Color(0xFFF5F5F5),
    /** Color of date labels in the header. */
    val headerTextColor: Color = Color.Black,
    /** Background of the time column. */
    val timeColumnBackgroundColor: Color = Color(0xFFF5F5F5),
    /** Color of hour labels in the time column. */
    val timeColumnTextColor: Color = Color(0xFF757575),
    /** Background for day columns before the current time (non-today). */
    val pastDayBackgroundColor: Color = Color(0xFFF7F7F7),
    /** Background for day columns after the current time (non-today). */
    val futureDayBackgroundColor: Color = Color.White,
    /** Background for today's column before the current time. */
    val todayPastBackgroundColor: Color = Color(0xFFF0F0F0),
    /** Background for today's column after the current time. */
    val todayFutureBackgroundColor: Color = Color(0xFFFAFAFA),
    /** Color of horizontal lines separating hour rows. */
    val hourSeparatorColor: Color = Color(0xFFE0E0E0),
    /** Color of vertical lines separating day columns. */
    val daySeparatorColor: Color = Color(0xFFE0E0E0),
    /** Color of the separator between the time column and the grid. */
    val timeColumnSeparatorColor: Color = Color(0xFFE0E0E0),
    /** Color of the current-time horizontal line. */
    val nowLineColor: Color = Color(0xFF4285F4),
    /** Draws horizontal lines at each hour boundary. */
    val showHourSeparators: Boolean = true,
    /** Draws vertical lines between day columns. */
    val showDaySeparators: Boolean = true,
    /** Draws a vertical line between the time column and the grid. */
    val showTimeColumnSeparator: Boolean = true,
    /** Draws the current-time indicator line across the grid. */
    val showNowLine: Boolean = true,
    /** Draws a dot at the left end of the current-time line. */
    val showNowLineDot: Boolean = true,
    /** Stroke height of hour separator lines. */
    val hourSeparatorHeightDp: Dp = 1.dp,
    /** Stroke width of day separator lines. */
    val daySeparatorWidthDp: Dp = 1.dp,
    /** Stroke width of the current-time line. */
    val nowLineWidthDp: Dp = 2.dp,
    /** Radius of the current-time dot. */
    val nowDotRadiusDp: Dp = 5.dp,
    /** Default fill color for timed event chips. */
    val defaultEventBackgroundColor: Color = Color(0xFF9FC6E7),
    /** Default fill color for blocked time ranges on the day grid. */
    val defaultBlockedTimeBackgroundColor: Color = Color(0x1A757575),
    /** Default text color for blocked time range labels. */
    val defaultBlockedTimeTextColor: Color = Color(0xFF757575),
    /** Default border color for blocked time ranges; `null` means no border. */
    val defaultBlockedTimeBorderColor: Color? = null,
    /** Default text color for timed event chips. */
    val defaultEventTextColor: Color = Color.White,
    /** Default border color for timed event chips; `null` means no border. */
    val defaultEventBorderColor: Color? = null,
    /** Corner radius of timed event chips. */
    val eventCornerRadiusDp: Dp = 4.dp,
    /** Horizontal padding inside timed event chips. */
    val eventPaddingHorizontalDp: Dp = 4.dp,
    /** Vertical padding inside timed event chips. */
    val eventPaddingVerticalDp: Dp = 2.dp,
    /** Text size of timed event titles. */
    val eventTextSizeSp: TextUnit = 12.sp,
    /** Shrinks timed and all-day chip text until it fits the chip bounds (View library: `adaptiveEventTextSize`). */
    val adaptiveEventTextSize: Boolean = false,
    /** Gap between horizontally overlapping event chips in the same row. */
    val overlappingEventGapDp: Dp = 1.dp,
    /** Vertical margin between stacked event chips. */
    val eventMarginVerticalDp: Dp = 2.dp,
    /** Horizontal gap between day columns; event chips use day width minus this value. */
    val columnGapDp: Dp = 1.dp,
    /** Extra horizontal inset for single-day mode event chips. */
    val singleDayHorizontalPaddingDp: Dp = 0.dp,
    /** Scrolls the grid to center the current time once when the view is first shown. */
    val scrollToCurrentTimeOnLaunch: Boolean = true,
    /** Allows continuous horizontal scrolling when [WeekView] receives [onFirstVisibleDateChange]. */
    val horizontalScrollingEnabled: Boolean = true,
    /** Animates to a snap target when the user lifts their finger after horizontal scrolling. */
    val horizontalScrollSnapEnabled: Boolean = true,
    /** First day of the week used when [numberOfVisibleDays] >= 7 for week alignment and page snap. */
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    /** Allows long-press drag-and-drop when [WeekView] receives [onEventDrop]. */
    val dragAndDropEnabled: Boolean = true,
) {
    init {
        require(numberOfVisibleDays >= 1) { "numberOfVisibleDays must be at least 1" }
        require(minHour in 0..23) { "minHour must be between 0 and 23" }
        require(maxHour in 1..24) { "maxHour must be between 1 and 24" }
        require(minHour < maxHour) { "minHour must be less than maxHour" }
        require(minHourHeightDp.value >= 0f) { "minHourHeightDp must be non-negative" }
        require(maxHourHeightDp >= minHourHeightDp) { "maxHourHeightDp must be >= minHourHeightDp" }
    }

    internal val hours: IntRange
        get() = minHour until maxHour

    internal val hoursCount: Int
        get() = maxHour - minHour

    /** Default style used when none is supplied to [WeekView]. */
    @PublicApi
    companion object {
        val Default = WeekViewStyle()
    }
}
