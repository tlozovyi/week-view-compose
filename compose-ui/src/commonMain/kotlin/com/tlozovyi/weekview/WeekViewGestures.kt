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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

private enum class ScrollAxis {
    Horizontal,
    Vertical,
}

internal fun Modifier.weekViewScrollGestures(
    enabled: Boolean,
    onHorizontalDrag: (Float) -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(Unit) {
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
                    }
                }

                if (scrollAxis == ScrollAxis.Horizontal) {
                    change.consume()
                    onHorizontalDrag(delta.x)
                }
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
    if (!enabled) {
        return this
    }

    return pointerInput(eventChips, horizontalTranslationPx) {
        detectTapGestures { offset ->
            val event = eventChips.findEventAt(
                x = offset.x - horizontalTranslationPx,
                y = offset.y,
            )
            if (event != null) {
                onEventClick(event)
            }
        }
    }
}
