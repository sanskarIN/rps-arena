# Documentation Index

RPS Arena documentation is organized by audience and responsibility. This index is the recommended entry point when you need more detail than the root `README.md`.

## Start here by goal

### I want to install the tools and run the project

Read in this order:

1. [`setup.md`](setup.md) — concise setup path;
2. [`toolchain.md`](toolchain.md) — deep installation/version/upgrade guidance;
3. [`command-reference.md`](command-reference.md) — what every common command/flag means;
4. [`troubleshooting.md`](troubleshooting.md) — common environment/build/data failures.

### I want to understand the architecture

1. [`architecture.md`](architecture.md) — architecture overview;
2. [`build-system.md`](build-system.md) — Gradle/modules/source sets/targets;
3. [`domain-and-gameplay.md`](domain-and-gameplay.md) — game/state/CPU rules;
4. [`storage-and-backup.md`](storage-and-backup.md) — persistence/migration/schema;
5. [`localization.md`](localization.md) — bilingual UI architecture;
6. [`private-room-protocol.md`](private-room-protocol.md) — optional room transport boundary;
7. ADRs in [`adr/`](adr/) — architectural decisions/rationale.

### I want to work on Android

1. [`android-platform.md`](android-platform.md);
2. [`build-system.md`](build-system.md);
3. [`testing.md`](testing.md);
4. [`accessibility.md`](accessibility.md);
5. [`privacy`](../PRIVACY.md) / [`security`](../SECURITY.md) when permissions/data change.

### I want to work on iPhone or iPad

1. [`ios-platform.md`](ios-platform.md);
2. [`build-system.md`](build-system.md);
3. [`testing.md`](testing.md);
4. [`accessibility.md`](accessibility.md);
5. [`release.md`](release.md) for signing/framework boundaries.

### I want to work on Windows, Linux, or macOS desktop

1. [`desktop-platform.md`](desktop-platform.md);
2. [`build-system.md`](build-system.md);
3. [`testing.md`](testing.md);
4. [`accessibility.md`](accessibility.md).

### I want to work on the browser/Web app

1. [`web-platform.md`](web-platform.md);
2. [`build-system.md`](build-system.md);
3. [`testing.md`](testing.md);
4. [`accessibility.md`](accessibility.md);
5. [`release.md`](release.md) for compatibility-distribution packaging.

### I want to work on the optional Rust engine

1. [`rust-engine.md`](rust-engine.md);
2. `../rust-engine/README.md` for crate-local short instructions;
3. [`domain-and-gameplay.md`](domain-and-gameplay.md) for Kotlin rule source/parity expectations;
4. [`test-catalog.md`](test-catalog.md).

### I want to add/fix gameplay

1. [`domain-and-gameplay.md`](domain-and-gameplay.md);
2. [`test-catalog.md`](test-catalog.md);
3. [`development.md`](development.md);
4. [`localization.md`](localization.md) if visible labels/copy change;
5. [`maintenance.md`](maintenance.md) for integration checklist.

### I want to change storage/backup/settings

1. [`storage-and-backup.md`](storage-and-backup.md);
2. [`privacy`](../PRIVACY.md);
3. [`security`](../SECURITY.md);
4. [`test-catalog.md`](test-catalog.md);
5. [`architecture.md`](architecture.md).

### I want to add another language

1. [`localization.md`](localization.md);
2. [`accessibility.md`](accessibility.md);
3. [`test-catalog.md`](test-catalog.md);
4. [`domain-and-gameplay.md`](domain-and-gameplay.md) for canonical IDs/history constraints.

### I want to implement real LAN/private-room networking

Do **not** begin by adding permissions. Read:

1. [`private-room-protocol.md`](private-room-protocol.md);
2. [`architecture.md`](architecture.md);
3. [`security`](../SECURITY.md);
4. [`privacy`](../PRIVACY.md);
5. ADR [`adr/0002-private-room-boundary.md`](adr/0002-private-room-boundary.md);
6. [`testing.md`](testing.md) / [`test-catalog.md`](test-catalog.md).

