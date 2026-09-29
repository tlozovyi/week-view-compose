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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

private enum class ScrollAxis {
    Horizontal,
    Vertical,
}

internal fun Modifier.weekViewGridScroll(
    scrollOffsetPx: () -> Float,
): Modifier = layout { measurable, constraints ->
        val viewportHeight = constraints.maxHeight
        val placeable = measurable.measure(
            constraints.copy(
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            ),
        )
        val maxScroll = (placeable.height - viewportHeight).coerceAtLeast(0)
        val scroll = scrollOffsetPx().roundToInt().coerceIn(0, maxScroll)
        layout(constraints.maxWidth, viewportHeight) {
            placeable.placeRelative(0, -scroll)
        }
    }

internal fun shouldCancelGridTapWait(
    pressedPointerCount: Int,
    tapBlocked: Boolean,
): Boolean = pressedPointerCount >= 2 || tapBlocked

internal const val GRID_TAP_SCROLL_OFFSET_EPSILON_PX = 0.5f

internal fun hasGridPointerMovedBeyondTapSlop(
    downPosition: Offset,
    currentPosition: Offset,
    touchSlop: Float,
): Boolean = (currentPosition - downPosition).getDistance() > touchSlop

internal fun hasGridScrollOffsetChangedForTapWait(
    initialOffsetPx: Float,
    currentOffsetPx: Float,
): Boolean = abs(currentOffsetPx - initialOffsetPx) > GRID_TAP_SCROLL_OFFSET_EPSILON_PX

internal fun shouldCancelGridLongPressWait(
    downPosition: Offset,
    currentPosition: Offset,
    touchSlop: Float,
    pressedPointerCount: Int,
    tapBlocked: Boolean,
    initialGridScrollOffsetPx: Float,
    currentGridScrollOffsetPx: Float,
    initialHorizontalScrollOffsetPx: Float,
    currentHorizontalScrollOffsetPx: Float,
): Boolean {
    if (shouldCancelGridTapWait(pressedPointerCount, tapBlocked)) {
        return true
    }
    if (hasGridPointerMovedBeyondTapSlop(downPosition, currentPosition, touchSlop)) {
        return true
    }
    if (hasGridScrollOffsetChangedForTapWait(initialGridScrollOffsetPx, currentGridScrollOffsetPx)) {
        return true
    }
    if (hasGridScrollOffsetChangedForTapWait(initialHorizontalScrollOffsetPx, currentHorizontalScrollOffsetPx)) {
        return true
    }
    return false
}

internal fun Modifier.weekViewPinchZoom(
    enabled: Boolean,
    gestureScope: WeekViewGestureScope,
    zoomConfig: () -> WeekViewPinchZoomConfig,
    hourHeightPx: () -> Float,
    onPinchStart: (focalYInContentPx: Float) -> Unit,
    onPinchStep: (newHourHeightPx: Float) -> Unit,
    onPinchEnd: (newHourHeightPx: Float) -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(gestureScope) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)

            var event: PointerEvent
            do {
                event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.pressedPointerCount() < 2 && event.changes.any { it.pressed })

            if (event.pressedPointerCount() < 2 || gestureScope.isPinchBlocked()) {
                return@awaitEachGesture
            }

            val initialSpan = event.pinchSpan()
            if (initialSpan <= 0f) {
                return@awaitEachGesture
            }

            val baselineHourHeightPx = hourHeightPx()
            val baselineFocalYInContentPx = event.pinchCentroid().y
            onPinchStart(baselineFocalYInContentPx)

            var latestHourHeightPx = baselineHourHeightPx
            var latestSpan = initialSpan

            do {
                event.changes.forEach { change ->
                    if (change.pressed) {
                        change.consume()
                    }
                }

                val span = event.pinchSpan()
                if (span > 0f) {
                    latestSpan = span
                    val cumulativeScale = span / initialSpan
                    val result = applyPinchZoomFromBaseline(
                        baselineHourHeightPx = baselineHourHeightPx,
                        cumulativeScale = cumulativeScale,
                        baselineScrollOffsetPx = 0f,
                        focalYInViewportPx = focalYInViewportPx(
                            focalYInContentPx = baselineFocalYInContentPx,
                            scrollOffsetPx = gestureScope.gridScrollOffsetPx,
                            viewportGridHeightPx = zoomConfig().viewportGridHeightPx,
                        ),
                        config = zoomConfig(),
                    )
                    if (result != null) {
                        val (newHourHeightPx, _) = result
                        latestHourHeightPx = newHourHeightPx
                        onPinchStep(newHourHeightPx)
                    }
                }

                event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.pressedPointerCount() >= 2)

            do {
                event.changes.forEach { change ->
                    if (change.pressed) {
                        change.consume()
                    }
                }
                event = awaitPointerEvent(PointerEventPass.Initial)
            } while (event.changes.any { it.pressed })

            onPinchEnd(latestHourHeightPx)
        }
    }
}

