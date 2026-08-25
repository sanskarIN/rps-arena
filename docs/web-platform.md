# Web Platform Reference

RPS Arena now ships a browser application module that reuses the same shared Compose UI and business logic as Android, iOS, and desktop. The Web target uses Kotlin/Wasm for modern browsers and Kotlin/JS as a compatibility fallback.

## Platform status

- Kotlin/Wasm is the primary browser target.
- Kotlin/JS is built alongside it for compatibility distribution.
- The same `RpsArenaApp()` composable is used on Web.
- Browser persistence uses `localStorage` through the shared `PlatformStore` abstraction.
- Web support follows the upstream Compose Multiplatform Web stability level; the repository treats browser builds as supported but keeps platform-specific limitations explicit.

## Files

```text
webApp/build.gradle.kts
webApp/src/webMain/kotlin/in/sanskar/rpsarena/web/Main.kt
webApp/src/webMain/resources/index.html
shared/src/webMain/kotlin/in/sanskar/rpsarena/data/PlatformStore.web.kt
```

## Module architecture

`settings.gradle.kts` includes:

```text
:webApp
```

The web application depends on `:shared` and contains only browser startup/packaging code.

Architecture:

```text
Browser host -> webApp main() -> RpsArenaApp -> shared state/engine/repository
                                      |
                                      -> PlatformStore.web -> localStorage
```

Game rules, CPU behavior, history grammar, settings codecs, backup format, localization, and UI screens remain shared.

## Dual Web targets

`webApp/build.gradle.kts` declares both:

```kotlin
js {
    browser()
    binaries.executable()
}

wasmJs {
    browser()
    binaries.executable()
}
```

The shared module declares matching `js` and `wasmJs` library targets.

The Kotlin default hierarchy creates a `webMain` source set shared by both browser backends.

## Why both JS and Wasm exist

The normal development target can be Wasm, but the compatibility distribution combines both outputs so modern browsers can use Wasm while a JavaScript build remains available as a fallback where required by the generated compatibility loader.

Do not create separate product logic for JS and Wasm unless a platform API truly requires it.

## Browser entry point

`webApp/src/webMain/.../Main.kt`:

1. initializes `PlatformStore`;
2. creates a `ComposeViewport` attached to the `webApp` host element;
3. renders `RpsArenaApp()`.

The host page defines the viewport container and full-window CSS explicitly so the Compose canvas fills the browser area.

## Browser storage

`PlatformStore.web.kt` uses:

```text
window.localStorage
```

The same repository keys used on other platforms are stored as strings.

Important behavior:

- data is local to the browser origin/profile;
- private/incognito browsing policies may clear or isolate storage;
- clearing browser site data removes RPS Arena local state;
- storage quotas and browser policy are controlled by the browser;
- the explicit backup/export feature remains the portable app-level transfer format.

The Web adapter must not bypass `ArenaRepository` validation/codecs.

## Run the Wasm development app

From the repository root:

```bash
gradle :webApp:wasmJsBrowserDevelopmentRun --stacktrace
```

The development server prints the local URL/port.

## Run the JavaScript development app

```bash
gradle :webApp:jsBrowserDevelopmentRun --stacktrace
```

Use this when testing the JS fallback directly.

## Build production Wasm artifacts

```bash
gradle :webApp:wasmJsBrowserDistribution --stacktrace
```

Output is generated under the module build directory and is not tracked by Git.

## Build the compatibility distribution

Preferred deployable Web artifact:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

The generated compatibility distribution is expected under:

```text
webApp/build/dist/composeWebCompatibility/productionExecutable/
```

The release workflow packages this directory into:

```text
rps-arena-web.zip
```

## Host page

`webApp/src/webMain/resources/index.html` provides:

- UTF-8 metadata;
- responsive viewport metadata;
- RPS Arena title/description/theme color;
- full-window layout CSS;
- the `#webApp` Compose host container;
- a `noscript` fallback message.

Keep application behavior in Kotlin rather than embedding duplicated product logic into the HTML shell.

## CI

Primary CI runs:

```bash
gradle :webApp:composeCompatibilityBrowserDistribution --stacktrace
```

This verifies that both Web targets and the compatibility packaging path remain buildable together.

## Release workflow

The release job:

1. builds the JS+Wasm compatibility distribution;
2. packages it as `rps-arena-web.zip`;
3. includes it in tagged GitHub Release artifacts;
4. includes the ZIP in SHA-256 checksum generation.

The ZIP can be deployed to a static web host that serves the generated files correctly.

## Networking/privacy boundary

The Web runtime itself is loaded from the hosting origin, but RPS Arena product behavior remains offline-first after assets are available. The project still contains no account, ads, analytics, cloud model, or mandatory gameplay API.

Do not add remote telemetry or gameplay services merely because a browser environment is available.

## Browser limitations to test

Web is a distinct runtime and should be tested for:

- browser resize behavior;
- keyboard focus/navigation;
- pointer/touch input;
- text input/IME behavior;
- Hindi rendering;
- browser zoom/text scaling;
- localStorage persistence across reload/restart;
- storage-disabled/private browsing behavior;
- JS fallback startup;
- Wasm startup;
- backup export/import copy/paste flow.

## Generated files and caches

Do not commit:

```text
node_modules/
kotlin-js-store/
webApp/build/
```

The root `.gitignore` excludes these generated paths.

## Adding Web-specific APIs

Prefer shared code. Put code under `webMain` only when it needs browser APIs such as:

- clipboard integration;
- downloads/file APIs;
- browser share APIs;
- platform storage;
- future optional local-network/browser transport.

Avoid DOM/JS calls inside `commonMain`.

## Web change checklist

1. run repository source checks;
2. run shared tests;
3. build `composeCompatibilityBrowserDistribution`;
4. test the Wasm development target;
5. test the JS development target when compatibility behavior changes;
6. verify localStorage persistence;
7. resize across phone/tablet/desktop browser dimensions;
8. check keyboard/touch accessibility;
9. update this guide/release notes/file reference when platform files change;
10. keep browser-specific behavior outside shared domain rules.
