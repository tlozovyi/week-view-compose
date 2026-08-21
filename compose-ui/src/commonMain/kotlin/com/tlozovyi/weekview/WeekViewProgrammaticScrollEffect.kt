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
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlin.math.abs

@Composable
internal fun WeekViewProgrammaticScrollEffect(
    scrollState: WeekViewScrollState?,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    gridViewportHeightPx: Float,
    maxGridScrollOffsetPx: () -> Float,
    clampGridScrollOffsetPx: (Float) -> Float,
    anchorDate: () -> LocalDate,
    horizontalScrollOffsetPx: () -> Float,
    gridScrollOffsetPx: () -> Float,
    onAnchorDateChange: (LocalDate) -> Unit,
    onHorizontalScrollOffsetChange: (Float) -> Unit,
    onGridScrollOffsetChange: (Float) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
    horizontalScrollSnapState: WeekViewHorizontalScrollSnapState,
) {
    if (scrollState == null || gridViewportHeightPx <= 0f || layout.dayWidthPx <= 0f) {
        return
    }

    val mutex = remember { Mutex() }

    DisposableEffect(
        scrollState,
        layout,
        style,
        gridViewportHeightPx,
        onFirstVisibleDateChange,
    ) {
        scrollState.connection = WeekViewScrollConnection(
            execute = { command ->
                mutex.withLock {
                    when (command) {
                        is WeekViewScrollCommand.Date -> {
                            scrollHorizontallyToDate(
                                targetDate = command.date,
                                animated = command.animated,
                                layout = layout,
                                style = style,
                                anchorDate = anchorDate(),
                                horizontalScrollOffsetPx = horizontalScrollOffsetPx(),
                                onAnchorDateChange = onAnchorDateChange,
                                onHorizontalScrollOffsetChange = onHorizontalScrollOffsetChange,
                                onAnchorGenerationBump = onAnchorGenerationBump,
                                onFirstVisibleDateChange = onFirstVisibleDateChange,
                                horizontalScrollSnapState = horizontalScrollSnapState,
                            )
                        }
                        is WeekViewScrollCommand.Time -> {
                            scrollVerticallyToTime(
                                hour = command.hour,
                                minute = command.minute,
                                animated = command.animated,
                                layout = layout,
                                style = style,
                                gridViewportHeightPx = gridViewportHeightPx,
                                maxGridScrollOffsetPx = maxGridScrollOffsetPx(),
                                clampGridScrollOffsetPx = clampGridScrollOffsetPx,
                                currentOffsetPx = gridScrollOffsetPx(),
                                onGridScrollOffsetChange = onGridScrollOffsetChange,
                            )
                        }
                        is WeekViewScrollCommand.DateTime -> {
                            val clampedDate = clampProgrammaticScrollDate(
                                date = command.dateTime.date,
                                minDate = style.minDate,
                                maxDate = style.maxDate,
                                numberOfVisibleDays = style.numberOfVisibleDays,
                            )
                            scrollHorizontallyToDate(
                                targetDate = clampedDate,
                                animated = command.animated,
                                layout = layout,
                                style = style,
                                anchorDate = anchorDate(),
                                horizontalScrollOffsetPx = horizontalScrollOffsetPx(),
                                onAnchorDateChange = onAnchorDateChange,
                                onHorizontalScrollOffsetChange = onHorizontalScrollOffsetChange,
                                onAnchorGenerationBump = onAnchorGenerationBump,
                                onFirstVisibleDateChange = onFirstVisibleDateChange,
                                horizontalScrollSnapState = horizontalScrollSnapState,
                            )
                            scrollVerticallyToTime(
                                hour = command.dateTime.hour,
                                minute = command.dateTime.minute,
                                animated = command.animated,
                                layout = layout,
                                style = style,
                                gridViewportHeightPx = gridViewportHeightPx,
                                maxGridScrollOffsetPx = maxGridScrollOffsetPx(),
                                clampGridScrollOffsetPx = clampGridScrollOffsetPx,
                                currentOffsetPx = gridScrollOffsetPx(),
                                onGridScrollOffsetChange = onGridScrollOffsetChange,
                            )
                        }
                    }
                }
            },
        )
        onDispose {
            scrollState.connection = null
        }
    }
}

