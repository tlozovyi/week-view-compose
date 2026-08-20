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

import kotlin.math.max

/**
 * Computes the minimum hour height in pixels, matching the original View library:
 * users cannot pinch out further than the configured hour range filling the viewport.
 */
internal fun effectiveMinHourHeightPx(
    configuredMinHourHeightPx: Float,
    viewportHeightPx: Float,
    headerHeightPx: Float,
    hoursCount: Int,
    gridHeightPx: Float,
): Float {
    if (hoursCount <= 0) {
        return configuredMinHourHeightPx
    }

    val viewportGridHeightPx = (viewportHeightPx - headerHeightPx).coerceAtLeast(0f)
    val isNotFillingEntireHeight = gridHeightPx < viewportGridHeightPx
    if (!isNotFillingEntireHeight) {
        return configuredMinHourHeightPx
    }

    val fitHourHeightPx = viewportGridHeightPx / hoursCount
    return max(configuredMinHourHeightPx, fitHourHeightPx)
}

internal fun clampHourHeightPx(
    requestedHourHeightPx: Float,
    configuredMinHourHeightPx: Float,
    configuredMaxHourHeightPx: Float,
    viewportHeightPx: Float,
    headerHeightPx: Float,
    hoursCount: Int,
): Float {
    val gridHeightPx = hoursCount * requestedHourHeightPx
    val minHourHeightPx = effectiveMinHourHeightPx(
        configuredMinHourHeightPx = configuredMinHourHeightPx,
        viewportHeightPx = viewportHeightPx,
        headerHeightPx = headerHeightPx,
        hoursCount = hoursCount,
        gridHeightPx = gridHeightPx,
    )
    val maxHourHeightPx = max(minHourHeightPx, configuredMaxHourHeightPx)
    return requestedHourHeightPx.coerceIn(minHourHeightPx, maxHourHeightPx)
}

internal fun maxVerticalScrollOffsetPx(
    gridHeightPx: Float,
    viewportGridHeightPx: Float,
): Float {
    return (gridHeightPx - viewportGridHeightPx).coerceAtLeast(0f)
}

internal fun clampVerticalScrollOffsetPx(
    scrollOffsetPx: Float,
    gridHeightPx: Float,
    viewportGridHeightPx: Float,
): Float {
    return scrollOffsetPx.coerceIn(
        minimumValue = 0f,
        maximumValue = maxVerticalScrollOffsetPx(
            gridHeightPx = gridHeightPx,
            viewportGridHeightPx = viewportGridHeightPx,
        ),
    )
}

/**
 * Keeps the grid content under [focalYInViewportPx] fixed while [hourHeightScale] is applied.
 */
internal fun scrollOffsetForZoomAtFocalPoint(
    currentScrollOffsetPx: Float,
    focalYInViewportPx: Float,
    hourHeightScale: Float,
): Float {
    if (hourHeightScale == 1f) {
        return currentScrollOffsetPx
    }
    return currentScrollOffsetPx * hourHeightScale + focalYInViewportPx * (hourHeightScale - 1f)
}

/**
 * Focal-point zoom scroll using layout grid heights so scroll scale matches Compose layout size.
 */
internal fun scrollOffsetForLayoutGridZoomAtFocalPoint(
    baselineScrollOffsetPx: Float,
    baselineLayoutGridHeightPx: Float,
    newLayoutGridHeightPx: Float,
    focalYInViewportPx: Float,
): Float {
    if (baselineLayoutGridHeightPx <= 0f) {
        return baselineScrollOffsetPx
    }
    val heightScale = newLayoutGridHeightPx / baselineLayoutGridHeightPx
    return scrollOffsetForZoomAtFocalPoint(
        currentScrollOffsetPx = baselineScrollOffsetPx,
        focalYInViewportPx = focalYInViewportPx,
        hourHeightScale = heightScale,
    )
}

internal data class WeekViewPinchZoomConfig(
    val configuredMinHourHeightPx: Float,
    val configuredMaxHourHeightPx: Float,
    val viewportHeightPx: Float,
    val headerHeightPx: Float,
    val hoursCount: Int,
    val viewportGridHeightPx: Float,
)

/**
 * Applies one pinch step: clamps hour height, scrolls to keep the focal point stable, returns the
 * new hour height in pixels, or null when clamped with no effective change.
 */
internal fun applyPinchZoomFromBaseline(
    baselineHourHeightPx: Float,
    cumulativeScale: Float,
    baselineScrollOffsetPx: Float,
    focalYInViewportPx: Float,
    config: WeekViewPinchZoomConfig,
): Pair<Float, Float>? {
    return applyPinchZoomStep(
        currentHourHeightPx = baselineHourHeightPx,
        scaleFactor = cumulativeScale,
        focalYInViewportPx = focalYInViewportPx,
        currentScrollOffsetPx = baselineScrollOffsetPx,
        config = config,
    )
}

internal fun applyPinchZoomStep(
    currentHourHeightPx: Float,
    scaleFactor: Float,
    focalYInViewportPx: Float,
    currentScrollOffsetPx: Float,
    config: WeekViewPinchZoomConfig,
): Pair<Float, Float>? {
    if (scaleFactor == 1f) {
        return null
    }

    val newHourHeightPx = clampHourHeightPx(
        requestedHourHeightPx = currentHourHeightPx * scaleFactor,
        configuredMinHourHeightPx = config.configuredMinHourHeightPx,
        configuredMaxHourHeightPx = config.configuredMaxHourHeightPx,
        viewportHeightPx = config.viewportHeightPx,
        headerHeightPx = config.headerHeightPx,
        hoursCount = config.hoursCount,
    )
    if (newHourHeightPx == currentHourHeightPx) {
        return null
    }

    val hourHeightScale = newHourHeightPx / currentHourHeightPx
    val newGridHeightPx = config.hoursCount * newHourHeightPx
    val newScrollOffsetPx = clampVerticalScrollOffsetPx(
        scrollOffsetPx = scrollOffsetForZoomAtFocalPoint(
            currentScrollOffsetPx = currentScrollOffsetPx,
            focalYInViewportPx = focalYInViewportPx,
            hourHeightScale = hourHeightScale,
        ),
        gridHeightPx = newGridHeightPx,
        viewportGridHeightPx = config.viewportGridHeightPx,
    )

    return newHourHeightPx to newScrollOffsetPx
}

internal fun clampPinchHourHeightPx(
    baselineHourHeightPx: Float,
    cumulativeScale: Float,
    config: WeekViewPinchZoomConfig,
): Float {
    if (cumulativeScale == 1f) {
        return baselineHourHeightPx
    }
    return clampHourHeightPx(
        requestedHourHeightPx = baselineHourHeightPx * cumulativeScale,
        configuredMinHourHeightPx = config.configuredMinHourHeightPx,
        configuredMaxHourHeightPx = config.configuredMaxHourHeightPx,
        viewportHeightPx = config.viewportHeightPx,
        headerHeightPx = config.headerHeightPx,
        hoursCount = config.hoursCount,
    )
}
