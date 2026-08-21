# Week View Compose

[![](https://jitpack.io/v/tlozovyi/week-view-compose.svg)](https://jitpack.io/#tlozovyi/week-view-compose)

Compose Multiplatform calendar week view for **Android** and **iOS**.

**Status:** `0.10.0-beta` — calendar grid, event chips, blocked time, gestures, programmatic scroll (`WeekViewScrollState`), horizontal page snap, all-day events, pinch-to-zoom, and drag-and-drop.

## Features

- Single-day and multi-day calendar views (1, 3, 7, or any custom column count)
- Timed event chips with overlap layout; optional subtitle and adaptive text size
- All-day events in the header row (vertical or horizontal arrangement)
- Tap handling for timed and all-day events; empty-slot tap and long-press
- Blocked time ranges (non-interactive; taps fall through to empty-slot callbacks)
- Programmatic scroll via `WeekViewScrollState` (`scrollToDate`, `scrollToTime`, `scrollToDateTime`)
- Continuous horizontal scrolling with optional page snap on finger release
- Week-aligned 7-day mode via `firstDayOfWeek`
- Free-scroll mode when `horizontalScrollSnapEnabled = false`
- Pinch-to-zoom hour row height
- Long-press drag-and-drop to move timed events (15-minute snap, edge auto-scroll)
- Current-time indicator line and dot
- Per-event styling via `WeekViewEventStyle`
- Extensive theming via `WeekViewStyle`
- Shared layout algorithms in a platform-agnostic `common` module (`kotlinx-datetime`)
- Written in Kotlin

### Not yet implemented

- Adapter / paging API
- Emoji support in event titles
- RTL layout
- Accessibility

## Versions (0.10.0-beta)

| | |
|---|---|
| **Release** | 0.10.0-beta |
| **minSdk (Android)** | 24 |
| **compileSdk / targetSdk** | 35 |
| **Kotlin** | 2.1.20 |
| **Compose Multiplatform** | 1.7.3 |
| **AGP** | 8.6.1 (JDK 17 required to build from source) |

## Lineage

This project is a Compose Multiplatform reimplementation **inspired by** [Android Week View](https://github.com/alamkanak/Android-Week-View). Calendar layout concepts and algorithms from that lineage are ported to Kotlin Multiplatform with `kotlinx-datetime`. This is not a drop-in replacement for the View-based library.

| | Repository |
|---|---|
| **Original author** (2014) | [alamkanak/Android-Week-View](https://github.com/alamkanak/Android-Week-View) by [Raquib-ul-Alam](https://github.com/alamkanak) |
| **Maintainer fork** | [thellmund/Android-Week-View](https://github.com/thellmund/Android-Week-View) by [Till Hellmund](https://github.com/thellmund) |
| **View library (XML)** | [tlozovyi/Android-Week-View](https://github.com/tlozovyi/Android-Week-View) — maintained View-based fork used as the primary reference for gesture and snap behaviour |
| **Compose library (this repo)** | [tlozovyi/week-view-compose](https://github.com/tlozovyi/week-view-compose) |

## Getting started

- Take a look at the [sample app](sample/src/commonMain/kotlin/com/tlozovyi/weekview/sample/SampleApp.kt) for a working integration with multiple view modes.
- See [CHANGELOG.md](CHANGELOG.md) for release notes and API changes.
- See [docs/xml-vs-compose-parity.md](docs/xml-vs-compose-parity.md) for XML vs Compose feature gaps and 1.0.0 roadmap.
- For the View-based library, see the [Android Week View wiki](https://github.com/tlozovyi/Android-Week-View/wiki).

## Dependency

Add the [JitPack](https://jitpack.io/#tlozovyi/week-view-compose) repository, then depend on the UI module:

```kotlin
// settings.gradle.kts / build.gradle.kts
repositories {
    maven("https://jitpack.io")
}
```

```kotlin
// commonMain (Kotlin Multiplatform)
implementation("com.github.tlozovyi.week-view-compose:compose-ui:0.10.0-beta")
```

When building from source, add both modules to your project:

```kotlin
implementation(project(":common"))
implementation(project(":compose-ui"))
```

## Usage

### Basic 3-day view

`onFirstVisibleDateChange` is required for horizontal scrolling. Hold `firstVisibleDate` in your state and pass updates back into `WeekView`.

```kotlin
var firstVisibleDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }

WeekView(
    events = events,
    style = WeekViewStyle(
        numberOfVisibleDays = 3,
        minHour = 7,
        maxHour = 22,
    ),
    firstVisibleDate = firstVisibleDate,
    onFirstVisibleDateChange = { firstVisibleDate = it },
    onEventClick = { event -> /* handle tap */ },
    onEventLongClick = { event ->
        /* return true to consume; false to allow drag when onEventDrop is set */
        false
    },
    onEmptyViewClick = { time -> /* create event at time */ },
    onEmptyViewLongClick = { time -> /* e.g. show create menu */ },
)
```

### Programmatic scroll

```kotlin
val scrollState = rememberWeekViewScrollState()
var firstVisibleDate by remember { mutableStateOf(today) }

WeekView(
    events = events,
    scrollState = scrollState,
    firstVisibleDate = firstVisibleDate,
    onFirstVisibleDateChange = { firstVisibleDate = it },
)

LaunchedEffect(selectedEvent) {
    scrollState.scrollToDateTime(selectedEvent.startTime)
    firstVisibleDate = scrollState.firstVisibleDate
}
```

### Blocked time

```kotlin
WeekView(
    events = events,
    blockedTimes = listOf(
        WeekViewBlockedTime(
            id = 100,
            startTime = date.atTime(12, 0),
            endTime = date.atTime(13, 0),
            title = "Lunch break",
        ),
    ),
    onEmptyViewClick = { time -> /* fires when tapping blocked ranges too */ },
)
```

### Events

```kotlin
WeekViewEvent(
    id = 1,
    title = "Meeting",
    startTime = LocalDate(2026, 8, 20).atTime(9, 0),
    endTime = LocalDate(2026, 8, 20).atTime(10, 0),
)

WeekViewEvent(
    id = 2,
    title = "Holiday",
    startTime = LocalDate(2026, 8, 20).atTime(0, 0),
    endTime = LocalDate(2026, 8, 21).atTime(0, 0),
    isAllDay = true,
)
```

All-day events render as chips in the header row below the date labels. When a day has more than two all-day events, the header collapses to one chip plus a `+N` label; tap the arrow in the time column to expand or collapse.

Per-event colors:

```kotlin
WeekViewEvent(
    id = 3,
    title = "Important",
    startTime = start,
    endTime = end,
    style = WeekViewEventStyle(
        backgroundColor = Color(0xFFE57373),
        textColor = Color.White,
    ),
)
```

Subtitles and adaptive text size:

```kotlin
WeekViewEvent(
    id = 4,
    title = "Product sync",
    subtitle = "Roadmap Q4",
    startTime = start,
    endTime = end,
)

WeekViewStyle(
    adaptiveEventTextSize = true,  // shrink labels to fit short chips (default false)
    eventTextSizeSp = 12.sp,
)
```

### Horizontal scroll and snap

Page snap runs when the user lifts their finger. In 7-day mode, pages align to `firstDayOfWeek`.

```kotlin
WeekViewStyle(
    numberOfVisibleDays = 7,
    horizontalScrollSnapEnabled = true,   // default
    firstDayOfWeek = DayOfWeek.MONDAY,
)
```

Free scroll — no snap animation; the view stays where you leave it:

```kotlin
WeekViewStyle(
    numberOfVisibleDays = 7,
    horizontalScrollSnapEnabled = false,
)
```

Static week — disable horizontal scrolling and drive the date range yourself (e.g. with prev/next buttons):

```kotlin
WeekViewStyle(
    numberOfVisibleDays = 7,
    horizontalScrollingEnabled = false,
)
```

### Pinch-to-zoom

Pinch with two fingers on the day grid to zoom hour row height in and out. Limits are controlled by `minHourHeightDp`, `maxHourHeightDp`, and `pinchToZoomEnabled` on `WeekViewStyle`.

### Drag-and-drop

Provide `onEventDrop` to enable long-press drag on timed event chips. Times snap to 15-minute increments; dragging near the grid edge auto-scrolls.

```kotlin
WeekView(
    events = events,
    style = WeekViewStyle(dragAndDropEnabled = true),
    onEventDrop = { event, newStart, newEnd ->
        events = events.map { current ->
            if (current.id == event.id) {
                current.copy(startTime = newStart, endTime = newEnd)
            } else {
                current
            }
        }
    },
)
```

## Modules

| Module | Description |
|--------|-------------|
| `common` | Shared models, date utilities, event layout algorithms |
| `compose-ui` | `@Composable WeekView` — the UI layer |
| `sample` | Demo app for Android and iOS |

## Requirements

- JDK 17+
- Android Studio Ladybug or newer (for Android targets)
- Xcode 15+ (for iOS targets)

## Build

```bash
# Run shared unit tests
./gradlew :common:cleanAllTests :common:allTests :compose-ui:testDebugUnitTest

# Build Android sample
./gradlew :sample:assembleDebug

# Run iOS sample (requires macOS + Xcode 15+)
open iosApp/iosApp.xcodeproj
# Select an iPhone simulator and press Run in Xcode.

# Compile iOS Kotlin framework only
./gradlew :sample:embedAndSignAppleFrameworkForXcode
```

## Versioning

This library starts fresh at `0.1.0-alpha`. See [CHANGELOG.md](CHANGELOG.md) for release notes.

## License

Apache License 2.0 — see [LICENSE](LICENSE).

Based on [Android Week View](https://github.com/alamkanak/Android-Week-View) (Copyright 2014 Raquib-ul-Alam).
