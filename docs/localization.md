# Localization Architecture and Compatibility Notes

RPS Arena's current localization system is **Compose Multiplatform resources**. The authoritative implementation/translation guide is [`LOCALIZATION.md`](LOCALIZATION.md).

This lowercase guide is retained because older v2.5.8 branch history and documentation linked to this path. Its purpose after reconciliation is to document the migration boundary and prevent the superseded manual string architecture from being reintroduced accidentally.

## Current architecture

User-facing shared UI strings live in:

```text
shared/src/commonMain/composeResources/values/strings.xml
shared/src/commonMain/composeResources/values-hi/strings.xml
```

Generated resource imports use:

```text
in.sanskar.rpsarena.resources
```

Visible Compose UI reads those resources through `stringResource(...)`.

The English catalog is the default/fallback catalog. Hindi is selected through normal runtime locale resolution rather than a persisted `AppLanguage` enum inside `ArenaSettings`.

## Automated contract

Run:

```bash
python3 scripts/verify_localizations.py
```

The validator requires every localized catalog to:

- contain the same keys as the default English catalog;
- preserve formatting placeholders in the same order;
- remain valid XML.

The same validation runs in local verification and CI.

## Stable UI automation

UI tests do not rely on translated labels. `ArenaUiTags` provides localization-independent semantic identifiers for onboarding, navigation, gameplay, settings, backup dialogs, and other high-value controls.

That separation allows copy/translations to change without destabilizing selectors.

## Persisted history boundary

`history_v1` currently stores human-readable round summaries for compatibility with existing installations and backup schema `RPSARENA_BACKUP|1`.

Existing history entries are displayed as stored. The application does not rewrite persisted history when the runtime locale changes.

A future structured-history migration should:

1. introduce an explicitly versioned representation;
2. retain a reader for existing `history_v1` values;
3. preserve schema-1 backup compatibility or introduce a new backup schema deliberately;
4. add migration and rendering tests before release.

## Superseded pre-reconciliation architecture

The old phase-7 branch contained:

```text
ArenaStrings.kt
AchievementStrings.kt
AppLanguage
settings_v2 language field
```

and manual language switching from state/settings.

Those components are **not** the current reconciled runtime and should not be copied over the Compose-resource implementation. Any future in-app language picker should work with the current resource system and platform locale APIs rather than reviving parallel string catalogs.

## Adding another language

Follow [`LOCALIZATION.md`](LOCALIZATION.md). In summary:

1. add an appropriate `values-<language>/strings.xml` catalog;
2. preserve every key and placeholder;
3. run `scripts/verify_localizations.py`;
4. run shared tests, desktop UI tests, Android instrumentation packaging, and relevant platform builds;
5. manually review long text, accessibility, RTL behavior when relevant, and typography/glyph coverage.

## Technical values that remain language-neutral

Do not translate identifiers such as:

- `RPSARENA_BACKUP|1`;
- semantic/build version numbers;
- stable `ArenaUiTags` values;
- enum names used by persisted `match_config_v1`;
- URLs and email addresses.

Brand text may remain intentionally stable according to project branding policy.

## Current limitation

The reconciled UI does not currently provide an explicit in-app English/Hindi switch. Locale selection follows the runtime environment. A future language selector is valid product work, but it must be implemented against Compose resources and tested on every supported platform rather than restoring the old `AppLanguage` persistence model unchanged.
