# Privacy Policy

RPS Arena is designed to work offline and does not require an account.

## Data stored on device

The app stores only local gameplay preferences and progress needed for its features:

- theme and accessibility settings;
- local profile display names and the active-profile selection;
- persisted match configuration such as ruleset, mode, difficulty, timer, and seed;
- aggregate rounds, wins, losses, draws, and streaks;
- up to 30 recent round summaries;
- onboarding completion state.

Local profiles are device-local identities, not online accounts. RPS Arena does not create authentication identifiers, upload profile names, or require a remote profile service.

## Backups

The app can generate a versioned, human-readable backup containing the local settings, profiles, aggregate statistics, match configuration, and recent history described above. Backup text is shown only to the user and is not uploaded by RPS Arena.

Treat exported backup text according to your own privacy needs because it may contain local profile display names and recent game summaries. The backup format is not encryption and must not be used for passwords, API keys, payment information, or other secrets.

A backup is fully validated before import. The preview operation is non-mutating and lets the user inspect a summary before replacing local data.

## Clipboard

The completed-round `Copy result` action writes only the displayed round summary to the operating system clipboard after an explicit user action. RPS Arena does not read clipboard contents and does not upload copied result text.

Clipboard contents may remain available to the operating system or other applications according to platform behavior, so copied text should be treated as user-controlled shared data after the copy action.

## Logging

Runtime logging is local and deliberately structured around technical events. Profile display names, backup contents, history text, credentials, and other free-form user data are not intentionally written to logs. The logging helper also redacts sensitive field names before formatting structured metadata.

## Network and tracking

The default application does not request Android internet permission and does not include analytics, advertising, telemetry, or remote account SDKs. CPU play and same-device two-player play remain fully local.

The codebase contains a protocol/transport boundary for a future explicitly opt-in private-room/LAN feature, but no production network transport is enabled in v1 and local gameplay does not depend on it.

About-screen links open only after a user selects them and are then handled by the operating system or external application.

## Deleting data

The in-app Settings screen can clear recent history, undo the most recent history clear until new history is written, or reset all RPS Arena local data after confirmation.

Uninstalling the app removes application-local data according to platform behavior. Desktop users can also clear the `in/sanskar/rpsarena` preferences node through their operating system's Java preferences storage.

Questions: `supportramsandesh@gmail.com`.
