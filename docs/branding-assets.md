# Branding and Visual Asset Reference

This guide documents the repository-owned visual assets and explains their relationship to Android launcher resources and the shared Compose theme. It prevents rebranding work from updating only one surface and leaving the rest inconsistent.

## Tracked brand artwork

```text
assets/logo.svg
assets/splash.svg
```

Android launcher resources are separate:

```text
androidApp/src/main/res/drawable/ic_launcher_foreground.xml
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
androidApp/src/main/res/values/colors.xml
```

Shared app colors are separate again:

```text
shared/src/commonMain/kotlin/in/sanskar/rpsarena/ui/ArenaTheme.kt
```

There is no automatic generation pipeline linking these files.

## `assets/logo.svg`

The root logo SVG uses:

- 1024 x 1024 viewBox;
- purple `#6750A4` rounded-square field;
- white circular arena disc;
- purple horizontal bars;
- accessible SVG `<title>` / `<desc>` elements.

Primary current usage:

- README presentation;
- repository/public project artwork source.

It is editable vector source, not an Android adaptive-icon resource by itself.

## `assets/splash.svg`

The splash artwork uses:

- 1600 x 900 viewBox;
- purple background;
- white central circle;
- Rock/Paper/Scissors emoji line;
- `RPS Arena` title;
- `Made by the Sanskar` credit;
- accessible SVG title/description.

Primary current usage is repository/documentation artwork.

The Android app does not currently reference this SVG as a native startup splash screen.

## Android launcher foreground

`ic_launcher_foreground.xml` is an Android vector drawable.

It approximates the same visual language using Android path data:

- purple field;
- white circle;
- purple bars.

Android vector XML and SVG are different formats. Do not paste arbitrary SVG markup into an Android vector resource.

## Adaptive icon background

`colors.xml` defines:

```text
ic_launcher_background = #6750A4
```

Both adaptive icon XML files reference that color resource.

## Adaptive icon files

`ic_launcher.xml` and `ic_launcher_round.xml` currently contain the same two layers:

```text
background -> @color/ic_launcher_background
foreground -> @drawable/ic_launcher_foreground
```

Android launcher masks determine final adaptive icon silhouette.

Do not bake a circular/rounded mask into the foreground in a way that conflicts with adaptive-icon safe-zone behavior without testing on several launcher masks.

## Shared Compose theme

`ArenaTheme.kt` defines application light/dark color schemes. It uses a branded purple family but is not required to use the exact same single purple value everywhere.

Brand asset palette and UI accessibility palette have different responsibilities:

- logo needs recognition/consistency;
- UI colors must satisfy contrast/readability/state requirements.

Do not force every surface to `#6750A4` if it harms dark-theme contrast.

## Android platform theme

`themes.xml` supplies an Android window shell accent/status/navigation configuration.

The Compose shared theme owns most in-app surfaces after `setContent` begins.

Therefore a complete brand-color change may require reviewing:

- root SVGs;
- Android adaptive icon foreground/background;
- Android platform accent/system-bar design;
- shared Compose light/dark schemes;
- badges/screenshots/docs.

## README preview policy

README currently uses source-controlled SVG artwork rather than fabricated device screenshots.

Real screenshots should be captured from actual builds/devices/emulators when added.

Do not label a mockup as a real app screenshot.

## Accessibility in SVG

Keep meaningful:

```xml
role="img"
aria-labelledby="title desc"
<title ...>
<desc ...>
```

when the SVG is intended to be interpreted as an image in accessible contexts.

Descriptions should explain the image, not stuff keywords.

## Emoji rendering caveat

`splash.svg` contains emoji text. Emoji appearance can depend on renderer/font availability.

For a production marketing export requiring pixel-identical output, render/verify the SVG in the intended export tool and platform. Do not assume every SVG renderer embeds the same emoji artwork.

## Rebranding checklist

If project name/logo/palette changes:

1. update `assets/logo.svg`;
2. update `assets/splash.svg`;
3. update Android launcher vector/background;
4. inspect both adaptive icon resources;
5. review `ArenaTheme.kt` light/dark colors;
6. update Android label/window theme when needed;
7. update desktop window/package name when project name changes;
8. update README title/alt text;
9. update About/UI copy catalogs;
10. update package/repository descriptions only if identity changed;
11. regenerate real screenshots if they show old branding;
12. test icon masks, dark/light UI contrast, text scaling;
13. update changelog for user-visible rebranding.

## What should not change casually

Changing branding does not require changing:

- Android `applicationId`;
- Kotlin package names;
- persistence keys;
- GitHub repository URL;
- MIT license.

Those are technical/legal identities with separate compatibility implications.

## Exported binary assets

Generated PNG/ICO/installer assets should be treated as derived artifacts unless there is a clear source-control need.

`.gitattributes` marks PNG/ICO as binary, and `.gitignore` excludes common generated installers.

Keep editable/source artwork whenever practical.

## Brand/contact strings

Current brand/contact values include:

```text
RPS Arena
Made by the Sanskar
https://github.com/sanskarIN/rps-arena
https://buymeacoffee.com/sanskarIN
```

These are intentionally kept as brand/identity values rather than ordinary localized prose.

When updating contact/funding links, review:

- README;
- About screen;
- SUPPORT;
- FUNDING config;
- documentation file reference/branding docs.

## Asset change validation

For Android icon/resource edits:

```bash
gradle :androidApp:lintDebug --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

For shared theme edits:

```bash
gradle :shared:desktopTest --stacktrace
gradle :desktopApp:classes --stacktrace
gradle :androidApp:assembleDebug --stacktrace
```

Manual visual review is still necessary because compilation cannot judge brand quality or contrast alone.
