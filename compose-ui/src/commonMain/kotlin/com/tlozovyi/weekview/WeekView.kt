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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
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
    onFirstVisibleDateChange: ((LocalDate) -> Unit)? = null,
    onEventClick: ((WeekViewEvent) -> Unit)? = null,
    dateFormatter: DateFormatter = { year, month, day, dayOfWeek ->
        defaultDateFormatter(year, month, day, dayOfWeek, style.numberOfVisibleDays)
    },
    timeFormatter: TimeFormatter = ::defaultTimeFormatter,
) {
    val density = LocalDensity.current
    val headerTextMeasurer = rememberTextMeasurer(cacheSize = 16)
    val eventTextMeasurer = rememberTextMeasurer(cacheSize = 64)
    val allDayTextMeasurer = rememberTextMeasurer(cacheSize = 32)
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var gridScrollOffsetPx by remember { mutableFloatStateOf(0f) }
    val layoutEngine = remember { WeekViewLayoutEngine() }
    val horizontalScrollingEnabled = style.horizontalScrollingEnabled && onFirstVisibleDateChange != null
    val eventClickEnabled = onEventClick != null
    var anchorDate by remember { mutableStateOf(firstVisibleDate) }
    var horizontalScrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var allDayEventsExpanded by remember { mutableStateOf(false) }
    var anchorGeneration by remember { mutableIntStateOf(0) }
    var hourHeightPx by remember(style.hourHeightDp, density) {
        mutableFloatStateOf(with(density) { style.hourHeightDp.toPx() })
    }
    val hourHeightDp = with(density) { hourHeightPx.toDp() }

    LaunchedEffect(style.hourHeightDp, density) {
        hourHeightPx = with(density) { style.hourHeightDp.toPx() }
    }

    LaunchedEffect(firstVisibleDate) {
        if (firstVisibleDate != anchorDate) {
            anchorDate = firstVisibleDate
            horizontalScrollOffsetPx = 0f
            anchorGeneration++
        }
    }

    LaunchedEffect(anchorDate) {
        allDayEventsExpanded = false
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor),
    ) {
        val configuredMinHourHeightPx = with(density) { style.minHourHeightDp.toPx() }
        val configuredMaxHourHeightPx = with(density) { style.maxHourHeightDp.toPx() }

        val baseLayout = remember(
            maxWidth,
            maxHeight,
            style,
            anchorDate,
            density,
            horizontalScrollingEnabled,
            hourHeightPx,
        ) {
            calculateWeekViewLayout(
                viewportWidthPx = with(density) { maxWidth.toPx() },
                viewportHeightPx = with(density) { maxHeight.toPx() },
                style = style,
                firstVisibleDate = anchorDate,
                density = density,
                horizontalScrollingEnabled = horizontalScrollingEnabled,
                hourHeightPxOverride = hourHeightPx,
            )
        }

        val horizontalTranslationPx = horizontalContentTranslationPx(
            scrollOffsetPx = horizontalScrollOffsetPx,
            dayWidthPx = baseLayout.dayWidthPx,
            scrollBufferDays = baseLayout.scrollBufferDays,
        )

        val eventDateRange = if (horizontalScrollingEnabled) baseLayout.renderDates else baseLayout.visibleDates

        val layoutConfig = remember(style) {
            WeekViewLayoutConfig.of(
                minHour = style.minHour,
                maxHour = style.maxHour,
                arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
            )
        }

        val allEventChips = remember(events, eventDateRange, anchorGeneration, style, layoutConfig, density) {
            val resolvedEvents = events.toResolvedEntities(style, density, eventDateRange)
            layoutEngine.createEventChips(resolvedEvents, layoutConfig)
        }

        val eventChips = remember(allEventChips) {
            allEventChips.filter { !it.event.isAllDay }
        }

        val allDayEventChips = remember(allEventChips) {
            allEventChips.filter { it.event.isAllDay }
        }

        val chipsByDate = remember(eventChips) {
            eventChips.groupBy { it.startTime.date }
        }

        val allDayChipsByDate = remember(allDayEventChips) {
            allDayEventChips.groupBy { it.startTime.date }
        }

        val maxAllDayEventsPerDay = remember(allDayChipsByDate, baseLayout.renderDates) {
            maxAllDayEventsPerDay(
                allDayChipsByDate = allDayChipsByDate,
                renderDates = baseLayout.renderDates,
            )
        }

        val showAllDayToggle = remember(maxAllDayEventsPerDay, style.arrangeAllDayEventsVertically) {
            showAllDayEventsToggleArrow(
                maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
            )
        }

        val expandProgress by animateFloatAsState(
            targetValue = if (allDayEventsExpanded) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "allDayExpandProgress",
        )

        val useExpandedAllDayLayout = shouldUseExpandedAllDayLayout(
            showAllDayToggle = showAllDayToggle,
            allDayEventsExpanded = allDayEventsExpanded,
            expandProgress = expandProgress,
        )

        val allDayBoundsLayout = remember(baseLayout, maxAllDayEventsPerDay, style, density) {
            baseLayout.withAllDaySection(
                maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                allDayEventsExpanded = true,
                style = style,
                density = density,
            ).copy(contentGridWidthPx = baseLayout.renderDates.size * baseLayout.dayWidthPx)
        }

        val layout = remember(
            baseLayout,
            maxAllDayEventsPerDay,
            expandProgress,
            showAllDayToggle,
            style,
            density,
        ) {
            val allDayLayout = if (showAllDayToggle) {
                baseLayout.withAnimatedAllDaySection(
                    maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                    expandProgress = expandProgress,
                    style = style,
                    density = density,
                )
            } else {
                baseLayout.withAllDaySection(
                    maxAllDayEventsPerDay = maxAllDayEventsPerDay,
                    allDayEventsExpanded = false,
                    style = style,
                    density = density,
                )
            }
            allDayLayout.copy(contentGridWidthPx = baseLayout.renderDates.size * baseLayout.dayWidthPx)
        }

        val gridLayout = remember(layout) {
            layout.copy(contentGridWidthPx = layout.renderDates.size * layout.dayWidthPx)
        }

        val allDayChipBoundsLayout = if (showAllDayToggle) {
            allDayBoundsLayout
        } else {
            layout
        }

        applyAllDayEventVisibility(
            allDayEventChips = allDayEventChips,
            allDayChipsByDate = allDayChipsByDate,
            renderDates = layout.renderDates,
            allDayEventsExpanded = useExpandedAllDayLayout,
            arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
        )
        prepareAllDayEventChipBounds(
            allDayEventChips = allDayEventChips,
            layout = allDayChipBoundsLayout,
            style = style,
            density = density,
            chipsByDate = allDayChipsByDate,
            useExpandedAllDayLayout = useExpandedAllDayLayout,
        )

        SideEffect {
            prepareEventChipBounds(
                layout = gridLayout,
                style = style,
                density = density,
                chipsByDate = chipsByDate,
            )
        }

        var isPinchZoomActive by remember { mutableStateOf(false) }
        var pinchBaselineScrollOffsetPx by remember { mutableFloatStateOf(0f) }
        var pinchBaselineLayoutGridHeightPx by remember { mutableFloatStateOf(0f) }
        var pinchBaselineFocalY by remember { mutableFloatStateOf(0f) }
        var measuredGridViewportHeightPx by remember { mutableFloatStateOf(0f) }
        var hasScrolledToCurrentTimeOnLaunch by remember { mutableStateOf(false) }
        val hourHeightPxState by rememberUpdatedState(hourHeightPx)
        val gridScrollOffsetPxState by rememberUpdatedState(gridScrollOffsetPx)
        val isPinchZoomActiveState by rememberUpdatedState(isPinchZoomActive)

        LaunchedEffect(
            style.scrollToCurrentTimeOnLaunch,
            layout.visibleDates,
            today,
            layout.hourHeightPx,
            layout.headerHeightPx,
            layout.viewportHeightPx,
        ) {
            if (hasScrolledToCurrentTimeOnLaunch || !style.scrollToCurrentTimeOnLaunch) {
                return@LaunchedEffect
            }
            if (!layout.visibleDates.contains(today)) {
                return@LaunchedEffect
            }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            if (now.hour < style.minHour || now.hour >= style.maxHour) {
                hasScrolledToCurrentTimeOnLaunch = true
                return@LaunchedEffect
            }

            val y = layout.hourY(now.hour, style.minHour) + (now.minute / 60f) * layout.hourHeightPx
            val viewportHeight = (layout.viewportHeightPx - layout.headerHeightPx).coerceAtLeast(0f)
            gridScrollOffsetPx = (y - viewportHeight / 2f).coerceAtLeast(0f)
            hasScrolledToCurrentTimeOnLaunch = true
        }

        val calculatedGridViewportHeightPx =
            (baseLayout.viewportHeightPx - layout.headerHeightPx).coerceAtLeast(0f)
        val gridViewportHeightPx = if (measuredGridViewportHeightPx > 0f) {
            measuredGridViewportHeightPx
        } else {
            calculatedGridViewportHeightPx
        }

        val pinchZoomConfig = remember(
            configuredMinHourHeightPx,
            configuredMaxHourHeightPx,
            baseLayout.viewportHeightPx,
            layout.headerHeightPx,
            style.hoursCount,
            gridViewportHeightPx,
        ) {
            WeekViewPinchZoomConfig(
                configuredMinHourHeightPx = configuredMinHourHeightPx,
                configuredMaxHourHeightPx = configuredMaxHourHeightPx,
                viewportHeightPx = baseLayout.viewportHeightPx,
                headerHeightPx = layout.headerHeightPx,
                hoursCount = style.hoursCount,
                viewportGridHeightPx = gridViewportHeightPx,
            )
        }
        val pinchZoomConfigState by rememberUpdatedState(pinchZoomConfig)

        fun layoutGridHeightForHourHeight(hourHeight: Float): Float {
            return with(density) { layoutHeightPx(style.hoursCount * hourHeight) }
        }

        fun maxGridScrollOffsetPx(hourHeight: Float = hourHeightPx): Float {
            return maxVerticalScrollOffsetPx(
                gridHeightPx = layoutGridHeightForHourHeight(hourHeight),
                viewportGridHeightPx = gridViewportHeightPx,
            )
        }

        fun clampGridScrollOffsetPx(scrollOffsetPx: Float, hourHeight: Float = hourHeightPx): Float {
            return scrollOffsetPx.coerceIn(0f, maxGridScrollOffsetPx(hourHeight))
        }

        fun pinchScrollForHourHeight(hourHeight: Float): Float {
            val layoutGridHeightPx = layoutGridHeightForHourHeight(hourHeight)
            val rawScrollOffsetPx = scrollOffsetForLayoutGridZoomAtFocalPoint(
                baselineScrollOffsetPx = pinchBaselineScrollOffsetPx,
                baselineLayoutGridHeightPx = pinchBaselineLayoutGridHeightPx,
                newLayoutGridHeightPx = layoutGridHeightPx,
                focalYInViewportPx = pinchBaselineFocalY,
            )
            return clampGridScrollOffsetPx(
                scrollOffsetPx = rawScrollOffsetPx,
                hourHeight = hourHeight,
            )
        }

        SideEffect {
            if (!isPinchZoomActive) {
                gridScrollOffsetPx = clampGridScrollOffsetPx(gridScrollOffsetPx)
            }
        }

        val maxGridScrollOffsetPxState by rememberUpdatedState(maxGridScrollOffsetPx())
        val gridScrollableState = rememberScrollableState { delta ->
            if (isPinchZoomActiveState) {
                return@rememberScrollableState 0f
            }
            val newOffsetPx = (gridScrollOffsetPxState - delta).coerceIn(0f, maxGridScrollOffsetPxState)
            val consumed = gridScrollOffsetPxState - newOffsetPx
            gridScrollOffsetPx = newOffsetPx
            consumed
        }

        val onPinchStart = remember(density) {
            { focalYInViewportPx: Float ->
                val currentScrollOffsetPx = clampGridScrollOffsetPx(gridScrollOffsetPx)
                isPinchZoomActive = true
                pinchBaselineScrollOffsetPx = currentScrollOffsetPx
                gridScrollOffsetPx = currentScrollOffsetPx
                pinchBaselineLayoutGridHeightPx = layoutGridHeightForHourHeight(hourHeightPx)
                pinchBaselineFocalY = focalYInViewportPx
            }
        }
        val onPinchStep = remember {
            { newHourHeightPx: Float ->
                hourHeightPx = newHourHeightPx
                gridScrollOffsetPx = pinchScrollForHourHeight(newHourHeightPx)
            }
        }
        val onPinchEnd = remember {
            { newHourHeightPx: Float ->
                hourHeightPx = newHourHeightPx
                gridScrollOffsetPx = pinchScrollForHourHeight(newHourHeightPx)
                isPinchZoomActive = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weekViewScrollGestures(
                    enabled = horizontalScrollingEnabled,
                    onHorizontalDrag = { delta ->
                        horizontalScrollOffsetPx = applyHorizontalScrollDelta(
                            offsetPx = horizontalScrollOffsetPx,
                            deltaPx = delta,
                            dayWidthPx = layout.dayWidthPx,
                            firstVisibleDate = anchorDate,
                            onFirstVisibleDateChange = { newDate ->
                                if (newDate != anchorDate) {
                                    anchorGeneration++
                                }
                                anchorDate = newDate
                                onFirstVisibleDateChange?.invoke(newDate)
                            },
                        )
                    },
                ),
        ) {
            WeekViewHeader(
                layout = layout,
                style = style,
                dateFormatter = dateFormatter,
                horizontalTranslationPx = horizontalTranslationPx,
                textMeasurer = headerTextMeasurer,
                allDayEventChips = allDayEventChips,
                allDayChipsByDate = allDayChipsByDate,
                allDayTextMeasurer = allDayTextMeasurer,
                allDayExpandProgress = expandProgress,
                useExpandedAllDayLayout = useExpandedAllDayLayout,
                allDayChipBoundsLayout = allDayChipBoundsLayout,
                showAllDayToggle = showAllDayToggle,
                onAllDayToggle = { allDayEventsExpanded = !allDayEventsExpanded },
                onAllDayEventClick = if (eventClickEnabled) onEventClick else null,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .background(style.backgroundColor)
                    .onSizeChanged { measuredGridViewportHeightPx = it.height.toFloat() },
            ) {
                val layoutGridHeightPx = with(density) { layoutHeightPx(gridLayout.gridHeightPx) }
                val gridHeightDp = with(density) { layoutGridHeightPx.toDp() }
                val displayGridLayout = if (abs(layoutGridHeightPx - gridLayout.gridHeightPx) < 0.5f) {
                    gridLayout
                } else {
                    gridLayout.copy(
                        hourHeightPx = layoutGridHeightPx / style.hoursCount,
                        gridHeightPx = layoutGridHeightPx,
                    )
                }
                val gridScrollModifier = Modifier
                    .weekViewGridScroll(scrollOffsetPx = { gridScrollOffsetPxState })
                    .scrollable(
                        state = gridScrollableState,
                        orientation = Orientation.Vertical,
                        enabled = !isPinchZoomActive,
                    )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeightDp)
                        .weekViewPinchZoom(
                            enabled = style.pinchToZoomEnabled,
                            zoomConfig = { pinchZoomConfigState },
                            hourHeightPx = { hourHeightPxState },
                            onPinchStart = onPinchStart,
                            onPinchStep = onPinchStep,
                            onPinchEnd = onPinchEnd,
                        )
                        .then(gridScrollModifier),
                ) {
                    WeekViewTimeColumn(
                        style = style,
                        hourHeightDp = hourHeightDp,
                        gridHeightDp = gridHeightDp,
                        timeFormatter = timeFormatter,
                    )

                    Box(
                        modifier = Modifier
                            .width(with(density) { layout.viewportGridWidthPx.toDp() })
                            .height(gridHeightDp)
                            .clipToBounds(),
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(with(density) { gridLayout.contentGridWidthPx.toDp() })
                                .height(gridHeightDp)
                                .weekViewEventClick(
                                    enabled = eventClickEnabled,
                                    eventChips = eventChips,
                                    horizontalTranslationPx = horizontalTranslationPx,
                                    onEventClick = onEventClick ?: {},
                                ),
                        ) {
                            translate(left = horizontalTranslationPx) {
                                drawWeekViewGrid(
                                    layout = displayGridLayout,
                                    style = style,
                                    today = today,
                                )

                                displayGridLayout.renderDates.forEach { date ->
                                    drawWeekViewEventChips(
                                        eventChips = chipsByDate[date].orEmpty(),
                                        layout = displayGridLayout,
                                        style = style,
                                        density = density,
                                        textMeasurer = eventTextMeasurer,
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
    }
}