private fun PointerEvent.pressedPointerCount(): Int = changes.count { it.pressed }

private fun PointerEvent.pinchSpan(): Float {
    val pressed = changes.filter { it.pressed }
    if (pressed.size < 2) {
        return 0f
    }
    return distanceBetween(pressed[0], pressed[1])
}

private fun PointerEvent.pinchCentroid(): Offset {
    val pressed = changes.filter { it.pressed }
    if (pressed.isEmpty()) {
        return Offset.Zero
    }
    var x = 0f
    var y = 0f
    pressed.forEach { change ->
        x += change.position.x
        y += change.position.y
    }
    val count = pressed.size.toFloat()
    return Offset(x / count, y / count)
}

private fun distanceBetween(first: PointerInputChange, second: PointerInputChange): Float {
    val dx = first.position.x - second.position.x
    val dy = first.position.y - second.position.y
    return hypot(dx, dy)
}

internal fun Modifier.weekViewScrollGestures(
    enabled: Boolean,
    gestureScope: WeekViewGestureScope,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(gestureScope) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val pointerId = down.id
            var totalX = 0f
            var totalY = 0f
            var scrollAxis: ScrollAxis? = null
            val touchSlop = viewConfiguration.touchSlop

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                if (!change.pressed) {
                    break
                }

                val delta = change.positionChange()
                if (delta == Offset.Zero) {
                    continue
                }

                totalX += delta.x
                totalY += delta.y

                if (scrollAxis == null) {
                    if (abs(totalX) > touchSlop || abs(totalY) > touchSlop) {
                        scrollAxis = if (abs(totalX) > abs(totalY)) {
                            ScrollAxis.Horizontal
                        } else {
                            ScrollAxis.Vertical
                        }
                        if (scrollAxis == ScrollAxis.Horizontal && !gestureScope.isScrollBlocked()) {
                            gestureScope.onHorizontalScrollStart()
                        }
                    }
                }

                if (scrollAxis == ScrollAxis.Horizontal && !gestureScope.isScrollBlocked()) {
                    change.consume()
                    gestureScope.onHorizontalDrag(delta.x)
                }
            }

            if (scrollAxis == ScrollAxis.Horizontal && !gestureScope.isScrollBlocked()) {
                gestureScope.onHorizontalScrollEnd()
            }
        }
    }
}

internal fun Modifier.weekViewAllDayToggleClick(
    enabled: Boolean,
    toggleAreaTopPx: Float,
    onToggle: () -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(toggleAreaTopPx) {
        detectTapGestures { offset ->
            if (offset.y >= toggleAreaTopPx) {
                onToggle()
            }
        }
    }
}

internal fun Modifier.weekViewAllDayEventClick(
    enabled: Boolean,
    eventChips: List<EventChip>,
    horizontalTranslationPx: Float,
    onEventClick: (WeekViewEvent) -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(eventChips, horizontalTranslationPx) {
        detectTapGestures { offset ->
            val event = eventChips.findAllDayEventAt(
                x = offset.x - horizontalTranslationPx,
                y = offset.y,
            )
            if (event != null) {
                onEventClick(event)
            }
        }
    }
}

internal fun Modifier.weekViewEventClick(
    enabled: Boolean,
    eventChips: List<EventChip>,
    horizontalTranslationPx: Float,
    onEventClick: (WeekViewEvent) -> Unit,
): Modifier {
    return weekViewTimedEventGestures(
        enabled = enabled,
        gestureScope = WeekViewGestureScope().apply {
            this.eventChips = eventChips
            this.horizontalTranslationPx = horizontalTranslationPx
            this.onEventClick = onEventClick
            this.tapEnabled = enabled
        },
    )
}

