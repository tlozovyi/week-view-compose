/*
 * Copyright 2014 Raquib-ul-Alam
 * Copyright 2018 Till Hellmund
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

import kotlinx.datetime.LocalDateTime

internal class EventChipsFactory {

    fun create(
        events: List<ResolvedWeekViewEntity>,
        config: WeekViewLayoutConfig,
    ): List<EventChip> {
        val eventChips = convertEventsToEventChips(events, config)
        val groups = eventChips.groupedByDate().values

        for (group in groups) {
            computePositionOfEvents(group, config)
        }

        return eventChips
    }

    private fun convertEventsToEventChips(
        events: List<ResolvedWeekViewEntity>,
        config: WeekViewLayoutConfig,
    ): List<EventChip> {
        return events.sortedByTime().sanitize(config).toEventChips(config)
    }

    private fun List<ResolvedWeekViewEntity>.sortedByTime(): List<ResolvedWeekViewEntity> {
        return sortedWith(compareBy({ it.startTime }, { it.endTime }))
    }

    private fun List<ResolvedWeekViewEntity>.sanitize(config: WeekViewLayoutConfig): List<ResolvedWeekViewEntity> {
        return map { it.sanitize(config) }
    }

    private fun List<ResolvedWeekViewEntity>.toEventChips(config: WeekViewLayoutConfig): List<EventChip> {
        return map { event ->
            val eventParts = event.split(config)
            eventParts.mapIndexed { index, eventPart ->
                EventChip(
                    event = event,
                    index = index,
                    startTime = eventPart.startTime,
                    endTime = eventPart.endTime,
                )
            }
        }.flatten()
    }

    private fun computePositionOfEvents(eventChips: List<EventChip>, config: WeekViewLayoutConfig) {
        val singleEventChips = eventChips.filter { it.event.isNotAllDay }
        val allDayEventChips = eventChips.filter { it.event.isAllDay }

        val singleEventGroups = singleEventChips.toMultiColumnCollisionGroups()
        val allDayGroups = if (config.arrangeAllDayEventsVertically) {
            allDayEventChips.toSingleColumnCollisionGroups()
        } else {
            allDayEventChips.toMultiColumnCollisionGroups()
        }

        for (collisionGroup in singleEventGroups) {
            expandEventsToMaxWidth(collisionGroup, config)
        }

        for (collisionGroup in allDayGroups) {
            expandEventsToMaxWidth(collisionGroup, config)
        }
    }

    private fun List<EventChip>.toSingleColumnCollisionGroups(): List<CollisionGroup> {
        return map { CollisionGroup(it) }
    }

    private fun List<EventChip>.toMultiColumnCollisionGroups(): List<CollisionGroup> {
        val collisionGroups = mutableListOf<CollisionGroup>()

        for (eventChip in this) {
            val collidingGroup = collisionGroups.firstOrNull { it.collidesWith(eventChip) }

            if (collidingGroup != null) {
                collidingGroup.add(eventChip)
            } else {
                collisionGroups += CollisionGroup(eventChip)
            }
        }

        return collisionGroups
    }

    private fun expandEventsToMaxWidth(collisionGroup: CollisionGroup, config: WeekViewLayoutConfig) {
        val columns = mutableListOf<Column>()
        columns += Column(index = 0)

        for (eventChip in collisionGroup.eventChips) {
            val fittingColumns = columns.filter { it.fits(eventChip) }
            when (fittingColumns.size) {
                0 -> {
                    val index = columns.size
                    columns += Column(index, eventChip)
                }
                1 -> {
                    val fittingColumn = fittingColumns.single()
                    fittingColumn.add(eventChip)
                }
                else -> {
                    val areAdjacentColumns = fittingColumns.map { it.index }.isContinuous
                    if (areAdjacentColumns) {
                        for (column in fittingColumns) {
                            column.add(eventChip)
                        }
                    } else {
                        val leftMostColumn = checkNotNull(fittingColumns.minByOrNull { it.index })
                        leftMostColumn.add(eventChip)
                    }
                }
            }
        }

        val rows = columns.map { it.size }.maxOrNull() ?: 0
        val columnWidth = 1f / columns.size

        for (row in 0 until rows) {
            val zipped = columns.zipWithPrevious()
            for ((previous, current) in zipped) {
                val hasEventInRow = current.size > row
                if (hasEventInRow) {
                    expandColumnEventToMaxWidth(current, previous, row, columnWidth, columns.size)
                }
            }
        }

        for (eventChip in collisionGroup.eventChips) {
            calculateMinutesFromStart(eventChip, config)
        }
    }

    private fun calculateMinutesFromStart(eventChip: EventChip, config: WeekViewLayoutConfig) {
        if (eventChip.event.isAllDay) {
            return
        }

        eventChip.minutesFromStartHour = config.minutesFromStart(eventChip.startTime)
    }

    private fun expandColumnEventToMaxWidth(
        current: Column,
        previous: Column?,
        row: Int,
        columnWidth: Float,
        columns: Int,
    ) {
        val index = current.index
        val eventChip = current[row]

        val duplicateInPreviousColumn = previous?.findDuplicate(eventChip)

        if (duplicateInPreviousColumn != null) {
            duplicateInPreviousColumn.relativeWidth += columnWidth
        } else {
            eventChip.relativeWidth = columnWidth
            eventChip.relativeStart = index.toFloat() / columns
        }
    }

    private class CollisionGroup(
        val eventChips: MutableList<EventChip>,
    ) {

        constructor(eventChip: EventChip) : this(mutableListOf(eventChip))

        fun collidesWith(eventChip: EventChip): Boolean {
            return eventChips.any { it.event.collidesWith(eventChip.event) }
        }

        fun add(eventChip: EventChip) {
            eventChips.add(eventChip)
        }
    }

    private class Column(
        val index: Int,
        val eventChips: MutableList<EventChip> = mutableListOf(),
    ) {

        constructor(index: Int, eventChip: EventChip) : this(index, mutableListOf(eventChip))

        val isEmpty: Boolean
            get() = eventChips.isEmpty()

        val size: Int
            get() = eventChips.size

        fun add(eventChip: EventChip) {
            eventChips.add(eventChip)
        }

        fun findDuplicate(eventChip: EventChip) = eventChips.firstOrNull { it == eventChip }

        operator fun get(index: Int): EventChip = eventChips[index]

        fun fits(eventChip: EventChip): Boolean {
            return isEmpty || !eventChips.last().event.collidesWith(eventChip.event)
        }
    }

    private val List<Int>.isContinuous: Boolean
        get() {
            val zipped = sorted().zipWithNext()
            return zipped.all { it.first + 1 == it.second }
        }

    private fun <T> List<T>.zipWithPrevious(): List<Pair<T?, T>> {
        val results = mutableListOf<Pair<T?, T>>()
        for (index in indices) {
            val previous = getOrNull(index - 1)
            val current = get(index)
            results += Pair(previous, current)
        }
        return results
    }

    private fun List<EventChip>.groupedByDate(): Map<LocalDateTime, List<EventChip>> {
        return groupBy { it.startTime.atStartOfDay }
    }
}

private fun ResolvedWeekViewEntity.sanitize(config: WeekViewLayoutConfig): ResolvedWeekViewEntity {
    return if (endTime.isAtStartOfPeriod(hour = config.minHour)) {
        createCopy(endTime = endTime.minusMillis(1))
    } else {
        this
    }
}
