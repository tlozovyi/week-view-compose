# Changelog

## 0.6.0-alpha

Pinch-to-zoom for hour height.

### Added

- Pinch-to-zoom on the day grid to adjust hour row height
- `minHourHeightDp`, `maxHourHeightDp`, and `pinchToZoomEnabled` on `WeekViewStyle`
- Viewport-aware minimum hour height (cannot pinch out past the configured hour range filling the grid area)
- Vertical scroll position scales proportionally while zooming
- Pinch zoom anchors to the focal point under your fingers
- Unit tests for hour-height clamping and focal-point scroll math

### Not yet implemented

- Drag-and-drop event editing
- Accessibility

## 0.5.0-alpha

MVP milestone — all-day events in the header row.

### Added

- All-day event chips rendered in the header row below date labels
- Dynamic header height based on the maximum number of all-day events per day
- Vertical and horizontal all-day arrangement via `arrangeAllDayEventsVertically` on `WeekViewStyle`
- All-day styling knobs: `headerPaddingDp`, `allDayEventTextSizeSp`, `allDayEventPaddingVerticalDp`
- All-day expand/collapse when more than two events overlap on a day (`+N` label, toggle arrow in time column, animated header height)
- Click handling for all-day events in the header
- Sample all-day events (single-day and multi-day)
- Unit tests for all-day header layout and chip bounds

### Not yet implemented

- Drag-and-drop event editing
- Accessibility

## 0.4.0-alpha

Basic gesture and interaction support.

### Added

- `onEventClick` callback with hit-testing on event chips
- Continuous horizontal scrolling from anywhere on `WeekView` (header or grid)
- Swipe axis detection so vertical scrolling still works on the grid
- `horizontalScrollingEnabled` on `WeekViewStyle` (replaces discrete header paging)
- Unit tests for event hit-testing, horizontal scroll offset, and date rolling

### Not yet implemented

- Pinch-to-zoom hour height
- Drag-and-drop event editing
- All-day events in the header row
- Accessibility

## 0.3.0-alpha

Event chip rendering on the day grid.

### Added

- Timed event chips drawn on the scrollable day grid (rounded rects + title text)
- Overlapping event layout via ported `EventChipsFactory` / `WeekViewLayoutEngine`
- `WeekViewEventStyle` for per-event color and shape overrides
- Event styling properties on `WeekViewStyle` (colors, padding, corner radius, gaps)
- `EventChipBoundsCalculator` for grid-local chip positioning
- Auto scroll to current time on launch (`scrollToCurrentTimeOnLaunch`, default `true`)
- Public `WeekViewLayoutEngine`, `EventChip`, `ChipBounds`, and entity types in `common`
- Sample events demonstrating horizontal overlap
- Unit tests for event chip bounds calculation

### Not yet implemented

- All-day events in the header row
- Horizontal scrolling / paging between date ranges
- Gestures, drag-and-drop, click handling
- Accessibility

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
