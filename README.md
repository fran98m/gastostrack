# Gastos Mamá

Android app (Kotlin + Jetpack Compose, Room) for logging expenses paid on
someone else's behalf and claiming them by WhatsApp at month's end. Built
for a single device — **Pixel 7 only** (minSdk 33, fixed 412×915dp layout).

Design source: the Claude Design canvas `Gastos Mamá.dc.html` and its
handoff notes (`design_handoff_gastos_mama/README.md` in that project) —
screens 1a (interactive prototype) for behavior, 1c ("Libreta") for the
shipped visual treatment.

## Running it

Open the project root in Android Studio and run the `app` config on a
Pixel 7 device or emulator — the Gradle wrapper handles the rest.

From the command line:

```
./gradlew installDebug   # build + install on a connected device/emulator
./gradlew testDebugUnitTest
```

## Project layout

```
app/src/main/kotlin/com/franm/gastosmama/
  data/            Room entities, DAO, database, ExpenseRepository
  data/icons/      CategoryIcons.kt — the category → icon map (see below)
  ui/              AppViewModel, navigation, theme tokens, one file per screen
  util/            money/date formatting, keypad rules, CSV + share helpers
app/src/main/res/
  drawable/        ic_cat_*.xml — generated category icons (see below)
  font/            Archivo (Google Fonts, variable font + weight instances)
tools/
  svg_to_vector.py Converts an SVG into an Android VectorDrawable
```

## Adding a new category icon

This is deliberately a two-step, no-XML-by-hand workflow:

1. **Get an SVG.** Export it from Claude Design, or grab one from
   [lucide.dev](https://lucide.dev) (the seed icons all come from Lucide).
   It needs to be a simple stroke-style icon — single color, `<path>` and/or
   `<circle>` elements, `fill="none"`. That covers effectively all Lucide
   icons and most simple line-art SVGs.

2. **Run the converter:**

   ```
   python3 tools/svg_to_vector.py <path-or-url-to-icon.svg> <name>
   ```

   Examples:

   ```
   python3 tools/svg_to_vector.py https://unpkg.com/lucide-static@latest/icons/wallet.svg wallet
   python3 tools/svg_to_vector.py ~/Downloads/exported-icon.svg ferreteria
   ```

   This writes `app/src/main/res/drawable/ic_cat_<name>.xml` — a plain
   VectorDrawable with a placeholder stroke color, which is fine: the app
   tints every category icon at draw time (`ColorFilter.tint(...)` /
   `Icon(tint = ...)`), so the color in the file is never actually used.

3. **Add one line to `CategoryIcons.kt`:**

   ```kotlin
   val SEED_CATEGORY_ICONS: List<Pair<String, Int>> = listOf(
       "Frutera" to R.drawable.ic_cat_apple,
       ...
       "Ferretería" to R.drawable.ic_cat_ferreteria,   // ← new line
   )
   ```

   The order in this list is only the *fallback* order for a fresh install —
   real usage counts take over and re-sort the Categoría screen descending
   by how often each category gets used (see
   `ExpenseRepository.categoryTiles`). Any label typed via "Otro…" that
   isn't in this list automatically falls back to the smile icon
   (`OTHER_ICON`) until you give it a real one.

That's it — no manual XML editing, no asset pipeline. Rebuild and the new
icon shows up in the category picker.

### If the SVG doesn't convert cleanly

The script only understands `<path>` and `<circle>` — if it errors out or
the icon looks wrong (gradients, multiple colors, `<rect>`/`<polygon>`
elements), just hand-write the VectorDrawable following the pattern in any
existing `ic_cat_*.xml` file, or simplify the source SVG first.

## Known WhatsApp quirk

The Resumen screen shares the CSV and the message in one `ACTION_SEND`
intent, targeted at WhatsApp with a wildcard MIME type (see
`ShareUtil.kt` for why — WhatsApp drops the caption on some MIME types
once a file is attached). If a future WhatsApp update changes this again,
the message is also copied to the clipboard as a fallback so it can be
pasted in manually.
