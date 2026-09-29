# Changelog

## 1.0.0-rc8

Gesture fixes for grid scroll and horizontal paging.

### Fixed

- **Long-press after scroll** — long-press and drag no longer fire when the user scrolls the grid and keeps their finger down without lifting.
- **Horizontal page snap** — paging to the next or previous range is easier and consistent in both directions; snap uses a short drag threshold relative to the visible page width.

## 1.0.0-rc7

Fixes spurious taps and horizontal scrolls when the system back gesture is used.

### Fixed

- **System back gesture** — touches starting in the OS system-gesture inset (`WindowInsets.systemGestures`) are consumed before [WeekView] scroll/tap handlers run, matching legacy View `WeekView` behavior where the framework cancels the stream.
- **Predictive back (Android)** — while a predictive back gesture is in progress, pointer input is consumed so horizontal paging and grid taps do not fire when the gesture is cancelled or completed.
- **Grid tap on cancelled pointers** — timed-event tap detection uses `changedToUp()` instead of treating any pointer release as a tap, so cancelled gestures (including back) no longer trigger `onEventClick` / `onEmptyViewClick`.

## 1.0.0-rc6

Grid tap location detection after pinch-to-zoom and vertical scroll.

### Fixed

- **Grid event hit-testing** — pointer Y on the timed grid canvas is already in content space (`weekViewGridScroll` applies scroll via layout placement); hit-testing no longer adds `gridScrollOffsetPx` a second time. Fixes wrong or empty-slot taps after zoom and when scrolled.
- **Pinch focal point after scroll** — pinch start again converts the centroid from content Y to viewport Y via `focalYInViewportPx`, so zoom scroll math stays aligned with the visible focal point (regression from 1.0.0-rc3 hardening).
- **Stale chip bounds** — timed event bounds are cleared before each recalculation (same as all-day chips).
- **Drag edge detection** — viewport Y from canvas coordinates uses the same integer scroll offset as layout placement.

## 1.0.0-rc5

Per-event chip text styles.

### Added

- **`WeekViewEventTextStyle`** on **`WeekViewEvent`** — optional **`titleTextStyle`** and **`subtitleTextStyle`** with combinable **`bold`**, **`italic`**, **`underline`**, and **`strikethrough`**
- Sample includes all 16 `WeekViewEventTextStyle` flag combinations as timed events on today (19:00–21:30), plus six all-day style examples on tomorrow (default, strikethrough, bold, italic+underline, all flags, mixed title/subtitle)

### Fixed

- **Grid event hit-testing** — chip bounds use the same Dp-snapped grid layout as drawing and gesture hit-tests (`resolveDisplayGridLayout`); vertical position uses `gridHeightPx` for consistent time mapping after pinch-to-zoom
- **Main-thread jank on launch** — chip bounds/visibility updates moved out of composition into `SideEffect`; scroll clamp and viewport height only update when values change; paging callback wiring moved to `SideEffect`
- **Sample paging mode** — uses suspend `rememberWeekViewPagingState` loader and a cached event catalog instead of rebuilding the full list synchronously on the main thread
- **Styled chip text ANR** — uniform `titleTextStyle`/`subtitleTextStyle` (e.g. strikethrough on both lines) now use the original plain-text measure path via `TextStyle`; `AnnotatedString` is only used when title and subtitle styles differ, with a trim-loop guard

## 1.0.0-rc4

Fixes paging events not appearing after async `submit`.

### Fixed

- **Paging async submit** — [WeekView] now observes `pagingState.eventsState`; the scoped `submit` callback publishes into that state after the controller cache updates, so async loaders show events without waiting for scroll settlement.
- **Paging load lifecycle** — suspend loader overload runs loads on a dedicated scope with `abandonInFlightLoads()` on failure/cancellation; in-flight period tracking simplified to avoid duplicate fetches on callback updates.

## 1.0.0-rc3

Fixes pinch-zoom persistence jumps and paging initial-load race.

### Fixed

- **Pinch-zoom persistence** — `onHourHeightChanged` now reports the same canonical `Dp` as [WeekViewStyle.hourHeightDp]; echoing that value back into style no longer re-syncs and jumps the grid. External style changes scale vertical scroll proportionally.
- **Pinch-zoom release jump** — style sync no longer re-runs when pinch ends (before the parent applies the callback); hour height is canonicalized to match layout snapping; pinch end reuses the last gesture frame instead of re-clamping.
- **Pinch-zoom restart jump** — pinch start now stores the pointer centroid in viewport space (not content space), so a second pinch after vertical scroll no longer jumps; scroll is not re-clamped at pinch begin.
- **Paging initial load** — removed pre-fetch `reserve()` that marked empty months as cached before `onLoadMore` ran; callbacks are wired during composition (not `SideEffect`); stale in-flight periods are cleared and retried when callbacks update.

