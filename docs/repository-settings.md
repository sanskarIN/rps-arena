# Repository Settings Guide

This file documents GitHub-side settings that cannot be fully enforced by source files alone. Apply them to `sanskarIN/rps-arena` before a public release when the corresponding GitHub feature is available for the repository/account.

## Default branch and pull requests

- Keep `main` as the default branch.
- Prefer pull requests for release-bound changes.
- Require the latest branch to be up to date before merging when practical.
- Preserve meaningful individual commits; do not require squash-only merging for this project.
- Enable automatic deletion of merged feature branches if that matches the maintainer workflow.

## Recommended branch protection / ruleset for `main`

Require successful checks that correspond to the repository workflows:

- CI / Kotlin job
- CI / Rust job
- Documentation link validation
- CodeQL analysis
- Security checks / committed secret scan
- Security checks / dependency review for pull requests

Also consider:

- require pull requests before merging;
- require conversation resolution;
- block force pushes to `main`;
- block branch deletion;
- require linear history only if it does not conflict with the chosen merge strategy;
- allow maintainers to bypass only for genuine recovery situations.

Do not turn on a required check until its exact GitHub check name has been observed successfully at least once, otherwise merges can be accidentally blocked by a nonexistent status name.

## Security features

When available, enable GitHub-native:

- Dependabot alerts;
- Dependabot security updates;
- dependency graph;
- secret scanning;
- push protection for supported secret types;
- code scanning / CodeQL alerts;
- private vulnerability reporting.

Repository files already provide complementary checks through `.github/dependabot.yml`, `.github/workflows/codeql.yml`, `.github/workflows/security.yml`, and `scripts/check_for_secrets.py`.

## Discussions, issues, and community health

- Enable Issues for bug reports and feature requests.
- Enable Discussions only if the maintainer wants a separate community Q&A/ideas space.
- Keep the issue forms in `.github/ISSUE_TEMPLATE/` enabled.
- Keep the pull request template enabled.
- Use GitHub's community standards view to confirm that the license, code of conduct, contributing guide, security policy, support guide, and issue templates are detected.

Suggested labels:

- `bug`
- `enhancement`
- `documentation`
- `accessibility`
- `android`
- `desktop`
- `rust`
- `security`
- `dependencies`
- `good first issue`
- `help wanted`
- `release`

Suggested milestones:

- `v1.0`
- `v1.1`
- `v1.2`

## Releases

- Publish releases from verified commits on `main`.
- Use semantic tags such as `v1.0.0`.
- Let `.github/workflows/release.yml` build unsigned Android and native desktop artifacts.
- Never commit Android signing keys, keystores, certificates, passwords, tokens, or release credentials.
- Store signing material only in an appropriate secure release environment when signed distribution is introduced.

## Repository metadata

Recommended public metadata:

- Description: `Privacy-first Rock Paper Scissors arena for Android and desktop, built with Kotlin and Compose Multiplatform.`
- Website: leave empty unless an official project website is maintained.
- Topics: `rock-paper-scissors`, `kotlin`, `compose-multiplatform`, `android`, `desktop`, `offline-first`, `open-source`, `game`.

Do not add social/profile URLs that are expected to change frequently unless they are actively maintained.

## Verification checklist

Before changing a repository setting that can block contributions or releases:

1. Confirm the relevant workflow exists on `main`.
2. Confirm its most recent run succeeds.
3. Copy the exact status/check name from GitHub.
4. Apply the rule to `main`.
5. Open a small test pull request when possible to confirm the rule behaves as intended.
6. Record material repository-setting changes in `what_changed.md`.
