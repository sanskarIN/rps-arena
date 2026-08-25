# Domain Model and Gameplay Reference

This guide documents the **current reconciled v2.5.8** shared gameplay model, rule engine, CPU strategy, match state, scoring, achievements, persistence interaction, and known deferred behavior.

## Domain ownership

Primary files:

- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/model/GameModels.kt` — enums and immutable state models;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/RulesEngine.kt` — winner and counter rules;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/engine/CpuStrategy.kt` — deterministic seeded CPU selection;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/state/ArenaState.kt` — navigation, match orchestration, statistics/history updates and persistence coordination;
- `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/App.kt` — Compose presentation and user interaction, not authoritative game rules.

Visible UI copy comes from Compose Multiplatform resources. The old pre-reconciliation `ArenaStrings` model is not part of the current runtime; see [`LOCALIZATION.md`](LOCALIZATION.md).

## Gestures and variants

`Gesture` contains:

| Enum | Emoji | Canonical label |
|---|---|---|
| `ROCK` | 🪨 | Rock |
| `PAPER` | 📄 | Paper |
| `SCISSORS` | ✂️ | Scissors |
| `LIZARD` | 🦎 | Lizard |
| `SPOCK` | 🖖 | Spock |

`Gesture.availableFor(variant)` is the authoritative allowed-set filter:

- `CLASSIC` -> Rock, Paper, Scissors;
- `LIZARD_SPOCK` -> all five gestures.

`ArenaState.play()` rejects a gesture that is not valid for the active variant before changing match/stat/history state.

Canonical English `Gesture.label` values are still used in human-readable persisted history. Visible gesture labels are localized through Compose resources.

## Rule engine

`RulesEngine` stores the defeat relation:

- Rock defeats Scissors and Lizard;
- Paper defeats Rock and Spock;
- Scissors defeats Paper and Lizard;
- Lizard defeats Spock and Paper;
- Spock defeats Scissors and Rock.

`resolve(playerOne, playerTwo)` returns:

- `DRAW` when gestures match;
- `PLAYER_ONE_WIN` when Player 1's gesture defeats Player 2's;
- otherwise `PLAYER_TWO_WIN`.

`countersFor(gesture)` derives all gestures that defeat the supplied gesture and is used by adaptive CPU strategies.

## Opponent modes

`OpponentMode` contains:

- `CPU`;
- `LOCAL_TWO_PLAYER`.

### CPU

Player 1 chooses a valid gesture. `CpuStrategy` receives the current difficulty, game variant, and prior Player-1 move history. A completed round is recorded immediately.

### Local two-player

The first selection is held in `pendingPlayerOne`. The second selection completes the round. The first move is not exposed by gameplay state as a completed round until Player 2 chooses.

Private-room contracts are separate from `OpponentMode` and are not wired as production network gameplay. See [`private-room-protocol.md`](private-room-protocol.md).

## CPU difficulty

`CpuStrategy` owns a Kotlin `Random` created from `MatchConfig.seed`.

### Easy

Always selects uniformly from gestures valid for the active variant.

### Normal

- with fewer than 3 Player-1 moves: random valid gesture;
- otherwise: 55% random path;
- adaptive path: counters the player's most recent move using only counters valid in the current variant.

### Expert

- with fewer than 5 Player-1 moves: random valid gesture;
- otherwise: 20% random path;
- adaptive path: predicts the player's most frequent valid gesture and chooses a valid counter.

Because the pseudo-random generator is seeded, the same seed, difficulty, variant, and move sequence produces the same CPU sequence. State regression tests protect this behavior.

## Match configuration

`MatchConfig` currently contains:

```text
variant
opponentMode
difficulty
matchMode
seed
```

Defaults:

```text
variant      = CLASSIC
opponent     = CPU
difficulty   = NORMAL
match mode   = BEST_OF_3
seed         = 20260819
```

The complete current configuration is persisted under `match_config_v1` and restored when a new `ArenaState` is created. See [`storage-and-backup.md`](storage-and-backup.md).

## Match modes and completion

`MatchMode` contains:

- `BEST_OF_3` -> first to 2 wins;
- `BEST_OF_5` -> first to 3 wins;
- `TOURNAMENT` -> first to 5 wins;
- `ENDLESS` -> no automatic score target;
- `STREAK` -> no automatic score target.

`MatchConfig.roundsToWin` returns the corresponding target or `null` for Endless/Streak.

A finite match is marked `finished` when either player's score reaches the target. Once finished, further plays are ignored until the match is reset. Endless and Streak remain playable because they have no finite target.

## Round record

A completed `RoundRecord` contains:

