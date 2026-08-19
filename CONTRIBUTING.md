# Contributing to RPS Arena

Thanks for contributing to RPS Arena. Keep changes focused, testable, accessible, and privacy-preserving.

## Development setup

1. Install JDK 17 or newer.
2. Install Gradle 9.5.0.
3. Install Android SDK 37 if you will build the Android app.
4. Clone the repository and run:

```bash
gradle :shared:desktopTest
gradle :desktopApp:compileKotlin
gradle :androidApp:assembleDebug
gradle :androidApp:lintDebug
```

See `docs/setup.md` for platform details.

## Commit style

Use small Conventional Commits when practical, for example `feat: add ...`, `fix: handle ...`, `test: cover ...`, and `docs: document ...`.

Project-owner commits should use `sanskarin@outlook.in` as the Git email.

## Pull requests

- Keep one coherent change per pull request.
- Add regression coverage for fixes.
- Explain accessibility, privacy, storage, or permission changes.
- Never commit credentials, signing material, tokens, personal user data, or private endpoints.
- Update `CHANGELOG.md` for user-visible changes.

## Product principles

- Offline functionality must not require sign-in.
- CPU behavior must remain transparent and non-deceptive.
- Donation prompts must remain optional and non-intrusive.
- New UI must work with scalable text, keyboard interaction where applicable, and reduced motion.

## Contact

Business: `sanskarin@outlook.in` / `sanskarin.business@gmail.com`  
Support: `supportramsandesh@gmail.com`
