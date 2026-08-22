# XML vs Compose parity

Comparison between [tlozovyi/Android-Week-View](https://github.com/tlozovyi/Android-Week-View) (View/XML) and this Compose library.

Last updated for **1.0.0-rc1**.

## Feature matrix

| Feature | View (XML) | Compose |
|---------|:----------:|:-------:|
| Timed event chips | ✅ | ✅ |
| All-day events | ✅ | ✅ |
| Blocked time ranges | ✅ | ✅ |
| Adaptive event text | ✅ | ✅ |
| Empty-slot tap / long-press | ✅ | ✅ |
| Event long-press callback | ✅ | ✅ |
| Drag-and-drop | ✅ | ✅ |
| Horizontal scroll + page snap | ✅ | ✅ |
| Programmatic scroll | ✅ | ✅ |
| Pinch-to-zoom | ✅ | ✅ |
| Weekend background colors | ✅ | ✅ |
| Week number badge | ✅ | ✅ |
| Header bottom line / shadow | ✅ | ✅ |
| Fill patterns (lined / dotted) | ✅ | ✅ |
| Multi-day corner flattening | ✅ | ✅ |
| Custom fonts (`fontFamily`) | ✅ | ✅ |
| Adapter / paging API | ✅ | ✅ |
| Custom composable content (events, headers, …) | ❌ | ❌ |
| Emoji in titles | ✅ | ❌ |
| RTL layout | ✅ | ✅ |
| Accessibility | ✅ | ❌ |
| Fling physics | ✅ | ❌ |

## Compose-only notes

- **Paging:** Compose uses **`WeekViewPagingState`** instead of subclassing **`PagingAdapter`**. Use **`onLoadMore(start, end, submit)`** for async loads, or the suspend overload that returns `List<WeekViewEvent>`. Call **`submit`** from the callback (or let the suspend overload submit for you). **`onRangeChanged`** mirrors View **`Adapter.onRangeChanged`**.
- **Fonts:** Compose uses `FontFamily` on `WeekViewStyle` instead of XML `fontFamily` / `typeface` / `textStyle` attrs. Header labels use `headerFontWeight` (default medium) when a family is set.
- **Week number:** ISO-8601 week of the first visible date; shown when `showWeekNumber = true` and `numberOfVisibleDays > 1`.
- **Header shadow:** Gradient approximation of View `Paint.setShadowLayer` (no hardware layer).
- **Patterns:** `WeekViewFillPattern.Lined` / `WeekViewFillPattern.Dotted` on `WeekViewEventStyle`; RTL flips lined direction like the View library; patterns are clipped to rounded chip bounds.

## Paging API mapping

| View (XML) | Compose |
|------------|---------|
| `PagingAdapter.onLoadMore` + `submitList()` | `rememberWeekViewPagingState { start, end, submit -> … }` |
| `PagingAdapter.refresh()` | `pagingState.refresh()` |
| `Adapter.onRangeChanged` | `onRangeChanged` on `rememberWeekViewPagingState` |
| `SimpleAdapter` / static list | `WeekView(events = …)` |

## Remaining gaps (1.0.0 candidates)

- Custom `@Composable` content slots for events, date headers, time labels, all-day chips, and related UI (post-1.0.0 / 1.1.x)
- Emoji rendering in chip text
- TalkBack / accessibility actions
- Fling deceleration matching View scroller
