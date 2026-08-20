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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Library version exposed to consumers. */
@PublicApi
const val WEEK_VIEW_COMPOSE_VERSION: String = "0.1.0-alpha"

/**
 * Compose Multiplatform week calendar view.
 *
 * This is an early alpha placeholder. Rendering, gestures, and event layout
 * will be implemented in upcoming releases.
 */
@PublicApi
@Composable
fun WeekView(
    events: List<WeekViewEvent>,
    modifier: Modifier = Modifier,
    style: WeekViewStyle = WeekViewStyle.Default,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildString {
                append("Week View Compose ")
                append(WEEK_VIEW_COMPOSE_VERSION)
                append("\n")
                append(style.numberOfVisibleDays)
                append(" day view · ")
                append(events.size)
                append(" events")
            },
            modifier = Modifier.padding(16.dp),
            color = style.headerTextColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
