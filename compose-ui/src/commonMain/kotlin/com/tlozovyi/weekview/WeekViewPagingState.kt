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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.jvm.JvmName

/** Receives loaded events for a single [rememberWeekViewPagingState] fetch request. */
@PublicApi
fun interface WeekViewPagingSubmit {
    operator fun invoke(events: List<WeekViewEvent>)
}

/**
 * Paginated event source for [WeekView].
 *
 * Caches submitted events by calendar month and requests additional months through
 * [onLoadMore] when the user scrolls near an uncached month. Matches the behavior of View
 * `WeekView.PagingAdapter`: the current month plus its previous and next month are prefetched.
 */
@Stable
@PublicApi
class WeekViewPagingState internal constructor() {
    private val loadScope = CoroutineScope(SupervisorJob())
    private val controller = WeekViewPagingController<WeekViewEvent>(
        startTime = { it.startTime },
        endTime = { it.endTime },
        onLoadMore = { _, _, _ -> },
    )
    private var onRangeChanged: ((firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) -> Unit)? =
        null
    private var lastNotifiedFirstVisibleDate: LocalDate? = null
    private val displayedEventsState = mutableStateOf<List<WeekViewEvent>>(emptyList())

    val events: List<WeekViewEvent>
        get() = displayedEventsState.value

    /** Observe paging events for composition; prefer this over [events] inside [WeekView]. */
    internal val eventsState: State<List<WeekViewEvent>>
        get() = displayedEventsState

    private fun publishDisplayedEvents() {
        displayedEventsState.value = controller.items
    }

    internal fun updateCallbacks(
        onLoadMore: (startDate: LocalDate, endDate: LocalDate, submit: WeekViewPagingSubmit) -> Unit,
        onRangeChanged: ((firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) -> Unit)?,
    ) {
        controller.updateCallbacks { startDate, endDate, submit ->
            onLoadMore(startDate, endDate, wrapSubmit(submit))
        }
        this.onRangeChanged = onRangeChanged
    }

    private fun wrapSubmit(
        submit: (List<WeekViewEvent>) -> Unit,
    ): WeekViewPagingSubmit = WeekViewPagingSubmit { events ->
        submit(events)
        publishDisplayedEvents()
    }

    fun submit(events: List<WeekViewEvent>) {
        controller.submit(events)
        publishDisplayedEvents()
    }

    fun refresh() {
        controller.refresh()
        publishDisplayedEvents()
    }

    /**
     * Ensures events for the visible month window around [firstVisibleDate] are loaded.
     *
     * Called automatically by [WeekView] on first layout and when the visible date range changes.
     * Apps can call this manually when they need to prefetch before the user scrolls.
     */
    fun ensureLoaded(
        firstVisibleDate: LocalDate,
        numberOfVisibleDays: Int,
        isLtr: Boolean = true,
    ) {
        val rangeStart = visibleDateRange(
            firstVisibleDate = firstVisibleDate,
            numberOfVisibleDays = numberOfVisibleDays,
            isLtr = isLtr,
        ).first
        controller.onScrollSettled(rangeStart)
        publishDisplayedEvents()
    }

    internal fun onScrollSettled(
        firstVisibleDate: LocalDate,
        lastVisibleDate: LocalDate,
    ) {
        publishDisplayedEvents()
        if (lastNotifiedFirstVisibleDate != firstVisibleDate) {
            lastNotifiedFirstVisibleDate = firstVisibleDate
            onRangeChanged?.invoke(firstVisibleDate, lastVisibleDate)
        }
    }

    internal fun launchPagingLoad(block: suspend () -> Unit) {
        loadScope.launch {
            try {
                block()
            } catch (cancellation: CancellationException) {
                controller.abandonInFlightLoads()
                publishDisplayedEvents()
                throw cancellation
            } catch (_: Exception) {
                controller.abandonInFlightLoads()
                publishDisplayedEvents()
            }
        }
    }

    internal fun disposeLoads() {
        loadScope.cancel()
    }
}

/**
 * Creates and remembers a [WeekViewPagingState].
 *
 * The [submit] callback is scoped to the current fetch request — call it when your loader
 * finishes (sync or async). This avoids capturing [WeekViewPagingState] before it exists.
 *
 * @param onLoadMore Called when [WeekView] needs events for one or more consecutive months.
 * @param onRangeChanged Called when horizontal scrolling finishes and the leading visible date
 *   changes. Matches View `Adapter.onRangeChanged`.
 */
@PublicApi
@Composable
@JvmName("rememberWeekViewPagingStateWithSubmit")
fun rememberWeekViewPagingState(
    onLoadMore: (startDate: LocalDate, endDate: LocalDate, submit: WeekViewPagingSubmit) -> Unit,
    onRangeChanged: ((firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) -> Unit)? = null,
): WeekViewPagingState {
    val state = remember { WeekViewPagingState() }
    val latestOnLoadMore by rememberUpdatedState(onLoadMore)
    val latestOnRangeChanged by rememberUpdatedState(onRangeChanged)
    DisposableEffect(state) {
        onDispose { state.disposeLoads() }
    }
    SideEffect {
        state.updateCallbacks(
            onLoadMore = { startDate, endDate, submit ->
                latestOnLoadMore(startDate, endDate, submit)
            },
            onRangeChanged = latestOnRangeChanged,
        )
    }
    return state
}

/**
 * Suspend variant of [rememberWeekViewPagingState] for coroutine-based loaders.
 *
 * Returned events are submitted automatically when the suspend block completes.
 */
@PublicApi
@Composable
@JvmName("rememberWeekViewPagingStateWithLoader")
fun rememberWeekViewPagingState(
    onLoadMore: suspend (startDate: LocalDate, endDate: LocalDate) -> List<WeekViewEvent>,
    onRangeChanged: ((firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) -> Unit)? = null,
): WeekViewPagingState {
    val state = remember { WeekViewPagingState() }
    val latestOnLoadMore by rememberUpdatedState(onLoadMore)
    val latestOnRangeChanged by rememberUpdatedState(onRangeChanged)
    DisposableEffect(state) {
        onDispose { state.disposeLoads() }
    }
    SideEffect {
        state.updateCallbacks(
            onLoadMore = { startDate, endDate, submit ->
                state.launchPagingLoad {
                    submit(latestOnLoadMore(startDate, endDate))
                }
            },
            onRangeChanged = latestOnRangeChanged,
        )
    }
    return state
}

internal fun visibleDateRange(
    firstVisibleDate: LocalDate,
    numberOfVisibleDays: Int,
    isLtr: Boolean,
): Pair<LocalDate, LocalDate> {
    val lastVisibleDate = dateAtColumnOffset(
        anchorDate = firstVisibleDate,
        columnOffset = numberOfVisibleDays - 1,
        isLtr = isLtr,
    )
    return if (firstVisibleDate <= lastVisibleDate) {
        firstVisibleDate to lastVisibleDate
    } else {
        lastVisibleDate to firstVisibleDate
    }
}
