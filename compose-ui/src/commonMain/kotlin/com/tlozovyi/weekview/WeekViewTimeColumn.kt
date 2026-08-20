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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WeekViewTimeColumn(
    style: WeekViewStyle,
    hourHeightDp: Dp,
    gridHeightDp: Dp,
    timeFormatter: TimeFormatter,
) {
    val hours = style.hours
    Box(
        modifier = Modifier
            .width(style.timeColumnWidthDp)
            .height(gridHeightDp)
            .background(style.timeColumnBackgroundColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            hours.forEachIndexed { index, hour ->
                val rowHeight = if (hour == hours.last) {
                    gridHeightDp - hourHeightDp * (hours.count() - 1)
                } else {
                    hourHeightDp
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = timeFormatter(hour),
                        modifier = Modifier.padding(end = style.timeColumnPaddingDp),
                        color = style.timeColumnTextColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
