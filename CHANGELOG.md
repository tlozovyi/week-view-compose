# Changelog

## 0.1.0-alpha

Initial bootstrap release.

### Added

- Kotlin Multiplatform project scaffold (`common`, `compose-ui`, `sample`)
- Ported calendar algorithms from [Android Week View](https://github.com/thellmund/Android-Week-View):
  - `Period` / `FetchRange` paging windows
  - Multi-day event splitting
  - Event chip collision layout (`EventChipsFactory`)
- `kotlinx-datetime` as the internal date/time model
- Skeleton `@Composable WeekView` placeholder
- Shared unit tests for period, date extensions, and event splitting
- Android and iOS sample app entry points

### Not yet implemented

- Calendar grid, time column, and event chip rendering
- Scrolling, gestures, drag-and-drop
- Paging adapter equivalent
- Accessibility
