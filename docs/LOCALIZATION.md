# Localization

RPS Arena uses Compose Multiplatform resources for user-facing shared UI text. English is the default catalog and Hindi is the first additional locale.

## Current locale support

- English (`values/strings.xml`) — default and fallback.
- Hindi (`values-hi/strings.xml`) — selected automatically when the runtime locale is Hindi.
- Android, iOS/iPadOS, desktop, and Web consume the same shared Compose resource catalogs.
- Locale selection requires no network access, analytics, or account.

## Resource layout

```text
shared/src/commonMain/composeResources/
├── values/
│   └── strings.xml
└── values-hi/
    └── strings.xml
```

The shared module includes `compose.components.resources`, and generated resource imports use `in.sanskar.rpsarena.resources`.

## Translation contract

The default English catalog is authoritative. Every localized catalog must:

1. contain exactly the same string keys as the default catalog;
2. preserve printf-style placeholders such as `%1$s` and `%1$d` in the same order;
3. remain valid XML;
4. keep brand names, contact addresses, and technical identifiers accurate;
5. avoid network-dependent translation behavior.

Run:

```bash
python3 scripts/verify_localizations.py
```

The validator is also part of local verification and CI.

## Adding another language

1. Create a qualified directory such as `values-es/`.
2. Copy `values/strings.xml` into it.
3. Translate text values while keeping every `name` unchanged.
4. Preserve placeholders and XML escaping.
5. Run `python3 scripts/verify_localizations.py`.
6. Run normal shared/UI/platform verification.

Compose Multiplatform falls back to the default catalog when a locale-specific resource cannot be selected.

## Localization boundaries

Shared Compose screens, controls, match labels, gesture labels, achievement copy, backup dialogs/errors, onboarding, settings, and About content use resources.

Recent-history entries are currently persisted as human-readable summaries for compatibility with `history_v1` and backup schema `RPSARENA_BACKUP|1`. Existing history is therefore displayed as stored. A future structured-history migration must preserve old history/backups explicitly.

The pre-reconciliation `ArenaStrings` / `AppLanguage` implementation is not the current runtime architecture and must not be reintroduced over these resource catalogs.

## Review checklist

- Translation keys match the default catalog.
- Placeholders match the default catalog.
- Shared tests pass.
- Desktop UI tests pass.
- Android device-test APK compiles.
- Android/desktop/Web platform builds pass.
- No new Android permission or network dependency is introduced.
- Backup compatibility remains unchanged unless a separately documented migration is intentional.
