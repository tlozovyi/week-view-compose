# Week View Compose

Compose Multiplatform calendar week view for Android and iOS.

**Status:** `0.2.0-alpha` — date header, time column, and scrollable day grid.

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

# Compile iOS framework (requires macOS + Xcode)
./gradlew :compose-ui:compileKotlinIosSimulatorArm64
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
    ),
    style = WeekViewStyle(numberOfVisibleDays = 3, minHour = 7, maxHour = 22),
)
```

## Versioning

This library starts fresh at `0.1.0-alpha`. See [CHANGELOG.md](CHANGELOG.md) for release notes.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
