# Design System

> Version: 1.0
>
> This document defines the visual identity and design foundations for all internal software, including Android applications, web applications, dashboards, and future employee-facing tools.
>
> This is **not** a product packaging or marketing guide.

---

# Design Principles

## Trustworthy

The interface should communicate reliability and professionalism rather than luxury or entertainment.

## Calm

Users spend many hours inside these applications.

Colors should reduce fatigue rather than attract attention.

## Functional

The UI exists to help employees complete work efficiently.

Information always has higher priority than decoration.

## Consistent

Android, Web, Desktop and future platforms should feel like the same product family.

---

# Brand Identity

## Logo

- Stylized stroked **F**
- Circular outline
- No text inside application icons
- Optional company name or tagline only on splash screens, websites and large branding

---

# Brand Colors

## Brand Orange

Primary identity color.

```text
#F59E25
```

Used for:

- Logo
- Primary CTA
- Selected navigation
- Progress
- Focus states
- Important highlights

Avoid using orange for:

- Large backgrounds
- Cards
- Entire pages
- Status colors

Orange is an identity color, not a layout color.

---

## Brand Purple

Primary interface color.

```text
#4A347A
```

Represents:

- Brand
- Navigation
- Identity
- Application chrome

---

# Color Palette

## Brand

| Token | Value |
|--------|---------|
| Brand 900 | #231936 |
| Brand 800 | #332551 |
| Brand 700 | #39285F |
| Brand 600 | #4A347A |
| Brand 500 | #5A448A |
| Brand 400 | #715FB3 |
| Brand 300 | #9383C9 |
| Brand 200 | #D9D3EE |
| Brand 100 | #F3F1FA |

Brand 600 is the canonical brand color.

---

## Neutrals

| Token | Value |
|--------|---------|
| Background | #201A2E |
| Surface | #2A233A |
| Surface Elevated | #342C47 |
| Surface Variant | #433A59 |
| Border | #4B4560 |
| Divider | #5B5572 |

---

## Text

| Token | Value |
|--------|---------|
| Primary | #FCFAF7 |
| Secondary | #C9C4D9 |
| Tertiary | #9B95AD |
| Disabled | #7F7991 |

Avoid using pure white (#FFFFFF) except where maximum contrast is required.

---

# Semantic Colors

Semantic colors must remain independent from brand colors.

| Purpose | Color |
|----------|---------|
| Success | #22C55E |
| Warning | #EAB308 |
| Error | #EF4444 |
| Info | #3B82F6 |

Never use Brand Orange as a warning color.

---

# App Icon

## Logo

Orange stroked "F"

```text
#F59E25
```

---

## Background

Radial gradient.

```text
Center
#5A448A

↓

Middle
#4A347A

↓

Edges
#39285F
```

Avoid:

- mesh gradients
- glossy effects
- glass effects
- heavy textures

The icon should remain recognizable at launcher sizes.

---

# Application Background

Preferred background:

```text
Top Left
#5A448A

↓

Center
#4A347A

↓

Bottom Right
#332551
```

The gradient should be subtle and should never distract from content.

---

# Environment Badges

Production applications have **no badge**.

## Development

Background

```text
#2563EB
```

Text

```
DEV
```

---

## Preview / Staging

Background

```text
#14B8A6
```

Text

```
PREVIEW
```

---

Badges should appear in the lower-right corner of the launcher icon.

---

# Design Tokens

Raw colors should never be referenced directly by components.

Components should consume semantic tokens only.

Example:

```text
color.brand.primary

color.background.default
color.background.surface
color.background.elevated

color.text.primary
color.text.secondary

color.border.default

color.icon.default
color.icon.active

color.state.success
color.state.warning
color.state.error
color.state.info
```

This allows future rebranding without component changes. A semantic token layer is a common best practice in mature design systems because it decouples implementation from raw color values. :contentReference[oaicite:0]{index=0}

---

# Icons

Style:

- Outline first
- Rounded corners
- Minimal detail
- Consistent optical weight
- Avoid filled icons unless emphasis is required

Recommended stroke:

- Android: 2dp
- Web (24px): 2px

---

# Elevation

Use elevation sparingly.

Preferred hierarchy:

Background

↓

Surface

↓

Elevated Surface

↓

Dialog

↓

Modal

Avoid excessive shadows.

Prefer contrast between surfaces instead.

---

# Corner Radius

| Component | Radius |
|------------|--------|
| Chips | 9999 |
| Buttons | 12 |
| Cards | 16 |
| Dialogs | 24 |
| Sheets | 28 |

Maintain consistent radii across all platforms.

---

# Motion

Motion should feel deliberate.

Prefer:

- fade
- scale
- slide

Avoid:

- bounce
- elastic animations
- flashy transitions

Duration:

- Fast: 150ms
- Standard: 250ms
- Large transitions: 350ms

---

# Cross-Platform Guidelines

## Android

- Use Material 3.
- Map Material color roles to semantic tokens.
- Do not expose raw hex values in composables.
- Support dynamic type and accessibility settings.

## Web

- Use CSS custom properties generated from the same token source.
- Avoid hardcoded colors in components.
- Use semantic variables (for example, `--color-background-surface`) rather than raw hex values.

Design systems from organizations such as CMS, USWDS, and Dell similarly recommend separating primitive colors from semantic tokens so the same design language can be shared across platforms. :contentReference[oaicite:1]{index=1}

---

# Accessibility

- Meet WCAG AA contrast at minimum.
- Never rely on color alone to communicate status.
- Ensure keyboard focus is always visible.
- Use semantic icons alongside status colors.
- Test dark mode contrast before release.

---

# Future Scope

This document intentionally excludes:

- Typography
- Component library
- Spacing system
- Illustrations
- Charts
- Data visualization
- Marketing assets

These will be documented separately.
