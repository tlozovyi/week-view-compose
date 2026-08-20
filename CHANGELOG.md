# Changelog

## 0.2.0-alpha

Basic calendar grid rendering.

### Added

- Date header row with configurable `DateFormatter`
- Time column with configurable `TimeFormatter` (default 12-hour labels)
- Day grid with past/future/today background colors
- Hour and day separators
- Current-time line and dot on today
- Vertical scrolling through the hour range
- Expanded `WeekViewStyle` (colors, separator toggles, dimensions)
- `firstVisibleDate` parameter on `WeekView`
- Unit tests for formatters and layout calculation

### Not yet implemented

- Event chip rendering
- Horizontal scrolling / paging between date ranges
- Gestures, drag-and-drop
- Accessibility

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
