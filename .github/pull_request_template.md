## Summary

Describe the change and the user/developer problem it solves.

## Scope

- Affected platforms: Android / Windows / macOS / Linux / shared / Rust / repository-only
- Affected areas: rules / CPU / UI / persistence / profiles / backup / networking boundary / build / docs / security

## Verification

List the exact commands/tests run and the result. For deterministic bug fixes, include the regression test that covers the previous failure.

## Compatibility and data

- Does this change persisted data or backup format? If yes, describe migration/backward compatibility.
- Does this change Android permissions, networking, clipboard behavior, logging, or external links? If yes, explain why.

## Accessibility and privacy

Describe any effect on keyboard/touch semantics, screen-reader labels, motion, text scaling, color/status communication, local user data, or logging.

## Documentation

List updated docs/changelog entries, or explain why none are needed.

## Checklist

- [ ] The change is focused and does not contain unrelated churn.
- [ ] Shared rules/state remain independent from unnecessary platform/network dependencies.
- [ ] Tests were added or updated where the behavior is deterministic.
- [ ] `scripts/verify.sh` or `scripts/verify.ps1` was run when the local toolchain permits it.
- [ ] No secrets, signing material, private user data, or production credentials were committed.
- [ ] Accessibility and reduced-motion behavior were considered for UI changes.
- [ ] Persistence/backup compatibility was considered for data-model changes.
- [ ] User-visible behavior is reflected in `CHANGELOG.md` and relevant documentation.
- [ ] Release-bound merging will wait for the latest CI, documentation, CodeQL, and security checks.
