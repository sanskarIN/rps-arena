# Privacy Policy

RPS Arena is designed to work offline and does not require an account.

## Data stored on device

The app stores only local gameplay preferences and progress needed for its features:

- theme and accessibility settings;
- local player display name;
- selected interface language;
- aggregate rounds, wins, losses, draws, and streaks;
- up to 30 recent round summaries, including timeout outcomes;
- onboarding completion state.

The current application does not collect passwords, payment data, contacts, precise location, device identifiers for tracking, advertising identifiers, or account credentials.

## Backup text

The Settings screen can prepare a versioned plain-text backup containing local settings, aggregate statistics, and recent history. The app does not upload this backup. Copying, storing, or sharing exported backup text is controlled by the user and by the destination application chosen outside RPS Arena.

A backup can include the local player display name and recent match summaries. Treat it as personal local data if those details are sensitive to you.

## Network and tracking

The primary Android application does not request internet permission and does not include analytics, advertising, telemetry, remote-account, or cloud-model SDKs.

The repository contains transport-neutral private-room interfaces and an in-memory reference adapter that performs no network I/O. A future real LAN adapter, if approved, must be an explicit optional feature and will require a privacy review before release. The offline game must remain usable without it.

## Retention

Recent history is intentionally bounded to 30 entries. Aggregate statistics remain until the user resets local data, application/platform storage is cleared, or the app is uninstalled according to platform behavior.

## Deleting data

The Settings screen provides a confirmed **Reset local data** action that clears local statistics, history, player preferences, and backup text while keeping onboarding completed for convenience.

Uninstalling the Android app removes application-local data according to Android/platform behavior. Desktop storage uses the Java preferences mechanism and is subject to the operating system/runtime's local storage behavior.

## Third parties

RPS Arena does not send gameplay data to Buy Me a Coffee, GitHub, support email providers, or other linked sites. Opening an external link is a separate action governed by that service's policies.

## Contact

Privacy/support questions: `supportramsandesh@gmail.com`.

Business contact: `sanskarin@outlook.in`.

Last updated: 2026-08-19.
