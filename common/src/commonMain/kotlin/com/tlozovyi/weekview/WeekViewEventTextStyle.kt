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

/**
 * Typography decorations for event chip labels.
 *
 * Set on [WeekViewEvent.titleTextStyle] and [WeekViewEvent.subtitleTextStyle]; attributes combine
 * freely (for example strikethrough and italic together).
 */
@PublicApi
data class WeekViewEventTextStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
) {
    companion object {
        /** Default chip label styling (no decorations). */
        val Default = WeekViewEventTextStyle()
    }
}
