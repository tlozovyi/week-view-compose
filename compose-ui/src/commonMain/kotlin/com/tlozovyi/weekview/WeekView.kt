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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Compose Multiplatform week calendar view.
 *
 * Renders the date header, time column, day grid, current-time indicator, and event chips.
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
    val textMeasurer = rememberTextMeasurer()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val verticalScrollState = rememberScrollState()
    val layoutEngine = remember { WeekViewLayoutEngine() }

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

        val layoutConfig = remember(style) {
            WeekViewLayoutConfig.of(minHour = style.minHour, maxHour = style.maxHour)
        }

        val eventChips = remember(events, layout, style, layoutConfig, density) {
            val resolvedEvents = events.toResolvedEntities(style, density, layout.visibleDates)
            layoutEngine.createEventChips(resolvedEvents, layoutConfig)
                .filter { !it.event.isAllDay }
        }

        val chipsByDate = remember(eventChips) {
            eventChips.groupBy { it.startTime.date }
        }

        LaunchedEffect(layout, style, today) {
            if (!style.scrollToCurrentTimeOnLaunch || !layout.visibleDates.contains(today)) {
                return@LaunchedEffect
            }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            if (now.hour < style.minHour || now.hour >= style.maxHour) {
                return@LaunchedEffect
            }

            val y = layout.hourY(now.hour, style.minHour) + (now.minute / 60f) * layout.hourHeightPx
            val viewportHeight = (layout.viewportHeightPx - layout.headerHeightPx).coerceAtLeast(0f)
            val target = (y - viewportHeight / 2f).coerceAtLeast(0f).toInt()
            verticalScrollState.scrollTo(target)
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

                        layout.visibleDates.forEachIndexed { dateIndex, date ->
                            val chipsForDate = chipsByDate[date].orEmpty()
                            chipsForDate.calculateBoundsForDate(
                                dateIndex = dateIndex,
                                layout = gridLayout,
                                style = style,
                                density = density,
                            )
                            drawWeekViewEventChips(
                                eventChips = chipsForDate,
                                layout = gridLayout,
                                style = style,
                                density = density,
                                textMeasurer = textMeasurer,
                            )
                        }

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
}
