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

package com.tlozovyi.weekview.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tlozovyi.weekview.WeekView
import com.tlozovyi.weekview.rememberWeekViewScrollState
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun SampleApp() {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var selectedMode by remember { mutableStateOf(SampleViewMode.ThreeDaySnapping) }
    var firstVisibleDate by remember(selectedMode) { mutableStateOf(today) }
    var events by remember { mutableStateOf(sampleEvents(today)) }
    var blockedTimes by remember { mutableStateOf(sampleBlockedTimes(today)) }
    val scrollState = rememberWeekViewScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SampleModeTopBar(
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = selectedMode.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (selectedMode.showsStaticNavigation) {
                        StaticWeekNavigationBar(
                            firstVisibleDate = firstVisibleDate,
                            numberOfVisibleDays = selectedMode.style.numberOfVisibleDays,
                            onPrevious = {
                                firstVisibleDate = firstVisibleDate.plusDays(-numberOfVisibleDays(selectedMode))
                            },
                            onNext = {
                                firstVisibleDate = firstVisibleDate.plusDays(numberOfVisibleDays(selectedMode))
                            },
                            onJumpToToday = {
                                scope.launch {
                                    scrollState.scrollToDateTime(today.atTime(9, 0))
                                    firstVisibleDate = scrollState.firstVisibleDate
                                }
                            },
                        )
                    }

                    key(selectedMode) {
                        WeekView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            events = events,
                            blockedTimes = blockedTimes,
                            scrollState = scrollState,
                            style = selectedMode.style,
                            firstVisibleDate = firstVisibleDate,
                            onFirstVisibleDateChange = { updatedDate ->
                                if (selectedMode.style.horizontalScrollingEnabled) {
                                    firstVisibleDate = updatedDate
                                }
                            },
                            onEventClick = { event ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Selected: ${event.title}")
                                }
                            },
                            onEventLongClick = { event ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Long-press: ${event.title}")
                                }
                                true
                            },
                            onEmptyViewClick = { time ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Empty tap: ${time.date} ${time.hour}:${time.minute.toString().padStart(2, '0')}",
                                    )
                                }
                            },
                            onEmptyViewLongClick = { time ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Empty long-press: ${time.date} ${time.hour}:${time.minute.toString().padStart(2, '0')}",
                                    )
                                }
                            },
                            onEventDrop = { event, newStartTime, newEndTime ->
                                events = events.map { current ->
                                    if (current.id == event.id) {
                                        current.copy(startTime = newStartTime, endTime = newEndTime)
                                    } else {
                                        current
                                    }
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Moved ${event.title} to ${newStartTime.hour}:${newStartTime.minute.toString().padStart(2, '0')}",
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleModeTopBar(
    selectedMode: SampleViewMode,
    onModeSelected: (SampleViewMode) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text("WeekView Sample")
                Text(
                    text = selectedMode.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            Box {
                TextButton(onClick = { menuExpanded = true }) {
                    Text("Mode")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    SampleViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(mode.title)
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                            onClick = {
                                onModeSelected(mode)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun StaticWeekNavigationBar(
    firstVisibleDate: LocalDate,
    numberOfVisibleDays: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToToday: () -> Unit,
) {
    val lastVisibleDate = firstVisibleDate.plusDays(numberOfVisibleDays - 1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
        }
        Text(
            text = "$firstVisibleDate – $lastVisibleDate",
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(onClick = onJumpToToday) {
            Text("Today")
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
        }
    }
}

private fun numberOfVisibleDays(mode: SampleViewMode): Int = mode.style.numberOfVisibleDays
