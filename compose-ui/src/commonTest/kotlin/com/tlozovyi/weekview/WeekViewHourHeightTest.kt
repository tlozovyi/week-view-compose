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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeekViewHourHeightTest {

    @Test
    fun effectiveMinUsesConfiguredMinWhenGridFillsViewport() {
        val effectiveMin = effectiveMinHourHeightPx(
            configuredMinHourHeightPx = 20f,
            viewportHeightPx = 800f,
            headerHeightPx = 100f,
            hoursCount = 10,
            gridHeightPx = 700f,
        )

        assertEquals(20f, effectiveMin)
    }

    @Test
    fun effectiveMinRaisesToFitViewportWhenGridIsShorter() {
        val effectiveMin = effectiveMinHourHeightPx(
            configuredMinHourHeightPx = 20f,
            viewportHeightPx = 800f,
            headerHeightPx = 100f,
            hoursCount = 10,
            gridHeightPx = 500f,
        )

        assertEquals(70f, effectiveMin)
    }

    @Test
    fun clampHourHeightRespectsConfiguredMax() {
        val clamped = clampHourHeightPx(
            requestedHourHeightPx = 500f,
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 120f,
            viewportHeightPx = 800f,
            headerHeightPx = 100f,
            hoursCount = 10,
        )

        assertEquals(120f, clamped)
    }

    @Test
    fun clampHourHeightRaisesMinimumWhenPinchingOutPastViewportFit() {
        val clamped = clampHourHeightPx(
            requestedHourHeightPx = 10f,
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 120f,
            viewportHeightPx = 800f,
            headerHeightPx = 100f,
            hoursCount = 10,
        )

        assertEquals(70f, clamped)
    }

    @Test
    fun focalYInViewportPxSubtractsScrollOffset() {
        assertEquals(
            150f,
            focalYInViewportPx(
                focalYInContentPx = 350f,
                scrollOffsetPx = 200f,
                viewportGridHeightPx = 400f,
            ),
        )
    }

    @Test
    fun scrollOffsetForLayoutGridZoomAtFocalPointUsesViewportFocalY() {
        val scrollAfter = scrollOffsetForLayoutGridZoomAtFocalPoint(
            baselineScrollOffsetPx = 200f,
            baselineLayoutGridHeightPx = 500f,
            newLayoutGridHeightPx = 1000f,
            focalYInViewportPx = 150f,
        )

        assertEquals(550f, scrollAfter)

        val contentBefore = 200f + 150f
        val contentAfter = scrollAfter + 150f
        assertEquals(contentBefore * 2f, contentAfter)
    }

    @Test
    fun scrollOffsetForZoomKeepsFocalPointStable() {
        val scrollBefore = 200f
        val focalY = 150f
        val scale = 2f

        val scrollAfter = scrollOffsetForZoomAtFocalPoint(
            currentScrollOffsetPx = scrollBefore,
            focalYInViewportPx = focalY,
            hourHeightScale = scale,
        )

        assertEquals(550f, scrollAfter)

        val contentBefore = scrollBefore + focalY
        val contentAfter = scrollAfter + focalY
        assertEquals(contentBefore * scale, contentAfter)
    }

    @Test
    fun applyPinchZoomStepReturnsNullWhenClampedWithoutChange() {
        val config = WeekViewPinchZoomConfig(
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 120f,
            viewportHeightPx = 800f,
            headerHeightPx = 100f,
            hoursCount = 10,
            viewportGridHeightPx = 700f,
        )

        val result = applyPinchZoomStep(
            currentHourHeightPx = 120f,
            scaleFactor = 1.2f,
            focalYInViewportPx = 100f,
            currentScrollOffsetPx = 50f,
            config = config,
        )

        assertNull(result)
    }

    @Test
    fun clampVerticalScrollOffsetPxKeepsScrollWithinGridBounds() {
        assertEquals(
            0f,
            clampVerticalScrollOffsetPx(
                scrollOffsetPx = -50f,
                gridHeightPx = 600f,
                viewportGridHeightPx = 400f,
            ),
        )
        assertEquals(
            200f,
            clampVerticalScrollOffsetPx(
                scrollOffsetPx = 500f,
                gridHeightPx = 600f,
                viewportGridHeightPx = 400f,
            ),
        )
    }

    @Test
    fun applyPinchZoomFromBaselineIgnoresFocalMovementWhenScaleUnchanged() {
        val config = WeekViewPinchZoomConfig(
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 200f,
            viewportHeightPx = 900f,
            headerHeightPx = 100f,
            hoursCount = 10,
            viewportGridHeightPx = 400f,
        )

        assertNull(
            applyPinchZoomFromBaseline(
                baselineHourHeightPx = 50f,
                cumulativeScale = 1f,
                baselineScrollOffsetPx = 100f,
                focalYInViewportPx = 200f,
                config = config,
            ),
        )
    }

    @Test
    fun applyPinchZoomFromBaselineUsesBaselineFocalNotCurrentFingerPosition() {
        val config = WeekViewPinchZoomConfig(
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 200f,
            viewportHeightPx = 900f,
            headerHeightPx = 100f,
            hoursCount = 10,
            viewportGridHeightPx = 400f,
        )

        val anchoredAt50 = applyPinchZoomFromBaseline(
            baselineHourHeightPx = 50f,
            cumulativeScale = 2f,
            baselineScrollOffsetPx = 100f,
            focalYInViewportPx = 50f,
            config = config,
        )
        val wouldDriftAt150 = applyPinchZoomFromBaseline(
            baselineHourHeightPx = 50f,
            cumulativeScale = 2f,
            baselineScrollOffsetPx = 100f,
            focalYInViewportPx = 150f,
            config = config,
        )

        assertEquals(100f to 250f, anchoredAt50)
        assertEquals(100f to 350f, wouldDriftAt150)
    }

    @Test
    fun applyPinchZoomFromBaselineUsesCumulativeScale() {
        val config = WeekViewPinchZoomConfig(
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 200f,
            viewportHeightPx = 900f,
            headerHeightPx = 100f,
            hoursCount = 10,
            viewportGridHeightPx = 400f,
        )

        val result = applyPinchZoomFromBaseline(
            baselineHourHeightPx = 50f,
            cumulativeScale = 2f,
            baselineScrollOffsetPx = 100f,
            focalYInViewportPx = 50f,
            config = config,
        )

        assertEquals(100f to 250f, result)
    }

    @Test
    fun applyPinchZoomStepAdjustsScrollForFocalPoint() {
        val config = WeekViewPinchZoomConfig(
            configuredMinHourHeightPx = 20f,
            configuredMaxHourHeightPx = 200f,
            viewportHeightPx = 900f,
            headerHeightPx = 100f,
            hoursCount = 10,
            viewportGridHeightPx = 400f,
        )

        val result = applyPinchZoomStep(
            currentHourHeightPx = 50f,
            scaleFactor = 2f,
            focalYInViewportPx = 50f,
            currentScrollOffsetPx = 100f,
            config = config,
        )

        assertEquals(100f to 250f, result)
    }
}
