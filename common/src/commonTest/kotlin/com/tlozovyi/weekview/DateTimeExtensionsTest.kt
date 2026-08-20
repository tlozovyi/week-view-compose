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
import kotlin.test.assertNotEquals
import kotlinx.datetime.DayOfWeek

class DateTimeExtensionsTest {

    @Test
    fun returnsCorrectDayOfWeek() {
        val date = firstDayOfYear().withYear(2019)
        assertEquals(DayOfWeek.TUESDAY, date.dayOfWeek)
    }

    @Test
    fun doesCorrectEqualityCheck() {
        val first = firstDayOfYear().withYear(2019)
        val second = firstDayOfYear().withYear(2019)
        assertEquals(first, second)

        val newSecond = second.plusMinutes(1)
        assertNotEquals(first, newSecond)
    }

    @Test
    fun addsDaysCorrectly() {
        val date = firstDayOfYear().withYear(2019)
        val result = date.plusDays(2)
        assertEquals(3, result.date.dayOfMonth)

        val secondResult = date.plusDays(31)
        assertEquals(1, secondResult.date.dayOfMonth)
        assertEquals(2, secondResult.monthNumber)
    }
}
