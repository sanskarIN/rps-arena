# Accessibility

Accessibility is part of RPS Arena's release definition, not optional polish.

## Current baseline

- Material 3 buttons, chips, switches, and text fields use platform-compatible focus/touch semantics.
- Gesture buttons are 88 dp high and expose explicit descriptions such as `Choose Rock` instead of relying on emoji alone.
- Round outcomes, scores, timer state, and local-player turn state are written as text.
- Completed rounds expose a text-labeled `Copy result` action and a textual copied-success state.
- Recent trends use W/L/D text, a written legend, and semantic descriptions such as `Recent result 1: Win`; they do not depend on color.
- Recent trend values are non-interactive status surfaces rather than controls that falsely advertise click behavior.
- Local profile selection uses visible display names and an explicit `active` label.
- Backup preview is textual and import remains disabled until the pasted backup validates.
- Light, dark, and system theme preferences are supported.
- A reduced-motion preference is persisted. It bypasses the animated round-result transition and renders the same result directly.
- Narrow groups of configuration/profile/trend chips scroll horizontally instead of clipping off-screen controls.
- Main content is bounded on wide desktop windows while retaining fill-width behavior on narrow layouts.
- Destructive full-data reset requires confirmation.
- Recent-history clear has a one-step undo until new history is written.
- The primary shared Compose UI journey has a semantic-tag regression test from onboarding through the first rendered round result.

## Keyboard

Desktop primary journeys must be usable with normal keyboard focus traversal. New custom components must not remove keyboard semantics without providing an equivalent accessible action.

Check:

- onboarding completion;
- Home navigation;
- local profile create/rename/select/delete;
- game configuration;
- all gesture choices;
- completed-round Copy result;
- match restart;
- History clear and undo;
- Settings switches, backup preview/import, and reset actions;
- Statistics trend status/labels;
- About links.

## Screen readers

Emoji and icons cannot be the only source of interaction meaning. Interactive gesture choices have explicit semantic descriptions. Trend entries expose full Win/Loss/Draw descriptions rather than single-letter meaning alone. New icon-only controls must supply meaningful labels.

Achievement lock/trophy symbols are accompanied by achievement title and description text. The copied-result state is also text, so clipboard success does not depend on a transient visual-only effect.

## Text scaling

Avoid fixed-height text containers except controls whose layout has been verified with larger fonts. Important content lives in scrollable screens where necessary so increased text size does not make actions unreachable.

Profile names are bounded to 24 characters and score labels are display-bounded to reduce layout breakage. Full names remain visible in profile management/history where space permits.

## Motion

Animations must:

- be non-essential to understanding state;
- respect `reducedMotion`;
- avoid forced flashing, shaking, or repeated celebratory effects;
- have an equivalent static status representation.

Current behavior uses `AnimatedContent` only for the round-result transition when reduced motion is disabled. With reduced motion enabled, the result card is rendered without that transition.

## Contrast and status

Use Material theme color roles rather than hard-coded text/status colors. Winners, errors, timers, locks, warnings, profile selection, copy confirmation, and trend results must include text or symbols so color is never the only signal.

## Automated accessibility-relevant coverage

`RpsArenaUiTest` uses stable semantic test tags to verify that the first-run user can:

1. find and activate the onboarding action;
2. reach Home;
3. open Play;
4. find and activate a gesture control;
5. reach the rendered result state.

This does not replace manual keyboard/screen-reader review, but it protects the primary semantic interaction path from accidental navigation/control regressions.

## Manual release checklist

- [ ] Navigate all primary desktop flows using the keyboard.
- [ ] Verify gesture descriptions with a screen reader/accessibility inspector.
- [ ] Verify Copy result is reachable, labeled, and announces/visibly shows the copied-success state.
- [ ] Verify W/L/D trend items announce Win/Loss/Draw in order without presenting themselves as buttons.
- [ ] Verify active-profile selection is understandable without color.
- [ ] Verify backup validation/preview state and disabled import state are understandable.
- [ ] Test large text/scaling where supported.
- [ ] Check light and dark themes for readable contrast.
- [ ] Confirm narrow Android layouts do not hide critical controls.
- [ ] Confirm wide desktop layouts remain readable and do not stretch primary content excessively.
- [ ] Confirm reduced-motion preference removes the round-result transition.
- [ ] Confirm errors, undo actions, and destructive confirmations are understandable without color.
- [ ] Confirm touch targets remain comfortable on Android.

Accessibility regressions are product defects and should receive automated coverage when the Compose/platform test stack can represent them reliably.