```text
playerOne: Gesture
playerTwo: Gesture
outcome: RoundOutcome
```

Both gestures are present for every currently supported completed round.

The older phase-7 model contained nullable gestures and typed timeout end reasons. That timer/timeout model is **not** part of the current reconciled runtime unless deliberately restored with model/persistence/history/UI migration tests.

## Match snapshot

`MatchSnapshot` contains:

- immutable copy of the `MatchConfig` used to start/reset the match;
- completed round list;
- Player-1 score;
- Player-2 score;
- draw count;
- `finished` flag.

`ArenaState.resetMatch()` reconstructs the seeded CPU strategy and replaces the active snapshot with an empty snapshot using the currently persisted configuration.

## Statistics

`ArenaStats` tracks:

```text
roundsPlayed
wins
losses
draws
bestStreak
currentStreak
```

The statistics perspective is Player 1:

- Player-1 round win -> `wins + 1`, `currentStreak + 1`;
- Player-2 round win -> `losses + 1`, current streak resets to 0;
- draw -> `draws + 1`, current streak resets to 0.

`bestStreak` becomes the maximum of its previous value and the new current streak.

`winRate` is integer percentage:

```text
0 when roundsPlayed == 0
otherwise (wins * 100) / roundsPlayed
```

Persisted statistics are validated against non-negativity, round totals, and streak invariants before being accepted.

## History

Every completed round is added to `history_v1` using a human-readable canonical line:

```text
<gesture> vs <gesture> — <outcome>
```

Examples:

```text
Rock vs Scissors — Player 1 won
Paper vs Paper — Draw
```

History is newest-first, newline-sanitized, limited to 160 characters per written entry, and capped at 30 retained entries.

Existing history remains human-readable for backward compatibility. Future structured history should use explicit migration rather than silently changing the meaning of `history_v1`.

## Achievements

`ArenaState.achievements` currently derives five stable IDs from aggregate statistics:

| ID | Requirement |
|---|---|
| `first_win` | at least 1 win |
| `ten_rounds` | at least 10 rounds |
| `streak_3` | best streak at least 3 |
| `streak_7` | best streak at least 7 |
| `century` | at least 100 rounds |

Achievement titles/descriptions are presentation resources mapped from these stable IDs in the UI. Unlock identity does not depend on translated text.

## Settings boundary

`ArenaSettings` currently contains:

```text
darkTheme
followSystemTheme
reducedMotion
soundEnabled
hapticsEnabled
extendedVariant
onboardingComplete
```

The current active gameplay ruleset comes from `MatchConfig.variant`; the retained `extendedVariant` settings field exists for settings compatibility and should not become an independent conflicting gameplay source of truth.

The older pre-reconciliation player-name and persisted `AppLanguage` fields are not present in the current reconciled `ArenaSettings` model.

## Backup interaction

Backup schema `RPSARENA_BACKUP|1` contains settings, aggregate statistics, and bounded history. It deliberately does not include `match_config_v1`.

Import therefore reloads settings/statistics while leaving the local match setup as a separate persisted preference. See [`BACKUP.md`](BACKUP.md) and [`storage-and-backup.md`](storage-and-backup.md).

## Safe logging

`ArenaState` accepts a `SafeLogger`. Its default sink is no-op, so logging does not imply telemetry.

Current state events include coarse metadata for:

- onboarding completion;
- settings updates;
- backup export/import result;
- match configuration changes;
- match resets;
- completed rounds;
- invalid-gesture rejection.

Raw backup text is never passed by `ArenaState` as a log field.

## Current deferred gameplay behavior

The original pre-reconciliation phase-7 history contains work for:

- configurable round timers;
- typed timeout outcomes;
- visible seed editing;
- player-name/profile behavior;
- recent trend UI;
- destructive reset/history management.

Those features are not current runtime behavior solely because historical commits exist. They must be ported onto the reconciled model/state/persistence/resource architecture with focused regression tests before being documented as shipped.

The planned next-patch scope is tracked in [`NEXT_VERSION.md`](NEXT_VERSION.md).

## Change checklist

When changing gameplay/domain behavior:

1. update `GameModels.kt` only when the domain representation truly changes;
2. keep rule resolution inside `RulesEngine`;
3. keep CPU selection inside `CpuStrategy`;
4. validate gestures against `Gesture.availableFor()`;
5. preserve deterministic seed behavior or document/test an intentional change;
6. version/migrate `match_config_v1` if its serialized field shape changes;
7. preserve statistics/history invariants;
8. add shared tests before UI-only assertions;
9. update this guide, storage docs, changelog/roadmap, and release notes;
10. require green exact-head CI/Security/CodeQL before release.