internal fun Modifier.weekViewTimedEventGestures(
    enabled: Boolean,
    gestureScope: WeekViewGestureScope,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(gestureScope) {
        awaitEachGesture {
            if (gestureScope.isTapBlocked()) {
                return@awaitEachGesture
            }
            if (!gestureScope.tapEnabled && !gestureScope.longPressEnabled) {
                return@awaitEachGesture
            }
            val down = awaitFirstDown(requireUnconsumed = false)
            val pointerId = down.id
            val touchSlop = viewConfiguration.touchSlop
            val longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis
            val layout = gestureScope.displayGridLayout ?: return@awaitEachGesture
            val initialGridScrollOffsetPx = gestureScope.gridScrollOffsetPx
            val initialHorizontalScrollOffsetPx = gestureScope.horizontalScrollOffsetPx

            val releasedBeforeLongPress = withTimeoutOrNull(longPressTimeoutMillis) {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: return@withTimeoutOrNull false
                    if (change.changedToUp()) {
                        return@withTimeoutOrNull true
                    }
                    if (!change.pressed) {
                        return@withTimeoutOrNull false
                    }
                    if (shouldCancelGridLongPressWait(
                            downPosition = down.position,
                            currentPosition = change.position,
                            touchSlop = touchSlop,
                            pressedPointerCount = event.changes.count { it.pressed },
                            tapBlocked = gestureScope.isTapBlocked(),
                            initialGridScrollOffsetPx = initialGridScrollOffsetPx,
                            currentGridScrollOffsetPx = gestureScope.gridScrollOffsetPx,
                            initialHorizontalScrollOffsetPx = initialHorizontalScrollOffsetPx,
                            currentHorizontalScrollOffsetPx = gestureScope.horizontalScrollOffsetPx,
                        )
                    ) {
                        return@withTimeoutOrNull false
                    }
                }
            }

            when {
                releasedBeforeLongPress == true &&
                    gestureScope.tapEnabled &&
                    !gestureScope.isTapBlocked() -> {
                    resolveGridTap(
                        offset = down.position,
                        eventChips = gestureScope.eventChips,
                        horizontalTranslationPx = gestureScope.horizontalTranslationPx,
                        displayGridLayout = layout,
                        style = gestureScope.style,
                        onEventClick = gestureScope.onEventClick,
                        onEmptyViewClick = gestureScope.onEmptyViewClick,
                    )
                }

                releasedBeforeLongPress == null &&
                    gestureScope.longPressEnabled &&
                    !gestureScope.isTapBlocked() -> {
                    val longPressResult = resolveGridLongPress(
                        offset = down.position,
                        eventChips = gestureScope.eventChips,
                        horizontalTranslationPx = gestureScope.horizontalTranslationPx,
                        displayGridLayout = layout,
                        style = gestureScope.style,
                        dragEnabled = gestureScope.dragEnabled,
                        onEventLongClick = gestureScope.onEventLongClick,
                        onEmptyViewLongClick = gestureScope.onEmptyViewLongClick,
                    )
                    if (longPressResult.shouldStartDrag) {
                        val event = gestureScope.eventChips.findEventAt(
                            x = down.position.x - gestureScope.horizontalTranslationPx,
                            y = gridContentYForHitTest(down.position.y),
                        )
                        if (event != null) {
                            gestureScope.onDragStart(event, down.position)
                            try {
                                while (true) {
                                    val dragEvent = awaitPointerEvent(PointerEventPass.Main)
                                    val dragChange = dragEvent.changes.firstOrNull { it.id == pointerId }
                                    if (dragChange == null || !dragChange.pressed) {
                                        gestureScope.onDragEnd()
                                        break
                                    }
                                    dragChange.consume()
                                    gestureScope.onDragMove(dragChange.position)
                                }
                            } catch (cancellation: CancellationException) {
                                gestureScope.onDragCancel()
                                throw cancellation
                            }
                        }
                    }
                }
            }
        }
    }
}
