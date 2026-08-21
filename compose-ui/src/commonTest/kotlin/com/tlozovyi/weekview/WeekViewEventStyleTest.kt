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

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeekViewEventStyleTest {

    @Test
    fun toArgbLongPreservesTransparentAlpha() {
        val transparent = Color.Red.copy(alpha = 0.2f)
        val restored = transparent.toArgbLong().toComposeColor()

        assertEquals(0.2f, restored.alpha, 0.01f)
    }

    @Test
    fun eventChipDrawColorMultipliesExistingAlpha() {
        val semiTransparent = Color.Blue.copy(alpha = 0.5f)

        assertEquals(0.5f, semiTransparent.eventChipDrawColor(isDragging = false).alpha, 0.01f)
        assertEquals(0.425f, semiTransparent.eventChipDrawColor(isDragging = true).alpha, 0.01f)
    }

    @Test
    fun eventChipDrawColorKeepsFullyTransparentBackgroundTransparent() {
        val transparent = Color.Green.copy(alpha = 0f)

        assertTrue(transparent.eventChipDrawColor(isDragging = false).alpha == 0f)
        assertTrue(transparent.eventChipDrawColor(isDragging = true).alpha == 0f)
    }
}
