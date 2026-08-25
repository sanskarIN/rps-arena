# GitHub Repository Settings Guide

Repository-host settings are not fully represented by committed files. Use this guide to keep the public `sanskarIN/rps-arena` repository aligned with its CI and security model.

## Default branch

Use `main` as the default branch.

## Recommended branch/ruleset protection for `main`

Configure a branch ruleset that:

- requires a pull request before merging;
- requires required status checks to pass;
- requires the `CI` Kotlin and Rust jobs and the `CodeQL` analysis job once their exact hosted check names are visible;
- requires branches to be up to date before merge when GitHub reports meaningful base conflicts;
- blocks force pushes;
- blocks branch deletion;
- applies to repository administrators unless an emergency recovery procedure requires a temporary documented exception;
- allows merge commits so the repository can preserve the requested atomic commit history.

Do not require a status check name until that check has completed successfully at least once, otherwise GitHub can make the branch impossible to merge.

## Merge policy

Enable merge commits. Squash merging is not recommended for milestone PRs because this repository intentionally preserves small meaningful commits. Rebase merging can remain available for contributors when it does not remove useful review history.

Delete feature branches automatically after a successful merge when no follow-up work depends on them.

## Actions permissions

- Keep workflow permissions at the least privilege declared by each workflow.
- CI and CodeQL require read-only repository contents.
- The release workflow requires `contents: write` only so a validated tag can publish a GitHub Release.
- Do not grant arbitrary write permissions to pull-request workflows.
- Do not expose repository secrets to untrusted fork pull requests.

## Security features

Where available for the repository/account plan, enable:

- Dependabot alerts;
- Dependabot security updates;
- dependency graph;
- secret scanning / push protection;
- CodeQL/default code scanning alerts;
- private vulnerability reporting.

Do not interpret an empty alert list as proof that the project has no vulnerability.

## Discussions

GitHub Discussions can be enabled for general usage questions, ideas, and community help. Security reports must follow `SECURITY.md`, not Discussions.

Suggested categories:

- Announcements;
- Q&A;
- Ideas;
- Show and tell.

## Labels

Recommended labels:

- `bug`
- `enhancement`
- `feature`
- `documentation`
- `security`
- `reliability`
- `accessibility`
- `android`
- `desktop`
- `kotlin`
- `rust`
- `dependencies`
- `good first issue`
- `help wanted`
- `skip-changelog`

The committed `.github/release.yml` uses several of these labels to categorize generated release notes.

## Milestones

Create milestones only for concrete planned releases. Suggested current milestone naming:

- `v2.5.8` — completion/release hardening;
- later milestones should follow semantic versioning and the canonical `ROADMAP.md`.

Close a milestone only after its tagged release or explicit cancellation.

## Repository metadata

Recommended description:

> Offline-first Rock Paper Scissors arena for Android and desktop with deterministic CPU challenges, local two-player play, Lizard–Spock, timers, stats, backups, and Kotlin Multiplatform.

Suggested topics:

`kotlin`, `kotlin-multiplatform`, `compose-multiplatform`, `android`, `desktop`, `rock-paper-scissors`, `offline-first`, `open-source`, `game`

Website/funding navigation may point to `https://buymeacoffee.com/sanskarIN` when appropriate without making donations part of the product flow.

## Release settings

Create releases only from validated version tags such as `v2.5.8`. Public CI artifacts are unsigned unless authorized release credentials have been configured outside Git.

Never paste signing credentials, access tokens, recovery codes, or private certificates into issue bodies, workflow files, release notes, or repository variables intended to be public.
