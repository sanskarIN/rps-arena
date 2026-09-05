# Reconciliation File Reference — v2.5.8 Release Addendum

This companion to `repository-file-reference.md` is intentionally retained during v2.5.8 release finalization because several current-main files were introduced after the older canonical reference was written. `scripts/check_docs_coverage.py` reads both references so exhaustive tracked-file coverage remains enforced.

This is a **temporary release-finalization addendum**, not a second product architecture source of truth. After the v2.5.8 release is complete, fold these entries into `repository-file-reference.md` and remove this file in the same focused cleanup change. Do not delete it earlier: doing so without updating the canonical reference would fail the coverage gate.

## Current-main entries pending canonical consolidation

### `docs/NEXT_VERSION.md`

Post-v2.5.8 planning document for v2.5.9. It defines the release entry criteria, planned version/build-code transition, scoped candidate work, deferred items, validation requirements, and the rule that package metadata remains at v2.5.8 until the current release is actually tagged and its artifacts/checksums are verified.

### `docs/BACKUP.md`

Authoritative current backup-schema guide for `RPSARENA_BACKUP|1`. It documents included data, validation, privacy, bounded history, and forward-compatibility expectations.

### `docs/LOCALIZATION.md`

Authoritative Compose Multiplatform localization guide. It documents the English/Hindi resource layout, catalog parity, placeholder compatibility, and localization testing workflow.

### `docs/UI_TESTING.md`

Authoritative UI-automation guide. It documents the common/desktop/Android device-test split, stable semantic tags, isolated persistence, local execution, and CI behavior.

### `scripts/verify_localizations.py`

Validates the Compose Multiplatform English and Hindi resource catalogs, including key parity and placeholder compatibility.

### `shared/src/androidDeviceTest/AndroidManifest.xml`

Android instrumentation-only manifest for the shared UI-test host. It does not add production permissions or gameplay networking.

### `shared/src/androidDeviceTest/kotlin/in/sanskar/rpsarena/RpsArenaAndroidUiTest.kt`

Android instrumentation smoke coverage for onboarding and gameplay using stable localization-independent semantic tags and isolated in-memory persistence.

### `shared/src/commonMain/composeResources/values/strings.xml`

Canonical English Compose resource catalog for shared UI text.

### `shared/src/commonMain/composeResources/values-hi/strings.xml`

Hindi Compose resource catalog kept in key and placeholder parity with the canonical English catalog.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaBackup.kt`

Versioned bounded offline backup model, decoder/encoder, validation errors, and import-result contracts used by the current shared repository implementation.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaStore.kt`

Injectable minimal key-value persistence boundary. Production delegates to platform storage while tests use isolated in-memory storage.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaUiTags.kt`

Stable semantic UI-test tags kept independent of visible English/Hindi strings so automation remains localization-safe.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaBackupCodecTest.kt`

Shared regression coverage for backup schema round trips, malformed input, history limits/sanitization, unsupported versions, and statistics invariants.

## Consolidation rule

Once v2.5.8 is tagged and its release artifacts/checksums are verified:

1. fold every current-main entry above into `docs/repository-file-reference.md`;
2. verify `python3 scripts/check_docs_coverage.py` with only the canonical reference;
3. remove this addendum in the same cleanup commit;
4. confirm documentation-link validation and the full CI gate remain green.
