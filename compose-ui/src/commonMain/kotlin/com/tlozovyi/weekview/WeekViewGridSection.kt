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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate

@Composable
internal fun WeekViewGridSection(
    style: WeekViewStyle,
    gridLayout: WeekViewLayout,
    displayGridLayout: WeekViewLayout,
    layout: WeekViewLayout,
    density: Density,
    today: LocalDate,
    hourHeightDp: Dp,
    gridHeightDp: Dp,
    horizontalTranslationPx: Float,
    chipsByDate: Map<LocalDate, List<EventChip>>,
    dragState: WeekViewDragState?,
    dragGhostChip: EventChip?,
    eventTextMeasurer: TextMeasurer,
    timeFormatter: TimeFormatter,
    gestureScope: WeekViewGestureScope,
    gridGesturesEnabled: Boolean,
    gridScrollOffsetPx: () -> Float,
    gridScrollableState: ScrollableState,
    isPinchZoomActive: Boolean,
    pinchZoomConfig: () -> WeekViewPinchZoomConfig,
    hourHeightPx: () -> Float,
    onPinchStart: (Float) -> Unit,
    onPinchStep: (Float) -> Unit,
    onPinchEnd: (Float) -> Unit,
) {
    val layoutGridHeightPx = with(density) { layoutHeightPx(gridLayout.gridHeightPx) }
    val resolvedDisplayLayout = with(density) {
        resolveDisplayGridLayout(gridLayout, style)
    }

    val gridScrollModifier = Modifier
        .weekViewGridScroll(scrollOffsetPx = gridScrollOffsetPx)
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
                gestureScope = gestureScope,
                zoomConfig = pinchZoomConfig,
                hourHeightPx = hourHeightPx,
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
            isLtr = layout.isLtr,
        )

        Box(
            modifier = Modifier
                .width(with(density) { layout.viewportGridWidthPx.toDp() })
                .height(gridHeightDp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
            ) {
                Canvas(
                    modifier = Modifier
                        .width(with(density) { gridLayout.contentGridWidthPx.toDp() })
                        .height(gridHeightDp)
                        .weekViewTimedEventGestures(
                            enabled = gridGesturesEnabled,
                            gestureScope = gestureScope,
                        ),
                ) {
                    translate(left = horizontalTranslationPx) {
                        drawWeekViewGrid(
                            layout = resolvedDisplayLayout,
                            style = style,
                            today = today,
                        )

                        resolvedDisplayLayout.renderDates.forEach { date ->
                            drawWeekViewEventChips(
                                eventChips = chipsByDate[date].orEmpty(),
                                layout = resolvedDisplayLayout,
                                style = style,
                                density = density,
                                textMeasurer = eventTextMeasurer,
                                draggingEventId = dragState?.eventId,
                                ghostChip = if (dragState?.currentStartTime?.date == date) {
                                    dragGhostChip
                                } else {
                                    null
                                },
                            )
                        }

                        drawWeekViewNowLine(
                            layout = resolvedDisplayLayout,
                            style = style,
                            today = today,
                            horizontalTranslationPx = horizontalTranslationPx,
                        )

                        if (style.showTimeColumnSeparator) {
                            val separatorX = if (layout.isLtr) {
                                0f
                            } else {
                                size.width - style.daySeparatorWidthDp.toPx()
                            }
                            drawLine(
                                color = style.timeColumnSeparatorColor,
                                start = androidx.compose.ui.geometry.Offset(separatorX, 0f),
                                end = androidx.compose.ui.geometry.Offset(separatorX, size.height),
                                strokeWidth = style.daySeparatorWidthDp.toPx(),
                            )
                        }
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { clip = false },
            ) {
                translate(left = horizontalTranslationPx) {
                    drawWeekViewNowDot(
                        layout = resolvedDisplayLayout,
                        style = style,
                        today = today,
                        horizontalTranslationPx = horizontalTranslationPx,
                    )
                }
            }
        }
    }
}
