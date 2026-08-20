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

class PeriodTest {

    @Test
    fun returnsCorrectPreviousPeriodForJanuary() {
        val period = Period.fromDate(firstDayOfYear())
        val previous = period.previous

        assertEquals(12, previous.month)
        assertEquals(period.year - 1, previous.year)
    }

    @Test
    fun returnsCorrectPreviousPeriodForAugust() {
        val date = firstDayOfYear().withMonth(8)
        val period = Period.fromDate(date)
        val previous = period.previous

        assertEquals(7, previous.month)
        assertEquals(period.year, previous.year)
    }

    @Test
    fun returnsCorrectNextPeriodForAugust() {
        val date = firstDayOfYear().withMonth(8)
        val period = Period.fromDate(date)
        val next = period.next

        assertEquals(9, next.month)
        assertEquals(period.year, next.year)
    }

    @Test
    fun returnsCorrectNextPeriodForDecember() {
        val date = firstDayOfYear().withMonth(12)
        val period = Period.fromDate(date)
        val next = period.next

        assertEquals(1, next.month)
        assertEquals(period.year + 1, next.year)
    }
}
