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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Compose Multiplatform week calendar view.
 *
 * Renders a scrollable date header, time column, day grid, current-time indicator, timed event
 * chips, and all-day events. Supports horizontal paging, pinch-to-zoom, tap, and drag-and-drop.
 *
 * @param events Timed and all-day events to display when [pagingState] is `null`. Ignored when
 *   [pagingState] is provided.
 * @param pagingState Optional month-based paging source. When set, events come from
 *   [WeekViewPagingState.events] and [WeekViewPagingState.submit] is used to return loaded pages.
 * @param blockedTimes Non-interactive time ranges drawn behind events on the day grid.
 * @param modifier Layout modifier for the outer container.
 * @param style Visual configuration ([WeekViewStyle.Default] when omitted).
 * @param firstVisibleDate First day column shown at the left edge of the viewport.
 * @param onFirstVisibleDateChange Called when the user scrolls horizontally to a new date range.
 *   Required for horizontal scrolling; when `null`, [WeekViewStyle.horizontalScrollingEnabled] has
 *   no effect.
 * @param onEventClick Called when the user taps a timed or all-day event chip.
 * @param onEventLongClick Called when the user long-presses a timed event chip. Return `true` to
 *   consume the gesture and prevent drag-and-drop; return `false` to allow drag when
 *   [onEventDrop] is set.
 * @param onEmptyViewClick Called when the user taps an empty area of the day grid (including over
 *   blocked time ranges).
 * @param onEmptyViewLongClick Called when the user long-presses an empty grid area.
 * @param onEventDrop Called when the user finishes dragging a timed event. Receives the event and
 *   its snapped start/end times. Requires [WeekViewStyle.dragAndDropEnabled].
 * @param scrollState Optional controller for programmatic scroll commands ([scrollToDate],
 *   [scrollToTime], [scrollToDateTime]). Create with [rememberWeekViewScrollState].
 * @param onHourHeightChanged Called when the user finishes a pinch-to-zoom gesture with the new
 *   hour row height. Use this to persist zoom level across sessions.
 * @param dateFormatter Formats date labels in the header row.
 * @param timeFormatter Formats hour labels in the time column.
 */