### I want to understand CI or fix a workflow

1. [`ci-cd.md`](ci-cd.md);
2. [`validation.md`](validation.md);
3. [`command-reference.md`](command-reference.md);
4. [`github-settings.md`](github-settings.md).

### I want to prepare/publish a release

1. [`release.md`](release.md);
2. [`ci-cd.md`](ci-cd.md);
3. [`validation.md`](validation.md);
4. [`testing.md`](testing.md);
5. [`accessibility.md`](accessibility.md);
6. `../CHANGELOG.md`;
7. `../what_changed.md`.

### I want to maintain/upgrade the repo

1. [`maintenance.md`](maintenance.md);
2. [`toolchain.md`](toolchain.md);
3. [`build-system.md`](build-system.md);
4. [`ci-cd.md`](ci-cd.md);
5. [`repository-file-reference.md`](repository-file-reference.md).

## Core root documents

| Document | Purpose |
|---|---|
| [`../README.md`](../README.md) | public landing page, highlights, quick start |
| [`../CHANGELOG.md`](../CHANGELOG.md) | version-by-version notable changes |
| [`../ROADMAP.md`](../ROADMAP.md) | completed/planned milestones |
| [`../CONTRIBUTING.md`](../CONTRIBUTING.md) | contributor workflow and required checks |
| [`../CODE_OF_CONDUCT.md`](../CODE_OF_CONDUCT.md) | community conduct expectations |
| [`../SECURITY.md`](../SECURITY.md) | supported versions and private vulnerability reporting |
| [`../PRIVACY.md`](../PRIVACY.md) | local data/network/privacy behavior |
| [`../SUPPORT.md`](../SUPPORT.md) | support routes and diagnostic information |
| [`../LICENSE`](../LICENSE) | MIT license |
| [`../what_changed.md`](../what_changed.md) | detailed project implementation/validation handoff |

## Setup and developer workflow

### [`setup.md`](setup.md)

Concise prerequisites, clone/build/run flow, local data note, owner Git identity.

### [`toolchain.md`](toolchain.md)

Deep guide to JDK 17, local Gradle 9.5.1 baseline, Android SDK 36/Build Tools, macOS/Xcode for iOS, browser requirements for Web, IDEs, Python, Git, optional Rust, environment variables, and safe upgrade procedure when a tool becomes outdated/unsupported.

### [`command-reference.md`](command-reference.md)

Explains Gradle task syntax/flags, validation scripts, Android/Desktop/iOS/Web/Rust commands, Git/tag commands, diagnostics, and command safety.

### [`development.md`](development.md)

Short architecture-aware contribution workflow and layer placement rules.

### [`maintenance.md`](maintenance.md)

Long-term change playbook: versions, dependencies, schemas, languages, networking, platform targets, docs, release, Git hygiene, incidents.

## Architecture and code behavior

### [`architecture.md`](architecture.md)

High-level module/layer/persistence/offline/network boundaries.

### [`build-system.md`](build-system.md)

Explains root Gradle files, version catalog, Gradle properties, `:shared`, `:androidApp`, `:desktopApp`, `:webApp`, Android/JVM/iOS/JS/Wasm targets, source sets, task dependency behavior, output directories, and absent Gradle Wrapper.

### [`domain-and-gameplay.md`](domain-and-gameplay.md)

Complete shared model/rules/CPU/match/state/timer/score/streak/achievement/history behavior and invariants.

### [`storage-and-backup.md`](storage-and-backup.md)

Exact storage keys, PlatformStore actuals, v1->v2 migration, stat invariants, history grammar, backup schema/escaping/limits/import/reset.

### [`localization.md`](localization.md)

English/Hindi catalogs, enum-keyed labels, canonical vs localized history, achievements, version metadata, adding languages/RTL considerations.

### [`private-room-protocol.md`](private-room-protocol.md)

Room code/participant/events/gateway/session/in-memory lifecycle plus future LAN threat/authority/fairness/privacy requirements.

## Platform-specific

