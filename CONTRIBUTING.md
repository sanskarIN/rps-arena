# Contributing

Thanks for helping improve RPS Arena.

## Local setup

1. Install JDK 17 or newer.
2. Install Gradle 9.5.1.
3. Install Android Studio with Android SDK 36 for Android work.
4. Clone the repository.
5. Run `gradle :shared:allTests`.
6. Run `gradle :desktopApp:run` or `gradle :androidApp:assembleDebug`.

## Commit identity

For owner-authored local commits, configure:

```bash
git config user.name "Sanskar"
git config user.email "sanskarin@outlook.in"
```

## Commit style

Prefer small, meaningful commits using prefixes such as `feat:`, `fix:`, `test:`, `docs:`, `build:`, `ci:`, and `chore:`.

## Pull requests

- Keep changes focused.
- Add or update tests for behavior changes.
- Run Kotlin tests and Rust tests when touching the respective engines.
- Update `what_changed.md` for substantial repository changes.
- Do not add tracking, ads, or unnecessary network access without a clearly documented design discussion.
