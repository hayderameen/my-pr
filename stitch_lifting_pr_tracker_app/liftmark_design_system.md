---
name: "LiftMark"
description: "Crisp, high-contrast athletic design system built for fast, tactile weight-lifting PR tracking on mobile."
---

## Overview

LiftMark is designed for lifters mid-session: sweaty hands, elevated heart rate, and zero patience for nested menus or visual clutter. The interface pairs a cool chalk-white canvas with deep slate ink and a high-energy Signal Vermilion accent that marks active sets, sliders, and confirmed entries. Every interaction is built around large touch targets, immediate numeric feedback, and frictionless transitions between picking a training day, selecting an exercise, and dialing in weight and reps.

## Colors

The palette uses **Signal Vermilion** (`primary`) for primary actions, active workout states, and tactile slider thumbs, grounded against **Cool Chalk** (`surface`) and **Deep Slate** (`on-surface`) for maximum legibility under bright gym lighting. **Cobalt** (`tertiary`) is reserved for secondary informational indicators.

- **Primary:** High-energy Signal Vermilion for primary CTAs, active slider tracks, and active workout indicators.
- **Surface Hierarchy:** Pure white (`surface-container-lowest`) cards sit over cool chalk (`surface`) and soft slate-tinted containers (`surface-container-low`, `surface-container`) for clear separation without heavy borders.

```yaml
colors:
  light:
    primary: "#B22200"
    on-primary: "#FFFFFF"
    primary-container: "#D93814"
    on-primary-container: "#FFFCFF"
    secondary: "#A0402A"
    on-secondary: "#FFFFFF"
    secondary-container: "#FE876C"
    on-secondary-container: "#741F0D"
    tertiary: "#005CAF"
    on-tertiary: "#FFFFFF"
    tertiary-container: "#0074DB"
    on-tertiary-container: "#FEFCFF"
    error: "#BA1A1A"
    on-error: "#FFFFFF"
    error-container: "#FFDAD6"
    on-error-container: "#93000A"
    background: "#F8F9FF"
    on-background: "#0B1C30"
    surface: "#F8F9FF"
    surface-dim: "#CBDBF5"
    surface-bright: "#F8F9FF"
    surface-container-lowest: "#FFFFFF"
    surface-container-low: "#EFF4FF"
    surface-container: "#E5EEFF"
    surface-container-high: "#DCE9FF"
    surface-container-highest: "#D3E4FE"
    on-surface: "#0B1C30"
    surface-variant: "#FFDAD2"
    on-surface-variant: "#5B403A"
    outline: "#907068"
    outline-variant: "#E4BEB5"
    inverse-surface: "#213145"
    inverse-on-surface: "#EAF1FF"
    inverse-primary: "#FFB4A3"
    shadow: "#000000"
    scrim: "#000000"
  dark:
    primary: "#FFB4A3"
    on-primary: "#630F00"
    primary-container: "#D93814"
    on-primary-container: "#FFFCFF"
    secondary: "#FFB4A3"
    on-secondary: "#611202"
    secondary-container: "#842B17"
    on-secondary-container: "#FFA08A"
    tertiary: "#A8C8FF"
    on-tertiary: "#003061"
    tertiary-container: "#0074DB"
    on-tertiary-container: "#FEFCFF"
    error: "#FFB4AB"
    on-error: "#690005"
    error-container: "#93000A"
    on-error-container: "#FFDAD6"
    background: "#031427"
    on-background: "#D3E4FE"
    surface: "#031427"
    surface-dim: "#031427"
    surface-bright: "#2A3A4F"
    surface-container-lowest: "#000F21"
    surface-container-low: "#0B1C30"
    surface-container: "#102034"
    surface-container-high: "#1B2B3F"
    surface-container-highest: "#26364A"
    on-surface: "#D3E4FE"
    surface-variant: "#5B403A"
    on-surface-variant: "#E4BEB5"
    outline: "#AB8981"
    outline-variant: "#5B403A"
    inverse-surface: "#D3E4FE"
    inverse-on-surface: "#213145"
    inverse-primary: "#B72300"
    shadow: "#000000"
    scrim: "#000000"
```

## Typography

LiftMark pairs **Afacad Flux** for bold, geometric headlines and workout titles with **Instrument Sans** for clean UI labels and exercise metadata, and **Spline Sans Mono** for tabular weight and rep readouts that never jitter while dragging a slider.

