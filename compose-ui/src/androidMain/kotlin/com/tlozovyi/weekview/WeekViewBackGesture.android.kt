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

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal actual fun Modifier.observePredictiveBackGestureInput(): Modifier = composed {
    val suppressInput = remember { AtomicBoolean(false) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backDispatcher) {
        if (backDispatcher == null) {
            return@DisposableEffect onDispose {}
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                suppressInput.set(true)
            }

            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                suppressInput.set(true)
            }

            override fun handleOnBackCancelled() {
                suppressInput.set(false)
            }

            override fun handleOnBackPressed() {
                suppressInput.set(false)
                isEnabled = false
                backDispatcher.onBackPressed()
                isEnabled = true
            }
        }
        backDispatcher.addCallback(callback)
        onDispose { callback.remove() }
    }

    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (!suppressInput.get()) {
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
}
