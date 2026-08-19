# Accessibility

Accessibility is a release requirement for RPS Arena, not an optional polish pass.

## Implemented baseline

- Material 3 controls with minimum gesture-choice height of 56 dp.
- Semantic content descriptions on game gesture controls.
- Text/symbol result descriptions instead of color-only winner states.
- Light, dark, and system themes.
- Reduced-motion preference that bypasses animated round-result transitions.
- Responsive mobile and desktop navigation.
- Descriptive labels for settings and destructive reset confirmation.

## Desktop keyboard expectations

Compose controls should remain reachable using standard keyboard focus traversal. New custom components must not remove default keyboard semantics unless an equivalent accessible interaction is supplied.

## Screen readers

Interactive icons or symbols that are not self-explanatory require semantic labels. Decorative symbols should not be the only source of meaning.

For gesture buttons, accessibility output should identify the action, such as “Choose Rock,” rather than reading only an emoji.

## Text scaling

Avoid fixed-height text containers. Test common screens with larger system font settings and keep important controls reachable through scrolling where necessary.

## Motion

When reduced motion is enabled:

- avoid result crossfades or movement animations;
- do not introduce celebratory motion that cannot be disabled;
- never rely on animation to communicate state.

## Contrast and theme

Use Material theme roles rather than hard-coded text colors inside screens. Verify both light and dark themes manually before release.

## Release checklist

- [ ] Navigate primary flows with keyboard on desktop.
- [ ] Check screen-reader labels for gesture choices and settings.
- [ ] Test 200% text scaling where supported.
- [ ] Confirm reduced motion removes non-essential animations.
- [ ] Confirm errors, warnings, winners, and locked states include text or symbols.
- [ ] Confirm touch targets remain comfortable on Android.

Accessibility regressions should be treated as product defects and receive tests where the platform test stack can represent them reliably.
