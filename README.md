# Week View Compose

Compose Multiplatform calendar week view for Android and iOS.

**Status:** `0.6.0-alpha` — calendar grid, event chips, tap handling, continuous horizontal scroll, all-day events, and pinch-to-zoom hour height.

## Acknowledgements

This project is a Compose Multiplatform reimplementation **inspired by** the excellent [Android Week View](https://github.com/thellmund/Android-Week-View) library:

- Original author: [Raquib-ul-Alam](https://github.com/raquib-ul-alam) (2014)
- Maintainer: [Till Hellmund](https://github.com/thellmund)

Calendar layout concepts and algorithms from that project are ported to Kotlin Multiplatform with `kotlinx-datetime`. This is not a drop-in replacement for the View-based library yet.

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
./gradlew :common:cleanAllTests :common:allTests

# Build Android sample
./gradlew :sample:assembleDebug

# Run iOS sample (requires macOS + Xcode 15+)
open iosApp/iosApp.xcodeproj
# Select an iPhone simulator and press Run in Xcode.
# Or from the command line:
# xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16' build

# Compile iOS Kotlin framework only
./gradlew :sample:embedAndSignAppleFrameworkForXcode
```

## Usage (alpha)

```kotlin
WeekView(
    events = listOf(
        WeekViewEvent(
            id = 1,
            title = "Meeting",
            startTime = LocalDate(2026, 8, 20).atTime(9, 0),
            endTime = LocalDate(2026, 8, 20).atTime(10, 0),
        ),
        WeekViewEvent(
            id = 2,
            title = "Holiday",
            startTime = LocalDate(2026, 8, 20).atTime(0, 0),
            endTime = LocalDate(2026, 8, 20).atTime(23, 59),
            isAllDay = true,
        ),
    ),
    style = WeekViewStyle(numberOfVisibleDays = 3, minHour = 7, maxHour = 22),
    onFirstVisibleDateChange = { firstVisibleDate = it },
    onEventClick = { event -> /* handle tap */ },
)
```

All-day events (`isAllDay = true`) render as chips in the header row below the date labels. When a day has more than two all-day events, the header collapses to one chip plus a `+N` label; tap the arrow in the time column to expand or collapse.

Pinch with two fingers on the day grid to zoom hour row height in and out. Limits are controlled by `minHourHeightDp` and `maxHourHeightDp` on `WeekViewStyle`.

## Versioning

This library starts fresh at `0.1.0-alpha`. See [CHANGELOG.md](CHANGELOG.md) for release notes.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
