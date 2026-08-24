# Reconciliation File Reference

This temporary companion to `repository-file-reference.md` documents files introduced from the validated `main` milestones while the v2.5.8 cross-platform branch is being reconciled. `scripts/check_docs_coverage.py` reads both references so exhaustive tracked-file coverage remains enforced throughout the transition. Once v2.5.8 documentation is consolidated, these entries can be folded into the canonical reference and this file removed in the same commit.

## Reconciliation reference

### `docs/reconciliation-file-reference.md`

Temporary self-documenting bridge for current-main files that were not present when the original v2.5.8 repository reference was written.

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

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/data/ArenaStore.kt`

Injectable minimal key-value persistence boundary. Production delegates to `PlatformStore`; tests provide isolated in-memory storage without changing platform persistence behavior.

### `shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaUiTags.kt`

Stable semantic UI-test tags kept independent of visible English/Hindi strings so automation remains localization-safe.

### `shared/src/commonTest/kotlin/in/sanskar/rpsarena/ArenaBackupCodecTest.kt`

Shared regression coverage for backup schema round trips, malformed input, history limits/sanitization, unsupported versions, and statistics invariants.