## 1.0.0-rc2

Fixes for pinch-zoom persistence, paging initial load, grid tap hit-testing after zoom/scroll, and today header styling.

### Added

- **`onHourHeightChanged`** on **`WeekView`** — called when a pinch-to-zoom gesture ends; use to persist hour row height
- **`todayHeaderTextColor`** on **`WeekViewStyle`** — optional accent for today's date header (`null` → **`headerTextColor`**)
- **`WeekViewPagingState.ensureLoaded()`** — prefetch events for the visible month window without waiting for horizontal scroll settlement

### Fixed

- **Paging** — initial month fetch runs on first layout when the visible date range is known, not only after scroll snap
- **Grid taps after zoom/scroll** — hit-testing uses content coordinates (includes vertical scroll offset) and the same Dp-snapped grid layout as drawing; pinch end no longer triggers stray taps

## 1.0.0-rc1

RTL layout, month-based event paging and fixes for fill patterns.

### Added

#### Paging (View `PagingAdapter` equivalent)

- **`WeekViewPagingState`** and **`rememberWeekViewPagingState()`** — month-based cache; prefetches current month plus previous and next
- **`WeekViewPagingSubmit`** — `submit` callback passed into **`onLoadMore(startDate, endDate, submit)`** so async loaders do not need a state holder
- Suspend overload of **`rememberWeekViewPagingState`** — `onLoadMore` returns `List<WeekViewEvent>` and results are submitted automatically
- Optional **`pagingState`** parameter on **`WeekView`** (uses `pagingState.events` instead of `events`)
- **`WeekViewPagingController<T>`** in the `common` module for non-Compose integrations
- Optional **`onRangeChanged`** on `rememberWeekViewPagingState` (matches View `Adapter.onRangeChanged`)
- **`pagingState.refresh()`** clears the month cache and reloads the current window
- Sample **3 days · paging** mode
- Unit tests: `WeekViewPagingControllerTest`, `WeekViewPagingStateTest`

#### RTL

- Automatic RTL mirroring from **`LocalLayoutDirection`** (time column on trailing edge, reversed date columns)
- RTL-aware event chip bounds (`columnGap` inset), chip text alignment, and all-day expand labels
- RTL now-dot pinning on the grid edge adjacent to the time column
- RTL horizontal drag auto-scroll and page-snap math
- Sample **3 days · RTL** mode
- Unit tests in **`WeekViewRtlTest`**

### Fixed

- **Fill patterns** — diagonal hatch clipped to rounded chip bounds; extra lines for tall chips; dotted grid uses `ceil` so the bottom row is not clipped

### Changed

- **`events`** parameter on **`WeekView`** defaults to `emptyList()` when using **`pagingState`**

## 0.10.0-beta

Programmatic scroll API and visual polish matching the View library styling surface.

### Added

- **`WeekViewScrollState`** and **`rememberWeekViewScrollState()`** — suspend `scrollToDate`, `scrollToTime`, `scrollToDateTime`
- Optional **`scrollState`** parameter on `WeekView`
- Read properties: **`firstVisibleDate`**, **`gridScrollOffsetPx`**
- Optional **`minDate`** / **`maxDate`** on `WeekViewStyle` for scroll clamping
- 300ms animated scroll (instant when `animated = false`)
- Unit tests for scroll target math and date clamping
- Sample **Today** button using `scrollToDateTime`
- Weekend column backgrounds: **`pastWeekendBackgroundColor`**, **`futureWeekendBackgroundColor`**, **`weekendHeaderTextColor`**
- ISO week-number badge: **`showWeekNumber`**, **`weekNumberTextColor`**, **`weekNumberTextSizeSp`**, **`weekNumberBackgroundColor`**, **`weekNumberBackgroundCornerRadiusDp`**
- Header decorations: **`showHeaderBottomLine`**, **`headerBottomLineColor`**, **`headerBottomLineWidthDp`**, **`showHeaderBottomShadow`**, **`headerBottomShadowColor`**, **`headerBottomShadowRadiusDp`**
- Per-event fill patterns via **`WeekViewFillPattern.Lined`** / **`WeekViewFillPattern.Dotted`** on **`WeekViewEventStyle`**
- Multi-day timed event corner flattening (continued events square off top/bottom corners per day slice)
- Shared typography: **`fontFamily`**, **`headerFontWeight`**, **`headerTextSizeSp`**, **`timeColumnTextSizeSp`**
- Unit tests for weekend colors, week number, and pattern mapping
- Sample **7 days · snap** mode demonstrates week number, header line/shadow, weekend colors, and sans-serif font