@PublicApi
@Composable
fun WeekView(
    events: List<WeekViewEvent> = emptyList(),
    modifier: Modifier = Modifier,
    blockedTimes: List<WeekViewBlockedTime> = emptyList(),
    pagingState: WeekViewPagingState? = null,
    style: WeekViewStyle = WeekViewStyle.Default,
    firstVisibleDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    onFirstVisibleDateChange: ((LocalDate) -> Unit)? = null,
    onEventClick: ((WeekViewEvent) -> Unit)? = null,
    onEventLongClick: ((WeekViewEvent) -> Boolean)? = null,
    onEmptyViewClick: ((LocalDateTime) -> Unit)? = null,
    onEmptyViewLongClick: ((LocalDateTime) -> Unit)? = null,
    onEventDrop: ((WeekViewEvent, LocalDateTime, LocalDateTime) -> Unit)? = null,
    scrollState: WeekViewScrollState? = null,
    onHourHeightChanged: ((Dp) -> Unit)? = null,
    dateFormatter: DateFormatter = { year, month, day, dayOfWeek ->
        defaultDateFormatter(year, month, day, dayOfWeek, style.numberOfVisibleDays)
    },
    timeFormatter: TimeFormatter = ::defaultTimeFormatter,
) {
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current.isLtr()
    val headerTextMeasurer = rememberTextMeasurer(cacheSize = 16)
    val eventTextMeasurer = rememberTextMeasurer(cacheSize = 64)
    val allDayTextMeasurer = rememberTextMeasurer(cacheSize = 32)
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val layoutEngine = remember { WeekViewLayoutEngine() }
    val gestureScope = remember { WeekViewGestureScope() }

    val horizontalScrollingEnabled = style.horizontalScrollingEnabled && onFirstVisibleDateChange != null
    val displayEvents = pagingState?.events ?: events
    val eventClickEnabled = onEventClick != null
    val gridTapEnabled = eventClickEnabled || onEmptyViewClick != null
    val gridLongPressEnabled = onEmptyViewLongClick != null || onEventLongClick != null ||
        (style.dragAndDropEnabled && onEventDrop != null)
    val eventDragEnabled = style.dragAndDropEnabled && onEventDrop != null
    val gridGesturesEnabled = gridTapEnabled || gridLongPressEnabled
    val horizontalScrollSnapState = rememberWeekViewHorizontalScrollSnapState()

    var pageOriginDate by remember {
        mutableStateOf(
            horizontalPageOriginDate(
                firstVisibleDate = firstVisibleDate,
                numberOfVisibleDays = style.numberOfVisibleDays,
                firstDayOfWeek = style.firstDayOfWeek,
            ),
        )
    }
    var anchorDate by remember {
        mutableStateOf(
            horizontalPageOriginDate(
                firstVisibleDate = firstVisibleDate,
                numberOfVisibleDays = style.numberOfVisibleDays,
                firstDayOfWeek = style.firstDayOfWeek,
            ),
        )
    }
    var horizontalScrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var allDayEventsExpanded by remember { mutableStateOf(false) }
    var anchorGeneration by remember { mutableIntStateOf(0) }
    var gridScrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var hourHeightPx by remember(style.hourHeightDp, density) {
        mutableFloatStateOf(with(density) { style.hourHeightDp.toPx() })
    }
    var isPinchZoomActive by remember { mutableStateOf(false) }
    var dragState by remember { mutableStateOf<WeekViewDragState?>(null) }
    var dragScrollEdge by remember { mutableStateOf(DragScrollEdge.None) }
    var pinchBaselineScrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var pinchBaselineLayoutGridHeightPx by remember { mutableFloatStateOf(0f) }
    var pinchBaselineFocalY by remember { mutableFloatStateOf(0f) }
    var measuredGridViewportHeightPx by remember { mutableFloatStateOf(0f) }
    var hasScrolledToCurrentTimeOnLaunch by remember { mutableStateOf(false) }
    var suppressTapGesturesUntilMillis by remember { mutableStateOf(0L) }

    val hourHeightDp = with(density) { hourHeightPx.toDp() }
    val onHourHeightChangedState by rememberUpdatedState(onHourHeightChanged)
    val hourHeightPxState by rememberUpdatedState(hourHeightPx)
    val gridScrollOffsetPxState by rememberUpdatedState(gridScrollOffsetPx)
    val isPinchZoomActiveState by rememberUpdatedState(isPinchZoomActive)
    val dragStateState by rememberUpdatedState(dragState)

    LaunchedEffect(style.hourHeightDp, density) {
        hourHeightPx = with(density) { style.hourHeightDp.toPx() }
    }

    LaunchedEffect(firstVisibleDate, style.numberOfVisibleDays, style.firstDayOfWeek) {
        val sync = syncExternalFirstVisibleDate(
            firstVisibleDate = firstVisibleDate,
            currentAnchorDate = anchorDate,
            currentScrollOffsetPx = horizontalScrollOffsetPx,
            numberOfVisibleDays = style.numberOfVisibleDays,
            firstDayOfWeek = style.firstDayOfWeek,
        )
        pageOriginDate = sync.pageOriginDate
        if (sync.anchorGenerationBump) {
            anchorDate = sync.anchorDate
            horizontalScrollOffsetPx = sync.scrollOffsetPx
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
            isLtr,
        ) {
            calculateWeekViewLayout(
                viewportWidthPx = with(density) { maxWidth.toPx() },
                viewportHeightPx = with(density) { maxHeight.toPx() },
                style = style,
                firstVisibleDate = anchorDate,
                density = density,
                horizontalScrollingEnabled = horizontalScrollingEnabled,
                hourHeightPxOverride = hourHeightPx,
                isLtr = isLtr,
            )
        }

        val horizontalTranslationPx = horizontalContentTranslationPx(
            scrollOffsetPx = horizontalScrollOffsetPx,
            dayWidthPx = baseLayout.dayWidthPx,
            scrollBufferDays = baseLayout.scrollBufferDays,
        )

        WeekViewHorizontalScrollSnapEffect(
            enabled = horizontalScrollingEnabled,
            snapState = horizontalScrollSnapState,
            anchorDate = anchorDate,
            scrollOffsetPx = horizontalScrollOffsetPx,
            dayWidthPx = baseLayout.dayWidthPx,
            style = style,
            isLtr = isLtr,
            onAnchorDateChange = { anchorDate = it },
            onScrollOffsetChange = { horizontalScrollOffsetPx = it },
            onFirstVisibleDateChange = onFirstVisibleDateChange,
            onAnchorGenerationBump = { anchorGeneration++ },
        )

        val eventDateRange = if (horizontalScrollingEnabled) {
            baseLayout.renderDates
        } else {
            baseLayout.visibleDates
        }

        val layoutConfig = remember(style) {
            WeekViewLayoutConfig.of(
                minHour = style.minHour,
                maxHour = style.maxHour,
                arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
            )
        }

        val chipLayers = rememberWeekViewChipLayers(
            events = displayEvents,
            blockedTimes = blockedTimes,
            eventDateRange = eventDateRange,
            anchorGeneration = anchorGeneration,
            style = style,
            layoutConfig = layoutConfig,
            layoutEngine = layoutEngine,
            renderDates = baseLayout.renderDates,
            density = density,
        )

        val expandProgress by animateFloatAsState(
            targetValue = if (allDayEventsExpanded) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "allDayExpandProgress",
        )

        val derivedLayouts = rememberWeekViewDerivedLayouts(
            baseLayout = baseLayout,
            maxAllDayEventsPerDay = chipLayers.maxAllDayEventsPerDay,
            expandProgress = expandProgress,
            arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
            style = style,
            density = density,
        )

        val useExpandedAllDayLayout = shouldUseExpandedAllDayLayout(
            showAllDayToggle = derivedLayouts.showAllDayToggle,
            allDayEventsExpanded = allDayEventsExpanded,
            expandProgress = expandProgress,
        )

        applyAllDayEventVisibility(
            allDayEventChips = chipLayers.allDayEventChips,
            allDayChipsByDate = chipLayers.allDayChipsByDate,
            renderDates = derivedLayouts.layout.renderDates,
            allDayEventsExpanded = useExpandedAllDayLayout,
            arrangeAllDayEventsVertically = style.arrangeAllDayEventsVertically,
        )
        prepareAllDayEventChipBounds(
            allDayEventChips = chipLayers.allDayEventChips,
            layout = derivedLayouts.allDayChipBoundsLayout,
            style = style,
            density = density,
            chipsByDate = chipLayers.allDayChipsByDate,
            useExpandedAllDayLayout = useExpandedAllDayLayout,
        )

        SideEffect {
            prepareEventChipBounds(
                layout = derivedLayouts.gridLayout,
                style = style,
                density = density,
                chipsByDate = chipLayers.chipsByDate,
            )
        }

        val calculatedGridViewportHeightPx =
            (baseLayout.viewportHeightPx - derivedLayouts.layout.headerHeightPx).coerceAtLeast(0f)
        val gridViewportHeightPx = if (measuredGridViewportHeightPx > 0f) {
            measuredGridViewportHeightPx
        } else {
            calculatedGridViewportHeightPx
        }

        LaunchedEffect(
            style.scrollToCurrentTimeOnLaunch,
            derivedLayouts.layout.visibleDates,
            today,
            derivedLayouts.layout.hourHeightPx,
            derivedLayouts.layout.headerHeightPx,
            gridViewportHeightPx,
        ) {
            if (hasScrolledToCurrentTimeOnLaunch || !style.scrollToCurrentTimeOnLaunch) {
                return@LaunchedEffect
            }
            val scrollOffsetPx = scrollOffsetForCurrentTime(
                layout = derivedLayouts.layout,
                style = style,
                today = today,
                gridViewportHeightPx = gridViewportHeightPx,
            )
            if (scrollOffsetPx != null) {
                gridScrollOffsetPx = scrollOffsetPx
            }
            hasScrolledToCurrentTimeOnLaunch = true
        }

        val pinchScrollOps = createPinchScrollOps(
            style = style,
            density = density,
            gridViewportHeightPx = gridViewportHeightPx,
            pinchBaselineScrollOffsetPx = { pinchBaselineScrollOffsetPx },
            pinchBaselineLayoutGridHeightPx = { pinchBaselineLayoutGridHeightPx },
            pinchBaselineFocalY = { pinchBaselineFocalY },
            hourHeightPx = hourHeightPx,
        )

        WeekViewProgrammaticScrollEffect(
            scrollState = scrollState,
            layout = derivedLayouts.layout,
            style = style,
            gridViewportHeightPx = gridViewportHeightPx,
            maxGridScrollOffsetPx = { pinchScrollOps.maxGridScrollOffsetPx() },
            clampGridScrollOffsetPx = pinchScrollOps.clampGridScrollOffsetPx,
            anchorDate = { anchorDate },
            horizontalScrollOffsetPx = { horizontalScrollOffsetPx },
            gridScrollOffsetPx = { gridScrollOffsetPx },
            onAnchorDateChange = { anchorDate = it },
            onHorizontalScrollOffsetChange = { horizontalScrollOffsetPx = it },
            onGridScrollOffsetChange = { gridScrollOffsetPx = it },
            onAnchorGenerationBump = { anchorGeneration++ },
            onFirstVisibleDateChange = onFirstVisibleDateChange,
            horizontalScrollSnapState = horizontalScrollSnapState,
        )

        SideEffect {
            scrollState?.updateObservedState(
                firstVisibleDate = firstVisibleDateFromScrollState(
                    anchorDate = anchorDate,
                    scrollOffsetPx = horizontalScrollOffsetPx,
                    dayWidthPx = derivedLayouts.layout.dayWidthPx,
                    numberOfVisibleDays = style.numberOfVisibleDays,
                    firstDayOfWeek = style.firstDayOfWeek,
                    isLtr = isLtr,
                ),
                gridScrollOffsetPx = gridScrollOffsetPx,
            )
        }

        WeekViewPagingEffect(
            pagingState = pagingState,
            settlementGeneration = anchorGeneration,
            firstVisibleDate = firstVisibleDateFromScrollState(
                anchorDate = anchorDate,
                scrollOffsetPx = horizontalScrollOffsetPx,
                dayWidthPx = derivedLayouts.layout.dayWidthPx,
                numberOfVisibleDays = style.numberOfVisibleDays,
                firstDayOfWeek = style.firstDayOfWeek,
                isLtr = isLtr,
            ),
            numberOfVisibleDays = style.numberOfVisibleDays,
            isLtr = isLtr,
        )

        SideEffect {
            if (!isPinchZoomActive) {
                gridScrollOffsetPx = pinchScrollOps.clampGridScrollOffsetPx(gridScrollOffsetPx)
            }
        }

        val pinchZoomConfig = remember(
            configuredMinHourHeightPx,
            configuredMaxHourHeightPx,
            baseLayout.viewportHeightPx,
            derivedLayouts.layout.headerHeightPx,
            style.hoursCount,
            gridViewportHeightPx,
        ) {
            WeekViewPinchZoomConfig(
                configuredMinHourHeightPx = configuredMinHourHeightPx,
                configuredMaxHourHeightPx = configuredMaxHourHeightPx,
                viewportHeightPx = baseLayout.viewportHeightPx,
                headerHeightPx = derivedLayouts.layout.headerHeightPx,
                hoursCount = style.hoursCount,
                viewportGridHeightPx = gridViewportHeightPx,
            )
        }
        val pinchZoomConfigState by rememberUpdatedState(pinchZoomConfig)

        val maxGridScrollOffsetPxState by rememberUpdatedState(pinchScrollOps.maxGridScrollOffsetPx())
        val gridScrollableState = rememberScrollableState { delta ->
            if (isPinchZoomActiveState || dragStateState != null) {
                return@rememberScrollableState 0f
            }
            val newOffsetPx = (gridScrollOffsetPxState - delta).coerceIn(0f, maxGridScrollOffsetPxState)
            val consumed = gridScrollOffsetPxState - newOffsetPx
            gridScrollOffsetPx = newOffsetPx
            consumed
        }

        val onPinchStart = remember(density, pinchScrollOps, gridViewportHeightPx) {
            { focalYInContentPx: Float ->
                val currentScrollOffsetPx = pinchScrollOps.clampGridScrollOffsetPx(gridScrollOffsetPx)
                isPinchZoomActive = true
                pinchBaselineScrollOffsetPx = currentScrollOffsetPx
                gridScrollOffsetPx = currentScrollOffsetPx
                pinchBaselineLayoutGridHeightPx = pinchScrollOps.layoutGridHeightForHourHeight(hourHeightPx)
                pinchBaselineFocalY = focalYInViewportPx(
                    focalYInContentPx = focalYInContentPx,
                    scrollOffsetPx = currentScrollOffsetPx,
                    viewportGridHeightPx = gridViewportHeightPx,
                )
            }
        }
        val onPinchStep = remember(pinchScrollOps) {
            { newHourHeightPx: Float ->
                hourHeightPx = newHourHeightPx
                gridScrollOffsetPx = pinchScrollOps.pinchScrollForHourHeight(newHourHeightPx)
            }
        }
        val onPinchEnd = remember(pinchScrollOps, density) {
            { newHourHeightPx: Float ->
                hourHeightPx = newHourHeightPx
                gridScrollOffsetPx = pinchScrollOps.pinchScrollForHourHeight(newHourHeightPx)
                isPinchZoomActive = false
                suppressTapGesturesUntilMillis = System.currentTimeMillis() + PINCH_TAP_SUPPRESSION_MILLIS
                onHourHeightChangedState?.invoke(with(density) { newHourHeightPx.toDp() })
                Unit
            }
        }

        WeekViewDragAutoScrollEffect(
            isDragging = dragState != null,
            dragScrollEdge = dragScrollEdge,
            dragState = dragState,
            gridScrollOffsetPx = gridScrollOffsetPx,
            maxGridScrollOffsetPx = { pinchScrollOps.maxGridScrollOffsetPx() },
            hourHeightPx = hourHeightPx,
            clampGridScrollOffsetPx = pinchScrollOps.clampGridScrollOffsetPx,
            onGridScrollOffsetChange = { gridScrollOffsetPx = it },
            onDragStateChange = { dragState = it },
            onDragScrollEdgeChange = { dragScrollEdge = it },
            anchorDate = anchorDate,
            isLtr = isLtr,
            onAnchorDateChange = { anchorDate = it },
            onAnchorGenerationBump = { anchorGeneration++ },
            onFirstVisibleDateChange = onFirstVisibleDateChange,
        )

        SideEffect {
            gestureScope.bindHorizontalScrollGestures(
                dragStateProvider = { dragState },
                isHorizontalSnappingProvider = { horizontalScrollSnapState.isSnapping },
                dayWidthPx = derivedLayouts.layout.dayWidthPx,
                anchorDateProvider = { anchorDate },
                horizontalScrollOffsetProvider = { horizontalScrollOffsetPx },
                isLtr = isLtr,
                onHorizontalScrollOffsetChange = { horizontalScrollOffsetPx = it },
                onAnchorDateChange = { anchorDate = it },
                onAnchorGenerationBump = { anchorGeneration++ },
                onHorizontalScrollSnapRequest = {
                    if (style.horizontalScrollSnapEnabled) {
                        horizontalScrollSnapState.requestSnap()
                    } else {
                        anchorGeneration++
                        onFirstVisibleDateChange?.invoke(anchorDate)
                    }
                },
                onHorizontalScrollStart = {
                    horizontalScrollSnapState.beginGesture(
                        currentPageStartDate(
                            pageOriginDate = pageOriginDate,
                            anchorDate = anchorDate,
                            scrollOffsetPx = horizontalScrollOffsetPx,
                            dayWidthPx = derivedLayouts.layout.dayWidthPx,
                            numberOfVisibleDays = style.numberOfVisibleDays,
                            firstDayOfWeek = style.firstDayOfWeek,
                            isLtr = isLtr,
                        ),
                    )
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weekViewScrollGestures(
                    enabled = horizontalScrollingEnabled,
                    gestureScope = gestureScope,
                ),
        ) {
            WeekViewHeader(
                layout = derivedLayouts.layout,
                style = style,
                today = today,
                dateFormatter = dateFormatter,
                horizontalTranslationPx = horizontalTranslationPx,
                textMeasurer = headerTextMeasurer,
                allDayEventChips = chipLayers.allDayEventChips,
                allDayChipsByDate = chipLayers.allDayChipsByDate,
                allDayTextMeasurer = allDayTextMeasurer,
                allDayExpandProgress = expandProgress,
                useExpandedAllDayLayout = useExpandedAllDayLayout,
                allDayChipBoundsLayout = derivedLayouts.allDayChipBoundsLayout,
                showAllDayToggle = derivedLayouts.showAllDayToggle,
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
                val layoutGridHeightPx = with(density) {
                    layoutHeightPx(derivedLayouts.gridLayout.gridHeightPx)
                }
                val gridHeightDp = with(density) { layoutGridHeightPx.toDp() }
                val displayGridLayout = derivedLayouts.gridLayout
                val hitTestGridLayout = with(density) {
                    resolveDisplayGridLayout(displayGridLayout, style)
                }

                val dragGhostChip = rememberDragGhostChip(
                    dragState = dragState,
                    displayGridLayout = displayGridLayout,
                    layoutConfig = layoutConfig,
                    style = style,
                    density = density,
                )

                SideEffect {
                    gestureScope.isTapBlocked = {
                        isPinchZoomActive ||
                            System.currentTimeMillis() < suppressTapGesturesUntilMillis
                    }
                    gestureScope.bindEventDragGestures(
                        eventChipsProvider = { chipLayers.eventChips },
                        horizontalTranslationPxProvider = { horizontalTranslationPx },
                        displayGridLayoutProvider = { hitTestGridLayout },
                        styleProvider = { style },
                        tapEnabled = gridTapEnabled,
                        longPressEnabled = gridLongPressEnabled,
                        dragEnabled = eventDragEnabled,
                        onEventClick = onEventClick,
                        onEmptyViewClick = onEmptyViewClick,
                        onEmptyViewLongClick = onEmptyViewLongClick,
                        onEventLongClick = onEventLongClick,
                        dragStateProvider = { dragState },
                        onDragStateChange = { dragState = it },
                        onDragScrollEdgeChange = { dragScrollEdge = it },
                        gridScrollOffsetPxProvider = { gridScrollOffsetPx },
                        gridViewportWidthPx = derivedLayouts.layout.viewportGridWidthPx,
                        gridViewportHeightPx = gridViewportHeightPx,
                        onEventDrop = onEventDrop,
                    )
                }

                WeekViewGridSection(
                    style = style,
                    gridLayout = derivedLayouts.gridLayout,
                    displayGridLayout = displayGridLayout,
                    layout = derivedLayouts.layout,
                    density = density,
                    today = today,
                    hourHeightDp = hourHeightDp,
                    gridHeightDp = gridHeightDp,
                    horizontalTranslationPx = horizontalTranslationPx,
                    chipsByDate = chipLayers.chipsByDate,
                    dragState = dragState,
                    dragGhostChip = dragGhostChip,
                    eventTextMeasurer = eventTextMeasurer,
                    timeFormatter = timeFormatter,
                    gestureScope = gestureScope,
                    gridGesturesEnabled = gridGesturesEnabled,
                    gridScrollOffsetPx = { gridScrollOffsetPxState },
                    gridScrollableState = gridScrollableState,
                    isPinchZoomActive = isPinchZoomActive,
                    pinchZoomConfig = { pinchZoomConfigState },
                    hourHeightPx = { hourHeightPxState },
                    onPinchStart = onPinchStart,
                    onPinchStep = onPinchStep,
                    onPinchEnd = onPinchEnd,
                )
            }
        }
    }
}

private const val PINCH_TAP_SUPPRESSION_MILLIS = 300L
