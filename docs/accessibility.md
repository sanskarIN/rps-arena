# Accessibility Guide

RPS Arena treats accessibility as a product requirement across Android and desktop.

## Current baseline

- Core actions use visible text labels instead of icon-only controls.
- Gesture buttons use large touch targets and combine emoji with text so meaning is not color-only.
- Score, timer, win/loss/draw results, and timeout states are represented in text.
- Light, dark, and system theme modes are available.
- Reduced motion disables the animated result transition.
- Round timers can be turned off entirely.
- Local two-player mode shows an explicit turn message before each selection.
- Keyboard navigation follows standard Compose focus order on desktop.
- English and Hindi core UI catalogs are available.

## Manual review checklist

Before a release:

1. Navigate every primary screen with keyboard only on desktop.
2. Verify visible focus movement and that no essential control is unreachable.
3. Test Android TalkBack through onboarding, starting a match, choosing a gesture, Settings, backup, and reset confirmation.
4. Increase platform text scaling and verify important actions remain readable and operable.
5. Check light and dark themes for readable contrast.
6. Enable reduced motion and verify result changes remain understandable without animation.
7. Enable a round timer and verify remaining time and timeout outcome are visible as text.
8. Verify results are understandable without relying on color.
9. Verify destructive reset requires confirmation.
10. Repeat the primary flow in both shipped languages and record untranslated copy as a defect.

## Motion policy

Animation must clarify state changes, not delay interaction. Reduced-motion mode renders result content without the animated crossfade. Do not add decorative looping animations to core gameplay.

## Timer policy

Timed rounds are opt-in. Players who need more time can select 30 or 60 seconds, or disable the timer. Timeout outcomes are recorded explicitly in history so users can distinguish them from gesture outcomes.

## Reporting accessibility problems

Use the repository issue tracker or email `supportramsandesh@gmail.com`. Include platform, assistive technology, app version, exact screen, and steps to reproduce. Do not include private data or credentials.