## 0.9.0-beta

Event chip text parity, blocked time ranges, and grid interaction callbacks.

### Added

- **`adaptiveEventTextSize`** on `WeekViewStyle` — shrinks chip labels until they fit (ported from View `TextFitter`)
- **Subtitle rendering** on timed event chips (title + newline + subtitle) and all-day chips (title + space + subtitle)
- **`WeekViewBlockedTime`** — non-interactive blocked ranges on the day grid (full column width, drawn behind events)
- **`defaultBlockedTimeBackgroundColor`** / **`defaultBlockedTimeTextColor`** on `WeekViewStyle`
- **`onEmptyViewClick(time)`** and **`onEmptyViewLongClick(time)`** for empty grid taps (including over blocked time)
- **`onEventLongClick(event): Boolean`** — return `true` to consume long-press; return `false` to allow drag-and-drop
- Multi-line trimming before font shrink, matching View library behavior
- Unit tests for chip text composition, blocked-time bounds, and grid touch routing

### Changed

- Sample app enables adaptive text by default; demonstrates blocked lunch break, empty-slot callbacks, and event long-press
- Sample events include short overlapping slots and all-day subtitles to demonstrate text fitting
- Timed grid gestures use a unified tap / long-press handler (fixes taps blocked when drag is enabled)

### Fixed

- Transparent event backgrounds no longer turn black when drag dimming is applied

## 0.8.0-beta3

### Changed

- Migrate `kotlinx.datetime.Clock` / `Instant` usage to **`kotlin.time.Clock`** for **kotlinx-datetime 0.7.x** compatibility (fixes `NoClassDefFoundError: kotlinx/datetime/Clock$System` on Android apps using datetime 0.7.1).

### Dependencies

- `kotlinx-datetime` **0.7.1** (was 0.6.1).

## 0.8.0-beta

Horizontal scroll snapping aligned with the View library, with free-scroll mode and sample presets.

### Added

- Snap to nearest [numberOfVisibleDays] page when horizontal scrolling ends
- Snap to adjacent calendar week when `numberOfVisibleDays >= 7` (aligned to `firstDayOfWeek`)
- Animated spring snap after the finger lifts (controlled by `horizontalScrollSnapEnabled`)
- `horizontalScrollSnapEnabled` and `firstDayOfWeek` on `WeekViewStyle`
- Week-aligned initial `firstVisibleDate` when `numberOfVisibleDays >= 7`
- Sample app mode switcher (3-day snap, 7-day snap, 7-day free scroll, static week, and more)
- Unit tests for page snap targets, scroll normalization, and external date sync

### Changed

- Snap runs only on finger release, using an Android-style threshold (lower than half-page rounding)
- Horizontal scroll buffer reduced to 1 off-screen day column on each side
- `onFirstVisibleDateChange` is not called during drag; only after snap completes or on release in free-scroll mode

### Fixed

- Snap animation no longer jumps through buffer dates (viewport-based target resolution)
- Small scrolls from a page start no longer over-jump to the previous page
- Free scroll (`horizontalScrollSnapEnabled = false`) no longer snaps back on release
- Scrolling into the future is no longer blocked by viewport/buffer edge cases

## 0.7.0-alpha

Drag-and-drop event editing on the day grid.

### Added

- Long-press on a timed event chip to drag it to a new time slot
- `onEventDrop` callback with the event and its new start/end times
- `dragAndDropEnabled` on `WeekViewStyle` (default `true`; requires `onEventDrop` to activate)
- 15-minute snap increments while dragging
- Edge auto-scroll when dragging near the top, bottom, or horizontal edges of the grid
- Ghost chip preview with drag styling during the gesture
- Unit tests for quarter-hour snapping, time-from-point mapping, and drag auto-scroll math

### Not yet implemented

- Accessibility

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
