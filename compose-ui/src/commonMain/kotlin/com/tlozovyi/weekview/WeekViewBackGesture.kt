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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Returns whether [touchX] lies in the OS system-gesture band used for back navigation.
 *
 * On Android this matches [WindowInsets.systemGestures]. Legacy [android.view.View] week views
 * did not see these touches as taps or scrolls because the framework cancels the stream; Compose
 * pointer handlers must ignore or consume them explicitly.
 */
internal fun isTouchInSystemGestureEdge(
    touchX: Float,
    layoutWidthPx: Float,
    leftGestureInsetPx: Float,
    rightGestureInsetPx: Float,
    isRtl: Boolean,
): Boolean {
    if (layoutWidthPx <= 0f) {
        return false
    }
    return if (isRtl) {
        touchX > layoutWidthPx - rightGestureInsetPx
    } else {
        touchX < leftGestureInsetPx
    }
}

/**
 * Consumes touches that belong to the system back gesture so [WeekView] does not scroll or click.
 */
internal fun Modifier.consumeSystemBackGestureTouches(): Modifier = composed {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val leftGestureInsetPx = WindowInsets.systemGestures.getLeft(density, layoutDirection).toFloat()
    val rightGestureInsetPx = WindowInsets.systemGestures.getRight(density, layoutDirection).toFloat()
    val isRtl = layoutDirection == LayoutDirection.Rtl

    then(
        Modifier
            .consumeSystemGestureEdgeTouches(
                leftGestureInsetPx = leftGestureInsetPx,
                rightGestureInsetPx = rightGestureInsetPx,
                isRtl = isRtl,
            )
            .observePredictiveBackGestureInput(),
    )
}

@Composable
internal expect fun Modifier.observePredictiveBackGestureInput(): Modifier

private fun Modifier.consumeSystemGestureEdgeTouches(
    leftGestureInsetPx: Float,
    rightGestureInsetPx: Float,
    isRtl: Boolean,
): Modifier = pointerInput(leftGestureInsetPx, rightGestureInsetPx, isRtl) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        if (!isTouchInSystemGestureEdge(
                touchX = down.position.x,
                layoutWidthPx = size.width.toFloat(),
                leftGestureInsetPx = leftGestureInsetPx,
                rightGestureInsetPx = rightGestureInsetPx,
                isRtl = isRtl,
            )
        ) {
            return@awaitEachGesture
        }

        down.consume()
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            event.changes.forEach { change ->
                if (change.pressed) {
                    change.consume()
                }
            }
        } while (event.changes.any { it.pressed })
    }
}
