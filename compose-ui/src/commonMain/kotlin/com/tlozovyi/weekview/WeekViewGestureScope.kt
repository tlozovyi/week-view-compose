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

/**
 * Mutable callback holder wired into [pointerInput] gesture detectors.
 *
 * Updated from [androidx.compose.runtime.SideEffect] on each frame so gesture blocks are not
 * recreated when drag or scroll state changes mid-gesture.
 */
internal class WeekViewGestureScope {
    var isScrollBlocked: () -> Boolean = { false }
    var onHorizontalDrag: (Float) -> Unit = {}
    var onHorizontalScrollStart: () -> Unit = {}
    var onHorizontalScrollEnd: () -> Unit = {}
    var isPinchBlocked: () -> Boolean = { false }
    var eventChips: List<EventChip> = emptyList()
    var horizontalTranslationPx: Float = 0f
    var onEventClick: (WeekViewEvent) -> Unit = {}
    var onDragStart: (WeekViewEvent, Offset) -> Unit = { _, _ -> }
    var onDragMove: (Offset) -> Unit = {}
    var onDragEnd: () -> Unit = {}
    var onDragCancel: () -> Unit = {}
}