### [`android-platform.md`](android-platform.md)

Every Android app/resource/storage file, manifest/permissions, SDK/versioning, launcher resources/theme, APK tasks, signing/offline checks.

### [`ios-platform.md`](ios-platform.md)

Complete iPhone/iPad path: Kotlin/Native targets, NSUserDefaults adapter, Compose UIViewController bridge, SwiftUI/Xcode host, direct framework integration, versioning, CI, framework release artifacts, signing/privacy boundaries.

### [`desktop-platform.md`](desktop-platform.md)

Windows/Linux/macOS JVM desktop launcher/build/storage files, Java Preferences, run/package behavior, DEB/MSI/DMG boundaries, keyboard/responsive checks.

### [`web-platform.md`](web-platform.md)

Browser JS+Wasm compatibility targets, ComposeViewport host, localStorage persistence, development/distribution commands, CI/release packaging, browser limitations/testing.

### [`rust-engine.md`](rust-engine.md)

Every Rust crate file, rule mirror, tests, package workflow, parity/dependency/FFI boundaries.

## Testing, quality, performance, accessibility

### [`testing.md`](testing.md)

Test/build commands and manual release journey checklist.

### [`test-catalog.md`](test-catalog.md)

Every tracked Kotlin/Compose test file and its exact regression purpose/assertions.

### [`validation.md`](validation.md)

What constitutes executable release validation and current gate model.

### [`accessibility.md`](accessibility.md)

Keyboard/TalkBack/VoiceOver/browser keyboard/text scaling/contrast/motion/timer/destructive-action review policy.

### [`performance.md`](performance.md)

Bounded data/performance budgets and evidence-first regression measurement.

## Automation and release

### [`ci-cd.md`](ci-cd.md)

Every `.github` file: CI, CodeQL, release workflow, Dependabot, issue forms, PR template, release categories, funding, permissions/concurrency, Android/Desktop/iOS/Web/Rust artifacts.

### [`release.md`](release.md)

Release gate, cross-platform version locations, Web/iOS artifacts, signing/notarization boundaries, notes/rollback.

### [`github-settings.md`](github-settings.md)

Repository-hosted settings not representable entirely in Git: branch rules, security features, Discussions, labels, milestones, metadata.

## Operations/support

### [`troubleshooting.md`](troubleshooting.md)

Common SDK/JDK/Gradle/desktop/backup/timer/private-room/CI problems.

### [`glossary.md`](glossary.md)

Project/build/platform/security/release terminology and repository-specific identifiers.

### [`branding-assets.md`](branding-assets.md)

Root SVGs, Android adaptive icon resources, shared theme ownership, accessibility/export/rebranding checks.

## Architecture Decision Records

### [`adr/0001-offline-first-kmp.md`](adr/0001-offline-first-kmp.md)

Why the product uses an offline-first Kotlin Multiplatform/shared architecture.

### [`adr/0002-private-room-boundary.md`](adr/0002-private-room-boundary.md)

Why optional private-room networking stays behind a transport boundary and primary gameplay remains offline.

## Roadmap pointer

`docs/ROADMAP.md` intentionally points to the canonical root [`../ROADMAP.md`](../ROADMAP.md) to avoid duplicate roadmap state.

## Documentation quality rules

- The root README should remain concise enough to be a landing page; deep detail belongs here under `docs/`.
- Do not duplicate incompatible values across documents; link to a canonical source where practical.
- Do not claim unimplemented features as shipped.
- Version/tool claims should distinguish project baseline from globally newest available releases.
- Commands should explain prerequisites and side effects.
- Security/privacy docs must change when permissions/data/network behavior changes.
- Every tracked repository file must appear in `repository-file-reference.md`.
- New documents must be added to this index when they introduce a new subject area.

## Documentation validation

Formatting:

```bash
python3 scripts/check_format.py
```

Every-file reference coverage:

```bash
python3 scripts/check_docs_coverage.py
```

The coverage check is intended to prevent a new tracked file from being silently omitted from the repository file reference.
