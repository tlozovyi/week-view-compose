# XML vs Compose parity

Comparison between [tlozovyi/Android-Week-View](https://github.com/tlozovyi/Android-Week-View) (View/XML) and this Compose library.

Last updated for **0.10.0-beta**.

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
| Adapter / paging API | ✅ | ❌ |
| Emoji in titles | ✅ | ❌ |
| RTL layout | ✅ | ❌ |
| Accessibility | ✅ | ❌ |
| Fling physics | ✅ | ❌ |

## Compose-only notes

- **Fonts:** Compose uses `FontFamily` on `WeekViewStyle` instead of XML `fontFamily` / `typeface` / `textStyle` attrs. Header labels use `headerFontWeight` (default medium) when a family is set.
- **Week number:** ISO-8601 week of the first visible date; shown when `showWeekNumber = true` and `numberOfVisibleDays > 1`.
- **Header shadow:** Gradient approximation of View `Paint.setShadowLayer` (no hardware layer).
- **Patterns:** `WeekViewFillPattern.Lined` / `WeekViewFillPattern.Dotted` on `WeekViewEventStyle`; RTL flips lined direction like the View library.

## Remaining gaps (1.0.0 candidates)

- RecyclerView-style adapter and paging
- Emoji rendering in chip text
- RTL mirroring (layout direction, scroll, patterns)
- TalkBack / accessibility actions
- Fling deceleration matching View scroller
