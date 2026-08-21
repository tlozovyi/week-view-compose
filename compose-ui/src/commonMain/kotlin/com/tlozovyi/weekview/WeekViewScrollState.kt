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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Programmatic scroll controller for [WeekView].
 *
 * Create with [rememberWeekViewScrollState] and pass to [WeekView]. Scroll commands suspend until
 * the animation completes (or the target is applied instantly when [animated] is `false`).
 */
@Stable
@PublicApi
class WeekViewScrollState internal constructor() {
    internal var connection: WeekViewScrollConnection? = null

    private var _firstVisibleDate by mutableStateOf<LocalDate?>(null)
    private var _gridScrollOffsetPx by mutableStateOf(0f)

    /** Leftmost visible day column, updated while [WeekView] is composed. */
    val firstVisibleDate: LocalDate
        get() = _firstVisibleDate ?: LocalDate(1970, 1, 1)

    /** Current vertical grid scroll offset in pixels. */
    val gridScrollOffsetPx: Float
        get() = _gridScrollOffsetPx

    internal fun updateObservedState(firstVisibleDate: LocalDate, gridScrollOffsetPx: Float) {
        _firstVisibleDate = firstVisibleDate
        _gridScrollOffsetPx = gridScrollOffsetPx
    }

    /**
     * Scrolls horizontally so [date] is visible, aligning to the page origin (week-aligned when
     * [WeekViewStyle.numberOfVisibleDays] >= 7).
     */
    suspend fun scrollToDate(date: LocalDate, animated: Boolean = true) {
        execute(WeekViewScrollCommand.Date(date, animated))
    }

    /**
     * Scrolls vertically so [hour]:[minute] is visible on the current day columns.
     *
     * Matches View library padding: one hour above the target when [hour] > [WeekViewStyle.minHour].
     */
    suspend fun scrollToTime(hour: Int, minute: Int, animated: Boolean = true) {
        execute(WeekViewScrollCommand.Time(hour, minute, animated))
    }

    /** Scrolls horizontally to [dateTime.date], then vertically to [dateTime] time. */
    suspend fun scrollToDateTime(dateTime: LocalDateTime, animated: Boolean = true) {
        execute(WeekViewScrollCommand.DateTime(dateTime, animated))
    }

    private suspend fun execute(command: WeekViewScrollCommand) {
        val activeConnection = connection
            ?: throw IllegalStateException("WeekViewScrollState is not attached to a WeekView")
        activeConnection.execute(command)
    }
}

/** Creates and remembers a [WeekViewScrollState] for [WeekView]. */
@PublicApi
@Composable
fun rememberWeekViewScrollState(): WeekViewScrollState {
    return remember { WeekViewScrollState() }
}

internal sealed class WeekViewScrollCommand {
    abstract val animated: Boolean

    data class Date(val date: LocalDate, override val animated: Boolean) : WeekViewScrollCommand()
    data class Time(val hour: Int, val minute: Int, override val animated: Boolean) : WeekViewScrollCommand()
    data class DateTime(val dateTime: LocalDateTime, override val animated: Boolean) : WeekViewScrollCommand()
}

internal class WeekViewScrollConnection(
    val execute: suspend (WeekViewScrollCommand) -> Unit,
)
