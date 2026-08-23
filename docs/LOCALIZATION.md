# Localization

RPS Arena uses Compose Multiplatform resources for user-facing shared UI text. English is the default catalog and Hindi is the first additional locale.

## Current locale support

- English (`values/strings.xml`) — default and fallback.
- Hindi (`values-hi/strings.xml`) — selected automatically when the runtime locale is Hindi.
- Android and desktop use the same shared string catalogs for the Compose UI.
- The app remains offline-first; locale selection does not require network access, analytics, or an account.

## Resource layout

```text
shared/src/commonMain/composeResources/
├── values/
│   └── strings.xml
└── values-hi/
    └── strings.xml
```

The shared module declares `compose.components.resources` and fixes the generated resource package to `in.sanskar.rpsarena.resources` so imports remain stable across build environments.

## Translation contract

The default English catalog is authoritative. Every localized catalog must:

1. contain exactly the same string keys as the default catalog;
2. preserve printf-style placeholders such as `%1$s` and `%1$d` in the same order;
3. remain valid XML;
4. keep brand names, contact addresses, and technical identifiers accurate;
5. avoid adding network-dependent translation behavior.

Run the catalog validator after changing any translation:

```bash
python3 scripts/verify_localizations.py
```

The validator is also part of `scripts/verify.sh`, `scripts/verify.ps1`, and GitHub Actions CI.

## Adding another language

1. Create a qualified directory such as `values-es/` using the supported ISO language qualifier.
2. Copy `values/strings.xml` to the new directory.
3. Translate only the text values; keep every `name` attribute unchanged.
4. Preserve placeholders and XML escaping.
5. Run `python3 scripts/verify_localizations.py`.
6. Run the normal shared, Android, and desktop verification tasks.

Compose Multiplatform falls back to the default catalog whenever a locale-specific resource cannot be selected.

## Localization boundaries

The visible shared Compose screens, controls, match labels, gesture labels, achievement copy, backup dialogs, validation errors, onboarding, settings, and About content use resources.

Recent-history entries are currently persisted as human-readable summaries for backward compatibility with `history_v1` and backup schema `RPSARENA_BACKUP|1`. Existing stored history is therefore displayed exactly as saved. A future persistence migration can introduce structured history records without invalidating existing backups.

## Review checklist

- Translation keys match the default catalog.
- Placeholders match the default catalog.
- Android debug assembly succeeds.
- Desktop classes compile.
- Shared tests pass.
- No new Android permission or network dependency is introduced.
- Backup schema compatibility remains unchanged unless a separately documented migration is intentional.
