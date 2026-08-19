# Domain Model and Gameplay Reference

This guide explains the shared game model, rule engine, CPU strategy, match state machine, scoring, streaks, timers, achievements, and history semantics. These rules live in shared Kotlin code so Android and desktop use the same behavior.

## Domain ownership

The main files are:

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt` — immutable game/settings/stat data and enums;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/RulesEngine.kt` — winner/counter rules;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/CpuStrategy.kt` — deterministic seeded opponent behavior;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt` — match orchestration, navigation, score/stat/history updates, timers, backup/reset state;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt` — user interaction and rendering, not authoritative game rules.

A new rule should normally enter the model/engine/state layers before the UI.

## Gestures

`Gesture` contains five canonical values:

| Enum | Emoji | Canonical label |
|---|---|---|
| `ROCK` | 🪨 | Rock |
| `PAPER` | 📄 | Paper |
| `SCISSORS` | ✂️ | Scissors |
| `LIZARD` | 🦎 | Lizard |
| `SPOCK` | 🖖 | Spock |

The `label` is canonical/internal English-compatible data used by persisted history and domain matching. Visible localized labels are supplied by `ArenaStrings` rather than mutating enum identity.

### Variant filtering

```kotlin
Gesture.availableFor(GameVariant.CLASSIC)
```

returns only Rock, Paper, and Scissors.

```kotlin
Gesture.availableFor(GameVariant.LIZARD_SPOCK)
```

returns all five values.

Every CPU choice must be filtered through this variant-specific set.

## Game variants

`GameVariant`:

- `CLASSIC` — Rock/Paper/Scissors only;
- `LIZARD_SPOCK` — five-gesture extended rules.

The current `ArenaSettings.extendedVariant` field is persisted as part of settings compatibility, while the active match uses `MatchConfig.variant` as the immediate source of truth.

## Opponent modes

`OpponentMode`:

- `CPU` — Player 1 chooses; CPU selection is generated immediately;
- `LOCAL_TWO_PLAYER` — pass-and-play on one device; Player 1's move is held temporarily until Player 2 selects.

Private-room transport contracts exist separately and are not yet wired as a production network opponent mode. See `docs/private-room-protocol.md`.

## CPU difficulty

`Difficulty`:

- `EASY`;
- `NORMAL`;
- `EXPERT`.

Difficulty changes strategy probabilities; it does not alter game rules.

## Match modes

`MatchMode`:

- `BEST_OF_3`;
- `BEST_OF_5`;
- `ENDLESS`;
- `STREAK`;
- `TOURNAMENT`.

### Win targets

`MatchConfig.roundsToWin` returns:

| Mode | Wins needed by one side |
|---|---:|
| Best of 3 | 2 |
| Best of 5 | 3 |
| Tournament | 5 |
| Endless | no finite target |
| Streak | no finite target |

A draw does not advance either player's win count.

For finite modes, `ArenaState` marks the match `finished` once either player's score reaches the target.

`ENDLESS` and `STREAK` have `roundsToWin == null`, so they remain playable until the user resets/reconfigures the match.

## Round outcomes

`RoundOutcome`:

- `PLAYER_ONE_WIN`;
- `PLAYER_TWO_WIN`;
- `DRAW`.

Outcome identifies the score result. It does not explain **why** the round ended.

## Round end reasons

`RoundEndReason`:

- `PLAYED` — both gestures exist and the rule engine resolved them;
- `PLAYER_ONE_TIMEOUT` — Player 1 failed the timed turn;
- `PLAYER_TWO_TIMEOUT` — Player 2 failed the timed turn.

This separation prevents timeout records from inventing fake gestures.

## `MatchConfig`

Defaults:

```text
variant            CLASSIC
opponentMode       CPU
difficulty         NORMAL
matchMode          BEST_OF_3
seed               20260819
roundTimerSeconds  0
```

### Timer validation

Allowed timer values:

```text
0, 5, 10, 20, 30, 60
```

`0` means disabled.

`MatchConfig` validates the timer in its `init` block. Unsupported values such as `15` throw an `IllegalArgumentException` instead of creating an invalid match configuration.

The UI uses the same `ALLOWED_TIMER_SECONDS` set, reducing duplicated configuration.

## `RoundRecord`

Fields:

- `playerOne: Gesture?`;
- `playerTwo: Gesture?`;
- `outcome: RoundOutcome`;
- `endReason: RoundEndReason`.

Gestures are nullable because a timeout can end a round before one or both gestures exist.

For `PLAYED`, state code creates the record with both gestures non-null.

## `MatchSnapshot`

A match snapshot contains:

- the active `MatchConfig`;
- ordered `rounds`;
- Player 1 score;
- Player 2 score;
- draw count;
- `finished` flag.

`ArenaState` replaces this immutable data class through Compose state rather than mutating score fields in place.

## Rule matrix

`RulesEngine` stores a map from each gesture to the gestures it defeats:

- Rock defeats Scissors and Lizard.
- Paper defeats Rock and Spock.
- Scissors defeats Paper and Lizard.
- Lizard defeats Spock and Paper.
- Spock defeats Scissors and Rock.

### Resolution algorithm

Given Player 1 gesture `a` and Player 2 gesture `b`:

1. if `a == b`, result is draw;
2. if `b` is in the set defeated by `a`, Player 1 wins;
3. otherwise Player 2 wins.

This works for both classic and extended play because classic mode restricts which gestures can be selected before resolution.

## Counter lookup

`RulesEngine.countersFor(gesture)` finds every gesture whose defeat set includes the supplied gesture.

Examples:

- counters for Rock are Paper and Spock;
- counters for Paper are Scissors and Lizard.

The CPU strategy filters this result against the active variant, so Classic CPU never selects Lizard/Spock.

## CPU determinism

`CpuStrategy(seed)` creates:

```kotlin
private val random = Random(seed)
```

This means pseudo-random choices are deterministic for a given seed and identical sequence of calls.

A replayable CPU challenge therefore requires all of these to match:

- seed;
- difficulty;
- variant;
- player move history/call sequence.

Changing one changes the future random/strategic sequence.

## Easy CPU

Easy always chooses a random gesture from the allowed variant set.

It does not inspect player history.

## Normal CPU

Normal receives the current player's prior gestures.

Behavior:

1. if fewer than 3 historical gestures exist, choose randomly;
2. otherwise generate `random.nextInt(100)`;
3. if the number is below 55, choose randomly;
4. otherwise inspect the player's **last** gesture;
5. find allowed counters for it;
6. choose randomly among those counters.

Therefore Normal remains probabilistic and intentionally does not counter every move.

## Expert CPU

Expert behavior:

1. if fewer than 5 historical gestures exist, choose randomly;
2. otherwise, if a random 0-99 value is below 20, choose randomly;
3. filter history to gestures allowed by the current variant;
4. count frequency of each gesture;
5. predict the most frequent gesture;
6. find allowed counters to the predicted gesture;
7. choose randomly among those counters.

This is a simple local statistical strategy, not machine learning and not an online model.

Tie behavior for most-frequent values follows Kotlin collection iteration/count selection behavior; do not present it as a sophisticated probabilistic prediction model.

## `ArenaState` as orchestration layer

`ArenaState` owns runtime product state visible to Compose:

- current screen;
- settings;
- statistics;
- active configuration;
- current match snapshot;
- pending Player 1 move for pass-and-play;
- backup text;
- data-operation feedback;
- latest round/turn announcement.

The public mutable properties use `private set`, so external UI code reads them but changes behavior through explicit state functions.

## Navigation state

`ArenaScreen` contains:

- HOME;
- PLAY;
- HISTORY;
- STATS;
- ACHIEVEMENTS;
- SETTINGS;
- ABOUT.

`navigate()` changes the screen and clears stale data-operation feedback.

This is an in-memory screen state, not a platform navigation back stack.

## Onboarding

`completeOnboarding()` persists settings with `onboardingComplete = true`.

At app startup, `RpsArenaApp` chooses onboarding or main scaffold from persisted settings.

## Configuration changes reset the match

`updateConfig(value)`:

1. replaces current config;
2. calls `resetMatch()`.

`resetMatch()`:

- rebuilds CPU strategy with the current seed;
- creates an empty `MatchSnapshot`;
- clears any pending Player 1 local move;
- clears latest round announcement.

This avoids mixing rounds from different rule/mode/seed configurations.

## CPU play flow

When `OpponentMode.CPU`:

1. collect previous non-null Player 1 gestures from current match rounds;
2. call `CpuStrategy.choose()`;
3. resolve Player 1 vs CPU through `RulesEngine.resolve()`;
4. record the played round;
5. update scores, finish state, statistics, history, and announcement.

Only **prior** moves are supplied to CPU history; the current selected gesture is not included before CPU chooses.

## Local two-player flow

First tap by Player 1:

- stores the gesture in `pendingPlayerOne`;
- does not create a round yet;
- shows a pass-device instruction.

Second tap by Player 2:

- reads pending Player 1 gesture;
- clears pending state;
- resolves the two gestures;
- records the round.

The Player 1 move is not rendered in the normal result area until Player 2 has made a choice, reducing accidental pass-and-play disclosure.

## Timer state and UI coroutine

The visible countdown is managed by a Compose `LaunchedEffect` in `App.kt` keyed to:

- timer duration;
- number of completed match rounds;
- pending local Player 1 state;
- match finished state.

When those keys represent a new turn, the timer starts at the configured seconds and delays one second per decrement.

When it reaches zero, UI calls `state.expireCurrentTurn()`.

The **authority for scoring a timeout** is in `ArenaState`; the UI only supplies the timing trigger.

## Timeout scoring

### CPU mode

If timer is enabled and current turn expires:

- Player 1 gesture is null;
- Player 2 gesture is null;
- outcome = Player 2 win;
- reason = Player 1 timeout.

### Local two-player before Player 1 chooses

- outcome = Player 2 win;
- reason = Player 1 timeout.

### Local two-player after Player 1 chooses

The pending Player 1 gesture is retained in the record:

- Player 1 gesture = locked move;
- Player 2 gesture = null;
- outcome = Player 1 win;
- reason = Player 2 timeout.

Then `pendingPlayerOne` is cleared.

## Round recording

`recordResolvedRound()` calculates:

- incremented Player 1 score when applicable;
- incremented Player 2 score when applicable;
- draw count;
- whether the finite target has been reached;
- appended immutable `RoundRecord`.

Then it:

1. updates aggregate statistics;
2. creates a canonical history line;
3. persists history;
4. exposes that line as the latest announcement.

This centralized path is why gesture-played and timeout rounds share score/stat/history behavior.

## Statistics

`ArenaStats` fields:

- `roundsPlayed`;
- `wins` — Player 1 wins;
- `losses` — Player 1 losses / Player 2 wins;
- `draws`;
- `bestStreak`;
- `currentStreak`.

### Win rate

```text
wins * 100 / roundsPlayed
```

using integer division.

If no rounds have been played, win rate is `0` to avoid division by zero.

This is an integer percentage; it intentionally does not retain decimal fractions.

## Streak behavior

After a Player 1 win:

```text
currentStreak = previous currentStreak + 1
```

After a loss or draw:

```text
currentStreak = 0
```

`bestStreak` is the maximum of the prior best and new current streak.

Therefore a draw breaks the Player 1 win streak in the current implementation.

## Recent trend

`ArenaRepository.loadRecentTrend(limit = 10)` inspects the newest stored history entries and counts suffixes:

- `Player 1 won` -> win;
- `Player 2 won` -> loss;
- `Draw` -> draw.

The default sample is the newest 10 entries, bounded by the repository maximum history of 30.

`ArenaTrend.sampleSize` is simply `wins + losses + draws`.

## Achievements

Runtime achievements are derived from current aggregate statistics rather than stored as separate unlock booleans.

Current IDs and requirements:

| ID | Requirement |
|---|---|
| `first_win` | at least 1 Player 1 win |
| `ten_rounds` | at least 10 rounds played |
| `streak_3` | best streak at least 3 |
| `streak_7` | best streak at least 7 |
| `century` | at least 100 rounds played |

`ArenaState` currently constructs `Achievement` with canonical English title/description, while visible UI replaces copy through `achievementCopy(id, language)`.

When adding an achievement, update both the state requirement and all shipped localization catalogs/tests.

## Canonical history format

Played round:

```text
Rock vs Scissors — Player 1 won
```

Possible outcome suffixes:

```text
Player 1 won
Player 2 won
Draw
```

Timeout examples:

```text
Player 1 timed out — Player 2 won
Player 2 timed out — Player 1 won
```

The UI recognizes these canonical forms and can render localized gesture/outcome text without rewriting stored data.

Changing canonical history grammar is a persistence compatibility change because trends/localization currently parse known string structure/suffixes.

## Match completion guards

For finite modes, `play()` and `expireCurrentTurn()` return early after `match.finished`.

`ENDLESS` and `STREAK` are explicitly exempt because they have no finite target.

The UI displays a New Match button when `match.finished` is true.

## Settings used by gameplay/UI

`ArenaSettings` includes:

- theme controls;
- reduced motion;
- sound preference;
- haptics preference;
- extended-variant compatibility field;
- onboarding completion;
- local player name;
- interface language.

Sound/haptics are currently persisted preferences; there is not yet a platform effect engine tied to those flags. Documentation should not claim that audible/tactile effects are already emitted merely because settings exist.

## Adding a new gesture

This is a major rules change. At minimum update:

1. `Gesture` enum and variant availability;
2. `RulesEngine.defeats` matrix;
3. every localization catalog;
4. UI layout assumptions;
5. CPU allowed/counter tests;
6. rules tests for both win directions and draws;
7. optional Rust mirror if parity is intended;
8. documentation/changelog;
9. persistence/history compatibility review.

Do not add an enum value without defining every matchup.

## Adding a new match mode

At minimum decide:

- finite or endless;
- `roundsToWin` behavior;
- UI label in every language;
- match-finished behavior;
- whether timer logic differs;
- achievement/stat implications;
- tests.

Then update `MatchMode`, `MatchConfig`, UI catalogs, state tests, and docs.

## Changing CPU behavior

CPU changes affect replayability. If an algorithm changes, the same historical seed may generate a different sequence even though seed plumbing remains deterministic.

Treat strategy changes as user-visible behavior:

- document thresholds/prediction change;
- update deterministic tests;
- update README/this guide;
- add changelog entry;
- avoid claiming cryptographic randomness or AI/ML unless the implementation actually provides it.

## Important invariants

The domain should maintain these invariants:

- a Classic round cannot contain Lizard/Spock from normal supported selection paths;
- a `PLAYED` round has both gestures;
- timeout reasons may have missing gestures;
- Player 1 score equals count of Player 1 winning rounds in a fresh match;
- Player 2 score equals count of Player 2 winning rounds;
- draw count equals draw rounds;
- finite match cannot accept normal new turns after finished;
- aggregate persisted rounds equal wins + losses + draws;
- current streak cannot exceed best streak;
- best streak cannot exceed total wins;
- CPU selection is always in the active variant's allowed set.

Tests in `shared/src/commonTest` protect the most important of these boundaries.
