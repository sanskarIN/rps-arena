# Reconciliation File Reference

This temporary companion to `repository-file-reference.md` documents files introduced from the validated `main` milestones while the v2.5.8 cross-platform branch is being reconciled. `scripts/check_docs_coverage.py` reads both references so exhaustive tracked-file coverage remains enforced throughout the transition. Once v2.5.8 documentation is consolidated, these entries can be folded into the canonical reference and this file removed in the same commit.

## Reconciliation reference

### `docs/reconciliation-file-reference.md`

Temporary self-documenting bridge for current-main files that were not present when the original v2.5.8 repository reference was written.

### `docs/NEXT_VERSION.md`

Post-v2.5.8 planning document for v2.5.9. It defines release entry criteria, the planned version/build-code transition, scoped candidate work, deferred items, validation requirements, and the rule that package metadata remains at v2.5.8 until the current release is actually merged and tagged.

### `docs/BACKUP.md`

Authoritative current backup-schema guide imported from the validated backup milestone. It documents `RPSARENA_BACKUP|1`, included data, validation, privacy, and forward-compatibility rules.

### `docs/LOCALIZATION.md`

Authoritative Compose Multiplatform resource-localization guide imported from the validated localization milestone. It documents English/Hindi resource layout and catalog validation.

### `docs/UI_TESTING.md`

Authoritative UI-automation guide imported from the validated UI-testing milestone. It documents the common/desktop/Android device-test split, stable tags, isolated persistence, local execution, and CI behavior.

### `docs/V0.1.3_PLAN.md`

Public v0.1.3 release plan defining the focused patch scope, release gates, compatibility boundaries, and meaningful-commit policy for the reliability hardening cycle.

### `docs/V0.1.3_RELEASE_CHECKLIST.md`

Public v0.1.3 release checklist covering source validation, cross-platform builds/tests, security/privacy checks, documentation consistency, release metadata, and post-release verification.

### `docs/V0.1.3_RELEASE_NOTES.md`

Evidence-based v0.1.3 release notes documenting only the backup and deterministic CPU regression coverage delivered by the release candidate, together with verification and compatibility boundaries.

### `scripts/verify_localizations.py`

Validates the Compose Multiplatform English and Hindi resource catalogs, including key parity and placeholder compatibility.

### `shared/src/androidDeviceTest/AndroidManifest.xml`

Android KMP device-test manifest that provides the instrumentation-only host activity used by shared Compose UI automation. It does not add production permissions.

### `shared/src/androidDeviceTest/kotlin/in/sanskar/rpsarena/RpsArenaAndroidUiTest.kt`

Android instrumentation smoke coverage for onboarding and gameplay using stable localization-independent semantic tags and isolated in-memory persistence.

### `shared/src/commonMain/composeResources/values/strings.xml`

Canonical English Compose resource catalog for shared UI text.

### `shared/src/commonMain/composeResources/values-hi/strings.xml`

Hindi Compose resource catalog kept in key/placeholder parity with the canonical English catalog.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaBackup.kt`

Versioned bounded offline backup model, decoder/encoder, validation errors, and import result contracts used by the current shared repository implementation.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaUiTags.kt`

Stable semantic UI-test tags kept independent of visible English/Hindi strings so automation remains localization-safe.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaBackupCodecTest.kt`

Shared regression coverage for backup schema round trips, malformed input, history limits/sanitization, unsupported versions, and statistics invariants.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/CpuStrategyTest.kt`

Shared deterministic CPU regression coverage across supported difficulties and classic/extended variants, including seeded sequence stability and gesture-boundary checks.
