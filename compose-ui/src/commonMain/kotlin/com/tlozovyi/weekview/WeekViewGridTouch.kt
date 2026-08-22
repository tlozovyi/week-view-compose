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

import androidx.compose.ui.geometry.Offset
import kotlinx.datetime.LocalDateTime

internal data class WeekViewGridLongPressResult(
    val handled: Boolean,
    val shouldStartDrag: Boolean,
)

internal fun resolveGridTap(
    offset: Offset,
    eventChips: List<EventChip>,
    horizontalTranslationPx: Float,
    displayGridLayout: WeekViewLayout,
    style: WeekViewStyle,
    gridScrollOffsetPx: Float,
    onEventClick: ((WeekViewEvent) -> Unit)?,
    onEmptyViewClick: ((LocalDateTime) -> Unit)?,
) {
    val contentX = offset.x - horizontalTranslationPx
    val contentY = offset.y + gridScrollOffsetPx
    val event = eventChips.findEventAt(x = contentX, y = contentY)
    if (event != null) {
        onEventClick?.invoke(event)
        return
    }

    val time = calculateTimeFromPoint(
        touchX = offset.x,
        touchY = offset.y,
        layout = displayGridLayout,
        style = style,
        horizontalTranslationPx = horizontalTranslationPx,
        gridScrollOffsetPx = gridScrollOffsetPx,
    )
    if (time != null) {
        onEmptyViewClick?.invoke(time)
    }
}

internal fun resolveGridLongPress(
    offset: Offset,
    eventChips: List<EventChip>,
    horizontalTranslationPx: Float,
    displayGridLayout: WeekViewLayout,
    style: WeekViewStyle,
    gridScrollOffsetPx: Float,
    dragEnabled: Boolean,
    onEventLongClick: ((WeekViewEvent) -> Boolean)?,
    onEmptyViewLongClick: ((LocalDateTime) -> Unit)?,
): WeekViewGridLongPressResult {
    val contentX = offset.x - horizontalTranslationPx
    val contentY = offset.y + gridScrollOffsetPx
    val event = eventChips.findEventAt(x = contentX, y = contentY)
    if (event != null) {
        val handled = onEventLongClick?.invoke(event) == true
        return WeekViewGridLongPressResult(
            handled = handled,
            shouldStartDrag = !handled && dragEnabled,
        )
    }

    val time = calculateTimeFromPoint(
        touchX = offset.x,
        touchY = offset.y,
        layout = displayGridLayout,
        style = style,
        horizontalTranslationPx = horizontalTranslationPx,
        gridScrollOffsetPx = gridScrollOffsetPx,
    )
    if (time != null) {
        onEmptyViewLongClick?.invoke(time)
    }
    return WeekViewGridLongPressResult(handled = true, shouldStartDrag = false)
}
