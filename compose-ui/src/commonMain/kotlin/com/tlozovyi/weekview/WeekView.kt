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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Compose Multiplatform week calendar view.
 *
 * Renders the date header, time column, day grid, and current-time indicator.
 * Event chips are accepted but not drawn until a later release.
 */
@PublicApi
@Composable
fun WeekView(
    events: List<WeekViewEvent>,
    modifier: Modifier = Modifier,
    style: WeekViewStyle = WeekViewStyle.Default,
    firstVisibleDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    dateFormatter: DateFormatter = { year, month, day, dayOfWeek ->
        defaultDateFormatter(year, month, day, dayOfWeek, style.numberOfVisibleDays)
    },
    timeFormatter: TimeFormatter = ::defaultTimeFormatter,
) {
    val density = LocalDensity.current
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val verticalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor),
    ) {
        val layout = remember(maxWidth, maxHeight, style, firstVisibleDate, density) {
            calculateWeekViewLayout(
                viewportWidthPx = with(density) { maxWidth.toPx() },
                viewportHeightPx = with(density) { maxHeight.toPx() },
                style = style,
                firstVisibleDate = firstVisibleDate,
                density = density,
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            WeekViewHeader(
                layout = layout,
                style = style,
                dateFormatter = dateFormatter,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(verticalScrollState),
                ) {
                    WeekViewTimeColumn(
                        style = style,
                        timeFormatter = timeFormatter,
                    )

                    Canvas(
                        modifier = Modifier
                            .width(with(density) { layout.gridWidthPx.toDp() })
                            .height(with(density) { layout.gridHeightPx.toDp() }),
                    ) {
                        val gridLayout = layout.copy(
                            gridWidthPx = size.width,
                            dayWidthPx = size.width / style.numberOfVisibleDays,
                        )
                        drawWeekViewGrid(
                            layout = gridLayout,
                            style = style,
                            today = today,
                        )

                        if (style.showTimeColumnSeparator) {
                            drawLine(
                                color = style.timeColumnSeparatorColor,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                                strokeWidth = style.daySeparatorWidthDp.toPx(),
                            )
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    val pendingEventRendering = events
}