private suspend fun scrollHorizontallyToDate(
    targetDate: LocalDate,
    animated: Boolean,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    anchorDate: LocalDate,
    horizontalScrollOffsetPx: Float,
    onAnchorDateChange: (LocalDate) -> Unit,
    onHorizontalScrollOffsetChange: (Float) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
    horizontalScrollSnapState: WeekViewHorizontalScrollSnapState,
) {
    val clampedDate = clampProgrammaticScrollDate(
        date = targetDate,
        minDate = style.minDate,
        maxDate = style.maxDate,
        numberOfVisibleDays = style.numberOfVisibleDays,
    )
    val target = horizontalScrollTargetForDate(
        targetDate = clampedDate,
        numberOfVisibleDays = style.numberOfVisibleDays,
        firstDayOfWeek = style.firstDayOfWeek,
    )

    if (isSameHorizontalScrollTarget(anchorDate, horizontalScrollOffsetPx, target)) {
        onFirstVisibleDateChange?.invoke(target.anchorDate)
        return
    }

    horizontalScrollSnapState.clearGesture()

    if (!animated) {
        applyHorizontalScrollTarget(
            target = target,
            onAnchorDateChange = onAnchorDateChange,
            onHorizontalScrollOffsetChange = onHorizontalScrollOffsetChange,
            onAnchorGenerationBump = onAnchorGenerationBump,
            onFirstVisibleDateChange = onFirstVisibleDateChange,
        )
        return
    }

    val dayWidthPx = layout.dayWidthPx
    val startScreenX = targetDateScreenX(
        anchorDate = anchorDate,
        scrollOffsetPx = horizontalScrollOffsetPx,
        targetDate = clampedDate,
        dayWidthPx = dayWidthPx,
    )
    val endScreenX = targetDateScreenX(
        anchorDate = target.anchorDate,
        scrollOffsetPx = target.scrollOffsetPx,
        targetDate = clampedDate,
        dayWidthPx = dayWidthPx,
    )

    if (abs(startScreenX - endScreenX) < 0.5f) {
        applyHorizontalScrollTarget(
            target = target,
            onAnchorDateChange = onAnchorDateChange,
            onHorizontalScrollOffsetChange = onHorizontalScrollOffsetChange,
            onAnchorGenerationBump = onAnchorGenerationBump,
            onFirstVisibleDateChange = onFirstVisibleDateChange,
        )
        return
    }

    val animatable = Animatable(startScreenX)
    animatable.animateTo(
        targetValue = endScreenX,
        animationSpec = tween(durationMillis = PROGRAMMATIC_SCROLL_DURATION_MILLIS),
    ) {
        val scrolled = scrollStateFromReferenceScreenX(
            referenceColumnScreenX = value,
            referenceDate = clampedDate,
            dayWidthPx = dayWidthPx,
        )
        onAnchorDateChange(scrolled.anchorDate)
        onHorizontalScrollOffsetChange(scrolled.scrollOffsetPx)
    }

    applyHorizontalScrollTarget(
        target = target,
        onAnchorDateChange = onAnchorDateChange,
        onHorizontalScrollOffsetChange = onHorizontalScrollOffsetChange,
        onAnchorGenerationBump = onAnchorGenerationBump,
        onFirstVisibleDateChange = onFirstVisibleDateChange,
    )
}

private fun applyHorizontalScrollTarget(
    target: HorizontalScrollSnapTarget,
    onAnchorDateChange: (LocalDate) -> Unit,
    onHorizontalScrollOffsetChange: (Float) -> Unit,
    onAnchorGenerationBump: () -> Unit,
    onFirstVisibleDateChange: ((LocalDate) -> Unit)?,
) {
    onAnchorDateChange(target.anchorDate)
    onHorizontalScrollOffsetChange(target.scrollOffsetPx)
    onAnchorGenerationBump()
    onFirstVisibleDateChange?.invoke(target.anchorDate)
}

private suspend fun scrollVerticallyToTime(
    hour: Int,
    minute: Int,
    animated: Boolean,
    layout: WeekViewLayout,
    style: WeekViewStyle,
    gridViewportHeightPx: Float,
    maxGridScrollOffsetPx: Float,
    clampGridScrollOffsetPx: (Float) -> Float,
    currentOffsetPx: Float,
    onGridScrollOffsetChange: (Float) -> Unit,
) {
    val targetOffsetPx = clampGridScrollOffsetPx(
        scrollOffsetForTime(
            layout = layout,
            style = style,
            hour = hour,
            minute = minute,
            gridViewportHeightPx = gridViewportHeightPx,
            maxGridScrollOffsetPx = maxGridScrollOffsetPx,
        ),
    )

    if (abs(currentOffsetPx - targetOffsetPx) < 0.5f) {
        onGridScrollOffsetChange(targetOffsetPx)
        return
    }

    if (!animated) {
        onGridScrollOffsetChange(targetOffsetPx)
        return
    }

    val animatable = Animatable(currentOffsetPx)
    animatable.animateTo(
        targetValue = targetOffsetPx,
        animationSpec = tween(durationMillis = PROGRAMMATIC_SCROLL_DURATION_MILLIS),
    ) {
        onGridScrollOffsetChange(clampGridScrollOffsetPx(value))
    }
    onGridScrollOffsetChange(targetOffsetPx)
}

internal const val PROGRAMMATIC_SCROLL_DURATION_MILLIS = 300
