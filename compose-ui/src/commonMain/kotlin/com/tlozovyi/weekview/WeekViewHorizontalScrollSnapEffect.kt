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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.abs

@Composable
internal fun rememberWeekViewHorizontalScrollSnapState(): WeekViewHorizontalScrollSnapState {
    return remember { WeekViewHorizontalScrollSnapState() }
}

internal class WeekViewHorizontalScrollSnapState {
    var snapGeneration by mutableIntStateOf(0)
        private set
    var isSnapping by mutableStateOf(false)
        internal set
    var gesturePageStartDate: LocalDate? = null
        internal set

    fun beginGesture(pageStartDate: LocalDate) {
        if (!isSnapping) {
            gesturePageStartDate = pageStartDate
        }
    }

    fun requestSnap() {
        snapGeneration++
    }

    fun clearGesture() {
        gesturePageStartDate = null
    }
}

@Composable
internal fun WeekViewHorizontalScrollSnapEffect(
    enabled: Boolean,
    snapState: WeekViewHorizontalScrollSnapState,
    anchorDate: LocalDate,
    scrollOffsetPx: Float,
    dayWidthPx: Float,
    style: WeekViewStyle,
    onAnchorDateChange: (LocalDate) -> Unit,
    onScrollOffsetChange: (Float) -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
    onAnchorGenerationBump: () -> Unit,
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(snapState.snapGeneration) {
        if (!enabled || !style.horizontalScrollSnapEnabled || snapState.snapGeneration == 0 || dayWidthPx <= 0f) {
            return@LaunchedEffect
        }

        val startAnchor = anchorDate
        val startOffset = scrollOffsetPx
        val gesturePageStart = snapState.gesturePageStartDate ?: startAnchor
        val target = snapHorizontalScrollTarget(
            gesturePageStart = gesturePageStart,
            anchorDate = startAnchor,
            scrollOffsetPx = startOffset,
            dayWidthPx = dayWidthPx,
            numberOfVisibleDays = style.numberOfVisibleDays,
            firstDayOfWeek = style.firstDayOfWeek,
        )
        val startReferenceScreenX = referenceColumnScreenX(
            anchorDate = startAnchor,
            scrollOffsetPx = startOffset,
            referenceDate = gesturePageStart,
            dayWidthPx = dayWidthPx,
        )
        val targetReferenceScreenX = referenceColumnScreenX(
            anchorDate = target.anchorDate,
            scrollOffsetPx = target.scrollOffsetPx,
            referenceDate = gesturePageStart,
            dayWidthPx = dayWidthPx,
        )

        if (abs(startReferenceScreenX - targetReferenceScreenX) < 0.5f) {
            snapState.clearGesture()
            return@LaunchedEffect
        }

        snapState.isSnapping = true
        try {
            animatable.snapTo(startReferenceScreenX)
            coroutineScope {
                val collector = launch {
                    snapshotFlow { animatable.value }.collect { value ->
                        val scrolled = scrollStateFromReferenceScreenX(
                            referenceColumnScreenX = value,
                            referenceDate = gesturePageStart,
                            dayWidthPx = dayWidthPx,
                        )
                        onAnchorDateChange(scrolled.anchorDate)
                        onScrollOffsetChange(scrolled.scrollOffsetPx)
                    }
                }
                animatable.animateTo(
                    targetValue = targetReferenceScreenX,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
                collector.cancel()
            }

            onAnchorDateChange(target.anchorDate)
            onScrollOffsetChange(target.scrollOffsetPx)
            onAnchorGenerationBump()
            onFirstVisibleDateChange?.invoke(target.anchorDate)
        } finally {
            snapState.isSnapping = false
            snapState.clearGesture()
        }
    }
}
