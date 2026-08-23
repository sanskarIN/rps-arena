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

## Localized history

Newly played rounds are persisted as structured `ArenaHistoryEntry.Round` records containing stable gesture and outcome identifiers rather than presentation text. The History screen resolves those identifiers through the same localized resources used by the live match screen, so a stored round follows the current runtime language.

Existing `history_v1` summaries and history imported from schema-v1 backups are preserved as `ArenaHistoryEntry.Legacy` records. Legacy entries are intentionally displayed exactly as saved because their original semantic structure cannot be reconstructed reliably from arbitrary human-readable text.

The repository prioritizes structured `history_v2` storage and falls back to `history_v1` when no valid structured history exists. The first new structured round carries readable legacy entries forward into the versioned v2 store. Backup schema 2 stores structured rounds directly while continuing to import schema 1. See [BACKUP.md](BACKUP.md).

## Localization boundaries

The visible shared Compose screens, controls, match labels, gesture labels, achievement copy, backup dialogs, validation errors, onboarding, settings, About content, and structured round history use resources.

Only legacy history summaries remain display-as-saved for compatibility. This is a deliberate migration boundary rather than a limitation on newly recorded rounds.

## Review checklist

- Translation keys match the default catalog.
- Placeholders match the default catalog.
- Structured history renders through resources instead of persisted English labels.
- Legacy history remains readable after migration and restore.
- Android debug assembly succeeds.
- Desktop classes compile.
- Shared and UI tests pass.
- No new Android permission or network dependency is introduced.
- Backup schema changes include an explicit backward-compatible decoder path.
