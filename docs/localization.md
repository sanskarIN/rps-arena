# Localization Architecture and Translation Guide

RPS Arena currently ships shared English and Hindi UI copy. Localization is implemented in Kotlin common code so Android and desktop render the same product language without duplicating string catalogs per platform.

This guide explains what is translated, what intentionally remains canonical/domain data, how language persistence works, how history/results are localized at render time, how achievements are keyed, and the exact steps/tests required to add another language safely.

## Files

Primary localization files:

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaStrings.kt` — core UI strings plus enum-keyed gesture/difficulty/match-mode labels;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/AchievementStrings.kt` — achievement title/description copy keyed by stable achievement ID;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt` — `AppLanguage` enum and canonical domain gesture labels;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt` — chooses catalog and performs known canonical history/data-message localization;
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaStringsTest.kt` — catalog invariants;
- `shared/src/commonTest/kotlin/in/sanskar/rpsarena/AchievementStringsTest.kt` — achievement localization/fallback coverage;
- `shared/src/desktopTest/kotlin/in/sanskar/rpsarena/RpsArenaUiTest.kt` — end-to-end UI smoke checks for language switching and Hindi gameplay/achievement copy.

## Language model

`AppLanguage` currently contains:

```kotlin
enum class AppLanguage { ENGLISH, HINDI }
```

The enum name is persisted in `settings_v2`.

That means renaming an enum constant is a persistence compatibility change. For example, changing `HINDI` to `HI` would make existing stored `HINDI` settings fail current decoding unless migration logic is added.

Prefer adding a new enum value rather than renaming an existing persisted value.

## Selecting the active catalog

At application composition:

```kotlin
val strings = stringsFor(state.settings.language)
```

The selected `ArenaStrings` object is passed through screen Composables.

When `settings.language` changes through `state.updateSettings(...)`, Compose observes the changed settings state and recomposes with the new catalog.

No application restart is required for the core shared UI language switch.

## `ArenaStrings`

`ArenaStrings` is a data class containing core visible copy, including:

- application/settings/navigation labels;
- onboarding content;
- play configuration labels;
- timer/seed copy;
- result/timeout copy;
- history/stat labels;
- accessibility/settings copy;
- backup/reset messages;
- About labels;
- enum-specific display maps.

Using a typed data class gives compile-time pressure: when a new required constructor property is added, every language branch in `stringsFor()` must supply a value.

This is preferable to scattered string map lookups that can silently return null/missing copy.

## Enum-keyed display labels

`ArenaStrings` keeps private maps for:

- `Gesture -> String`;
- `Difficulty -> String`;
- `MatchMode -> String`.

Public helpers:

```kotlin
strings.gestureLabel(gesture)
strings.difficultyLabel(difficulty)
strings.modeLabel(mode)
```

Why enum keys matter:

- domain identity is stable regardless of display language;
- code does not compare translated text to determine behavior;
- adding a translation cannot change rule resolution;
- UI tests can verify every enum value is covered.

Never use a translated label as a persistence key or game-rule identifier.

## Canonical gesture labels vs visible labels

`Gesture` also has a canonical `label` such as `Rock` or `Scissors`.

That canonical label is currently used by the history serialization grammar and by the parser that recognizes existing round summaries.

The visible UI uses `strings.gestureLabel(...)`.

Example stored line:

```text
Rock vs Scissors — Player 1 won
```

Possible Hindi rendering:

```text
पत्थर vs कैंची — खिलाड़ी 1 ने राउंड जीता
```

The stored line remains canonical. Switching language does not rewrite history.

Benefits:

- one stored history record can be rendered in different languages;
- changing the active UI language does not duplicate/migrate old records;
- trend parsing remains language independent under the current canonical grammar.

## Round history localization

`App.kt` contains `localizeRoundAnnouncement(raw, strings)`.

Recognized cases:

1. canonical timeout messages;
2. played-round grammar split around `" — "`;
3. gesture pair split around `" vs "`;
4. canonical gesture labels mapped back to `Gesture` enum;
5. canonical outcome suffix mapped to localized result copy.

If the string does not match the recognized canonical form, it is returned unchanged rather than guessed.

This fallback is deliberate: unexpected persisted text should remain visible, not disappear because localization parsing failed.

### Compatibility warning

Changing any of these canonical tokens affects localization parsing:

```text
" — "
" vs "
"Player 1 won"
"Player 2 won"
"Draw"
```

If history grammar changes, update old-format parsing/migration and tests before release.

## Data-operation message localization

Repository/state logic returns canonical machine-recognizable English messages such as:

```text
Backup is too large
Malformed backup record
Duplicate stats record
```

`App.kt` maps known messages through `localizeDataMessage(...)`.

This keeps repository validation code independent from UI language while still presenting translated feedback.

A future refactor could replace message-string matching with typed error/status codes. Until then, changing repository message text is also a UI localization compatibility change and requires updating `localizeDataMessage()` and tests.

## Achievement localization

`ArenaState` derives achievements using stable IDs:

```text
first_win
ten_rounds
streak_3
streak_7
century
```

`achievementCopy(id, language)` maps each ID to title/description.

Why stable IDs are important:

- unlock requirement is separate from presentation;
- titles can change/translate without changing identity;
- future persistence/analytics-free internal references can use IDs;
- unknown IDs have fallback copy instead of crashing.

When adding an achievement, update:

1. `ArenaState.achievements` requirement;
2. English `achievementCopy` branch;
3. Hindi branch;
4. every future language branch;
5. `AchievementStringsTest` known-ID list;
6. relevant UI tests/docs.

## English catalog role

English is both:

- a shipped UI language;
- a useful canonical reference for domain/history grammar.

`ArenaStringsTest` checks that English visible gesture labels match `Gesture.label` for current values.

Do not assume every future English phrase must become a persistence identifier. Prefer stable enums/IDs for new features.

## Hindi catalog coverage

Current Hindi catalog covers core product flows including:

- onboarding;
- Home/Play/Stats/History/Achievements/Settings/About navigation;
- opponent/rules labels;
- Easy/Normal/Expert;
- match modes;
- timer labels;
- seed helper/action;
- five gesture names;
- local turn instructions;
- timeout/result copy;
- score/stat labels;
- theme/accessibility/settings labels;
- player-name controls;
- backup/import/reset controls;
- validation/import feedback;
- About version/license labels;
- achievement names/descriptions.

Brand/contact strings such as `RPS Arena`, email addresses, GitHub path, `MIT`, and `Made by the Sanskar` are intentionally not translated as ordinary prose.

## Remaining direct/canonical UI text

Some strings in `App.kt` are intentionally technical/contact values rather than language catalog copy, for example:

- backup schema label `RPS_ARENA_BACKUP|1`;
- `Business:`/`Support:`/`GitHub:` contact lines;
- version value;
- license value;
- URLs/domains.

There is also a canonical state announcement for the first local two-player selection:

```text
Player 1 move locked. Pass the device to Player 2.
```

The normal turn header is localized, but this state-generated announcement is currently canonical English unless explicitly mapped. When extending localization completeness, prefer replacing state message strings with typed events/status or adding catalog-backed rendering rather than moving UI strings into domain code.

Documenting this boundary avoids falsely claiming that every runtime diagnostic/technical string is translated.

## Adding another language

Suppose a future language `SPANISH` is added.

### Step 1: add enum value

```kotlin
enum class AppLanguage { ENGLISH, HINDI, SPANISH }
```

Because the enum name is persisted, choose a stable name and do not casually rename it later.

### Step 2: extend `stringsFor()`

Add a complete `AppLanguage.SPANISH -> ArenaStrings(...)` branch.

Do not leave placeholder English text silently unless the translation is intentionally documented as fallback behavior.

### Step 3: add enum label maps

Every new catalog must cover all current:

- `Gesture.entries`;
- `Difficulty.entries`;
- `MatchMode.entries`.

The helpers use `getValue()`, so missing keys are programmer errors rather than silent empty labels.

### Step 4: extend achievement copy

Add the new language branch and translate every known stable achievement ID plus fallback copy.

### Step 5: expose language selection

Add a language-selection control using the catalog's own language display naming policy.

Avoid layouts that assume only two language chips; use responsive/wrapping behavior as language count grows.

### Step 6: add tests

At minimum verify:

- every gesture has nonblank localized label;
- every difficulty has label;
- every match mode has label;
- every achievement known ID has title/description;
- settings can select/persist/reload language;
- primary gameplay renders the new language;
- backup round trip preserves the new enum name;
- legacy English/Hindi records still decode.

### Step 7: manual review

Check:

- long text at narrow Android width;
- text scaling;
- button/chip wrapping;
- RTL behavior if applicable;
- punctuation and numeral conventions;
- accessibility pronunciation/semantics;
- font glyph availability;
- translated destructive warnings;
- no truncation in result/history cards.

## Right-to-left languages

The Android manifest sets `android:supportsRtl="true"`, but a real RTL language still requires product-level testing.

Before shipping RTL:

- avoid hard-coded left/right assumptions where start/end semantics are intended;
- verify Compose layout mirroring;
- verify arrow/back affordances;
- test mixed Latin technical strings/URLs;
- test history delimiter appearance;
- check desktop behavior separately.

`supportsRtl` alone is not proof of complete RTL readiness.

## Translation quality rules

- Translate meaning, not variable names literally.
- Keep `RPS Arena` branding stable unless branding policy changes.
- Preserve technical schema/version identifiers exactly.
- Do not translate email addresses or URLs.
- Do not translate stable enum/achievement IDs in source code.
- Keep destructive/reset/security warnings unambiguous.
- Avoid gender/appearance assumptions unrelated to gameplay.
- Prefer short control labels while keeping help text explanatory.

## Localization and persistence

Settings backup serializes `language.name`, for example:

```text
ENGLISH
HINDI
```

Import accepts only enum names present in the running build.

If a language is removed from a future version, old backups containing that language would fail settings decoding unless compatibility behavior is explicitly added. Therefore removing a shipped language is a schema-compatibility decision, not only a UI deletion.

## Localization and version metadata

Application version is deliberately stored in:

```kotlin
const val APP_VERSION = "1.1.0"
```

About renders:

```kotlin
Text("${strings.version}: $APP_VERSION")
```

This prevents `scripts/check_version.py` from depending on the English literal `Version:`. The script checks shared metadata separately and verifies that About references `$APP_VERSION`.

Do not reintroduce a hard-coded localized version number into each catalog.

## Testing expectations

### `ArenaStringsTest.kt`

Protects:

- English canonical gesture labels;
- Hindi labels differ for gesture/difficulty/match-mode sets;
- shared semantic version shape.

### `AchievementStringsTest.kt`

Protects:

- every known achievement has distinct Hindi copy;
- title/description are nonblank;
- unknown ID fallback is nonblank.

### `RpsArenaUiTest.kt`

Protects visible flows including:

- switch English -> Hindi;
- Hindi settings headings;
- Hindi Rock/Paper/Scissors labels;
- Hindi first-achievement title/description.

Unit catalog tests and UI tests solve different problems; keep both.

## What to update when visible copy changes

For core product copy review:

- all language catalogs;
- screenshots/release notes if wording is visible there;
- UI tests that intentionally assert the changed text;
- accessibility review for label clarity;
- support/troubleshooting docs if instructions quote the UI;
- `CHANGELOG.md` only when the wording change is meaningfully user-visible/release-relevant.

## Future improvement: typed UI messages

The current code contains some canonical string-to-localized-message mapping. A stronger future architecture can define typed status/error values such as:

```text
BackupStatus.Ready
BackupError.TooLarge
BackupError.MalformedRecord
LocalTurnStatus.PlayerOneLocked
```

Then repository/state can expose types and the UI can translate them without parsing English text.

If implementing this:

- preserve backup/persistence behavior;
- add exhaustive `when` mappings;
- migrate tests from message literals to typed status assertions where appropriate;
- retain readable internal/debug descriptions separately;
- update this guide and architecture docs.