```yaml
typography:
  display-lg:
    fontFamily: "Afacad Flux"
    fontSize: 56px
    fontWeight: 700
    lineHeight: 60px
    letterSpacing: -1.5px
  display-lg-mobile:
    fontFamily: "Afacad Flux"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 36px
    letterSpacing: -0.75px
  headline-lg:
    fontFamily: "Afacad Flux"
    fontSize: 36px
    fontWeight: 700
    lineHeight: 42px
    letterSpacing: -0.5px
  headline-lg-mobile:
    fontFamily: "Afacad Flux"
    fontSize: 28px
    fontWeight: 700
    lineHeight: 34px
    letterSpacing: -0.4px
  headline-md:
    fontFamily: "Afacad Flux"
    fontSize: 24px
    fontWeight: 600
    lineHeight: 30px
    letterSpacing: -0.25px
  title-lg:
    fontFamily: "Afacad Flux"
    fontSize: 20px
    fontWeight: 600
    lineHeight: 26px
    letterSpacing: 0px
  title-md:
    fontFamily: "Instrument Sans"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 22px
    letterSpacing: 0px
  body-lg:
    fontFamily: "Instrument Sans"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
    letterSpacing: 0px
  body-md:
    fontFamily: "Instrument Sans"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0px
  label-lg:
    fontFamily: "Instrument Sans"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 18px
    letterSpacing: 0.1px
  label-sm:
    fontFamily: "Instrument Sans"
    fontSize: 12px
    fontWeight: 600
    lineHeight: 16px
    letterSpacing: 0.2px
  mono-metric:
    fontFamily: "Spline Sans Mono"
    fontSize: 48px
    fontWeight: 700
    lineHeight: 52px
    letterSpacing: -1px
  mono-metric-mobile:
    fontFamily: "Spline Sans Mono"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 36px
    letterSpacing: -0.75px
  mono-sm:
    fontFamily: "Spline Sans Mono"
    fontSize: 13px
    fontWeight: 500
    lineHeight: 18px
    letterSpacing: 0px
```

## Layout & Spacing

A mobile-first **Fluid Grid** built on a strict `4px` base step (`8px` primary rhythm) with `16px` horizontal page margins and `12px–16px` card gutters. Big workout-day tiles use a 2-column grid (`grid-cols-2`), exercise rows span full width for one-handed thumb reach, and the PR entry screen centers a single-column tactile control stack.

```yaml
spacing:
  gutter-xs: 4px
  gutter-sm: 8px
  gutter-md: 12px
  gutter-lg: 16px
  gutter-xl: 24px
  section: 32px
  page-margin: 16px
```

## Elevation & Depth

LiftMark uses **Tonal Layers** paired with **Low-Contrast Outlines** (`outline-variant`) rather than heavy drop shadows. Interactive tiles sit on `surface-container-lowest` (`#FFFFFF`) over a cool `surface` (`#F8F9FF`) canvas, with subtle `1px` borders to define hit areas cleanly. Active workout banners and primary CTAs step up via high-contrast fills (`primary` or `inverse-surface`).

## Shapes

Generous, athletic rounded corners (`16px`–`24px`) give workout-day boxes and exercise cards a tactile, approachable feel, while slider tracks and action pills use `full` (`9999px`) pill geometry.

```yaml
rounded:
  sm: 8px
  DEFAULT: 12px
  md: 16px
  lg: 20px
  xl: 24px
  full: 9999px
```

## Motion & Micro-interaction

Motion is crisp, mechanical, and zero-lag. Button presses and slider ticks respond within `80ms` using `transform` and `color` only, while screen transitions decelerate cleanly in `200ms`. Nothing loops or bounces unnecessarily.

```yaml
motion:
  tick-feedback:
    duration: 80ms
    delay: 0ms
    timingFunction: ease-out
  state-change:
    duration: 120ms
    delay: 0ms
    timingFunction: ease-out
  surface-enter:
    duration: 200ms
    delay: 0ms
    timingFunction: cubic-bezier(0.2, 0, 0, 1)
```

## Voice & Content Strategy

- **Tone of voice:** Direct, encouraging, and zero-fluff. Use short athletic phrasing ("Log Set", "End Workout", "Push Day") instead of wordy instructions.
- **Capitalisation:** Sentence case for body copy and secondary labels; Title Case for workout day names, exercise names, and primary action buttons.
- **Numbers & units:** Always pair numeric values with explicit units (`kg` or `lbs`, `reps`) using tabular monospace figures so digits stay locked in place as sliders move.

## Components

Component implementations live as individual HTML sticker sheet files in
`components/`. Each file contains the canonical, styled HTML components with
`<!-- COMPONENT: name -->` comment markers. Read each file before use.

- **Button** → `@components/button.html`: `button-primary`, `button-icon`, `button-with-icon`
- **Card** → `@components/card.html`: `card-elevated`, `card-action`, `card-interactive`, `card-interactive-2`
- **List** → `@components/list.html`: `list-single`, `list-two-line`, `list-leading-icon`, `list-trailing-action`, `list-interactive`, `list-interactive-2`
- **Slider** → `@components/slider.html`: `slider-continuous`, `slider-discrete`, `slider-range`
- **Typography** → `@components/typography.html`: `typography-display`, `typography-body`, `typography-label`
