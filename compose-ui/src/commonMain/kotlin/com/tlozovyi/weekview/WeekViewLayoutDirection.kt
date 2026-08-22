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

import androidx.compose.ui.unit.LayoutDirection
import kotlinx.datetime.LocalDate

internal fun LayoutDirection.isLtr(): Boolean = this == LayoutDirection.Ltr

internal fun dateAtColumnOffset(
    anchorDate: LocalDate,
    columnOffset: Int,
    isLtr: Boolean,
): LocalDate {
    return if (isLtr) {
        anchorDate.plusDays(columnOffset)
    } else {
        anchorDate.plusDays(-columnOffset)
    }
}

internal fun columnOffsetBetween(
    anchorDate: LocalDate,
    date: LocalDate,
    isLtr: Boolean,
): Int {
    val dayDelta = (date.toEpochDays() - anchorDate.toEpochDays()).toInt()
    return if (isLtr) dayDelta else -dayDelta
}
