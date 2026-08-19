# Accessibility

Accessibility is part of RPS Arena's release definition, not optional polish.

## Current baseline

- Material 3 buttons, chips, switches, and text fields use platform-compatible focus/touch semantics.
- Gesture buttons are 88 dp high and expose explicit descriptions such as `Choose Rock` instead of relying on emoji alone.
- Round outcomes, scores, timer state, and local-player turn state are written as text.
- Light, dark, and system theme preferences are supported.
- A reduced-motion preference is persisted. The current audited gameplay UI intentionally avoids non-essential animation.
- Narrow groups of configuration chips scroll horizontally instead of clipping off-screen controls.
- Destructive full-data reset requires confirmation.

## Keyboard

Desktop primary journeys must be usable with normal keyboard focus traversal. New custom components must not remove keyboard semantics without providing an equivalent accessible action.

Check:

- onboarding completion;
- Home navigation;
- game configuration;
- all gesture choices;
- match restart;
- History clear;
- Settings switches and backup actions;
- About links.

## Screen readers

Emoji and icons cannot be the only source of interaction meaning. Interactive gesture choices have explicit semantic descriptions. New icon-only controls must supply meaningful labels.

## Text scaling

Avoid fixed-height text containers except controls whose layout has been verified with larger fonts. Important content lives in scrollable screens where necessary so increased text size does not make actions unreachable.

## Motion

Future animations must:

- be non-essential to understanding state;
- respect `reducedMotion`;
- avoid forced flashing, shaking, or repeated celebratory effects;
- have an equivalent static status representation.

## Contrast and status

Use Material theme color roles rather than hard-coded text/status colors. Winners, errors, timers, locks, and warnings must include text or symbols so color is never the only signal.

## Manual release checklist

- [ ] Navigate all primary desktop flows using the keyboard.
- [ ] Verify gesture descriptions with a screen reader/accessibility inspector.
- [ ] Test large text/scaling where supported.
- [ ] Check light and dark themes for readable contrast.
- [ ] Confirm narrow Android layouts do not hide critical controls.
- [ ] Confirm reduced-motion preference is respected by any animation added since the last release.
- [ ] Confirm errors and destructive confirmations are understandable without color.
- [ ] Confirm touch targets remain comfortable on Android.

Accessibility regressions are product defects and should receive automated coverage when the Compose/platform test stack can represent them reliably.
