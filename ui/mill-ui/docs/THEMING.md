# Color Theme System

## Overview

The application supports multiple color themes that can be independently selected for light and dark modes. Each theme defines three 10-shade palettes — **accent**, **neutrals**, and **darks** — which are wired into Mantine's color system at build time and overridden per color scheme via CSS custom properties.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Theme Data Model](#theme-data-model)
3. [How Palettes Map to Mantine](#how-palettes-map-to-mantine)
4. [CSS Variables Resolver](#css-variables-resolver)
5. [Color Token Reference](#color-token-reference)
6. [Provider Hierarchy](#provider-hierarchy)
7. [Persistence](#persistence)
8. [Built-in Themes](#built-in-themes)
9. [Creating a New Theme](#creating-a-new-theme)
10. [Generating Tinted Palettes](#generating-tinted-palettes)
11. [Component Authoring Rules](#component-authoring-rules)
12. [Semantic vs Explicit Tokens](#semantic-vs-explicit-tokens)
13. [File Reference](#file-reference)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│  themes.ts                                                          │
│  ┌──────────────────┐  ┌──────────────────┐                        │
│  │ lightThemes[]    │  │ darkThemes[]     │  ColorTheme objects     │
│  │  - Classic       │  │  - Classic       │  with colors, neutrals, │
│  │  - Ocean         │  │  - Neon          │  darks palettes         │
│  │  - Forest        │  │  - Aurora        │                        │
│  │  - Lavender      │  │  - Nebula        │                        │
│  │  - Sunset        │  │  - Ember         │                        │
│  └────────┬─────────┘  └────────┬─────────┘                        │
│           │                     │                                   │
│           └──────────┬──────────┘                                   │
└──────────────────────┼──────────────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────────────┐
│  ThemeContext.tsx                                                     │
│  ColorThemeProvider — React context + localStorage persistence       │
│  useColorTheme() → { lightTheme, darkTheme, setLightTheme, ... }    │
└──────────────────────┬───────────────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────────────┐
│  theme.ts                                                            │
│  buildTheme(lightTheme, darkTheme) → { theme, resolver }            │
│                                                                      │
│  Produces:                                                           │
│  ├── theme    → Mantine createTheme() object (colors, fonts, radii) │
│  └── resolver → CSSVariablesResolver (gray/dark overrides per mode) │
└──────────────────────┬───────────────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────────────┐
│  App.tsx — ThemedApp component                                       │
│                                                                      │
│  <MantineProvider                                                    │
│    theme={mantineTheme}                                              │
│    cssVariablesResolver={cssVariablesResolver}                        │
│    defaultColorScheme="auto"                                         │
│  />                                                                  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Theme Data Model

Each theme is a `ColorTheme` object defined in `src/theme/themes.ts`:

```typescript
interface ColorTheme {
  id: string;                    // Unique key, e.g. 'ocean', 'classic'
  name: string;                  // Display name, e.g. 'Ocean', 'Classic'
  mode: 'light' | 'dark';       // Which mode this theme is for
  swatch: string;                // Preview color (hex), e.g. '#14b8a6'
  colors: MantineColorsTuple;    // 10-shade accent palette
  neutrals: MantineColorsTuple;  // 10-shade neutral/gray palette
  darks: MantineColorsTuple;     // 10-shade dark-mode background palette
}
```

**`MantineColorsTuple`** is a fixed-length array of 10 hex strings, ordered lightest (index 0) to darkest (index 9).

### The Three Palettes

| Palette      | Purpose                             | Used in light mode                  | Used in dark mode                         |
|-------------|-------------------------------------|-------------------------------------|-------------------------------------------|
| **colors**  | Brand / accent color                | Mapped to `teal` slot               | Mapped to `cyan` slot                     |
| **neutrals**| Grays for text, borders, surfaces   | Mapped to `gray` + `slate` slots    | Overrides `gray` via CSS vars             |
| **darks**   | Dark-mode backgrounds / surfaces    | Mapped to `dark` slot (rarely used) | Overrides `dark` via CSS vars             |

---

## How Palettes Map to Mantine

`buildTheme()` in `src/theme/theme.ts` assembles a Mantine theme from two `ColorTheme` objects — one for light mode, one for dark mode.

### Slot Mapping

| Mantine color slot | Source                        | Notes                                                    |
|-------------------|-------------------------------|----------------------------------------------------------|
| `teal`            | `lightTheme.colors`           | Primary accent for light mode                             |
| `cyan`            | `darkTheme.colors`            | Primary accent for dark mode                              |
| `gray`            | `lightTheme.neutrals`         | Default neutrals; overridden in dark mode by CSS vars     |
| `dark`            | `darkTheme.darks`             | Dark-mode backgrounds; overridden by CSS vars             |
| `slate`           | `lightTheme.neutrals`         | Kept for any explicit `color="slate"` usage               |

### Why Two Mechanisms?

Mantine's `createTheme()` sets color slots **globally** — the same `gray` values apply in both light and dark mode. To give each mode its own neutral palette, the `cssVariablesResolver` overrides the CSS custom properties per color scheme.

---

## CSS Variables Resolver

The resolver returned by `buildTheme()` is passed to `<MantineProvider cssVariablesResolver={resolver}>`. It produces per-scheme overrides:

### Light Mode Overrides

| Variable                    | Value                     | Purpose                               |
|----------------------------|---------------------------|---------------------------------------|
| `--mantine-color-body`     | `lightTheme.neutrals[0]`  | Tinted body background (not pure `#fff`) |

### Dark Mode Overrides

| Variable pattern               | Value                     | Purpose                                  |
|-------------------------------|---------------------------|------------------------------------------|
| `--mantine-color-gray-0..9`   | `darkTheme.neutrals[0..9]`| Override neutrals for dark mode           |
| `--mantine-color-dark-0..9`   | `darkTheme.darks[0..9]`   | Override backgrounds/surfaces for dark    |

This means:
- In **light mode**, `var(--mantine-color-gray-4)` resolves to `lightTheme.neutrals[4]`.
- In **dark mode**, `var(--mantine-color-gray-4)` resolves to `darkTheme.neutrals[4]`.
- The transition is fully automatic — components just reference `gray` and it works in both modes.

---

## Color Token Reference

### Semantic Tokens (prefer these)

These resolve automatically per color scheme — no `isDark` ternary needed:

| Token                                    | Light mode resolves to                | Dark mode resolves to          | Usage                          |
|-----------------------------------------|---------------------------------------|-------------------------------|--------------------------------|
| `var(--mantine-color-body)`              | `neutrals[0]` (tinted off-white)      | `dark[7]`                      | Main content background        |
| `var(--mantine-color-text)`              | Mantine auto (near-black)             | Mantine auto (near-white)      | Primary text color             |
| `var(--mantine-color-default-border)`    | Mantine auto (`gray.3`)               | Mantine auto (`dark.4`)        | Standard border color          |
| `c="dimmed"`                             | Mantine auto                          | Mantine auto                   | Secondary/muted text           |

### Explicit Color Tokens (require `isDark` ternary)

For cases where semantic tokens aren't sufficient:

| Category            | Light mode                       | Dark mode                          | Pattern                                 |
|--------------------|----------------------------------|------------------------------------|-----------------------------------------|
| **Accent**          | `teal` / `teal.6`               | `cyan` / `cyan.6`                  | `isDark ? 'cyan' : 'teal'`             |
| **Accent icon**     | `var(--mantine-color-teal-6)`    | `var(--mantine-color-cyan-4)`      | Brighter shade for dark bg             |
| **Heading text**    | `gray.8`                         | `gray.1`                           | `c={isDark ? 'gray.1' : 'gray.8'}`    |
| **Body text**       | `gray.7`                         | `gray.2`                           | `c={isDark ? 'gray.2' : 'gray.7'}`    |
| **Muted text**      | `gray.5`                         | `gray.4`                           | `c={isDark ? 'gray.4' : 'gray.5'}`    |
| **Muted icon**      | `gray.6`                         | `gray.4`                           | `color={isDark ? 'gray.4' : 'gray.6'}`|
| **Surface bg**      | `var(--mantine-color-gray-0)`    | `var(--mantine-color-dark-6)`      | Card, assistant bubble                 |
| **Sidebar bg**      | `var(--mantine-color-gray-0)`    | `var(--mantine-color-dark-8)`      | Sidebars, headers                      |
| **Hover bg**        | `var(--mantine-color-gray-1)`    | `var(--mantine-color-dark-6)`      | List item hover                        |
| **Gradient (header)** | `teal-0 → body`               | `dark-8 → dark-7`                  | Section headers, dashboard             |
| **Popover border**  | `var(--mantine-color-gray-3)`    | `var(--mantine-color-gray-6)`      | Popover/dropdown borders               |

### Dark Palette Anatomy

The `darks` palette controls dark-mode surface hierarchy:

| Index    | Role                    | Classic value  | Description                              |
|---------|-------------------------|----------------|------------------------------------------|
| `dark.0` | Lightest text on dark   | `#b9c3cf`      | Rarely used directly                     |
| `dark.1` | Light text              | `#9da8b7`      |                                          |
| `dark.2` | Secondary text/outline  | `#8591a2`      | Active state outlines                    |
| `dark.3` | Muted elements          | `#556377`      |                                          |
| `dark.4` | **Borders**             | `#3d4d62`      | `--mantine-color-default-border` in dark |
| `dark.5` | Dark surface            | `#364559`      | OverviewDashboard border                 |
| `dark.6` | **Card / surface bg**   | `#334155`      | Assistant bubbles, cards, hover states   |
| `dark.7` | **Body background**     | `#1e293b`      | `--mantine-color-body` in dark mode      |
| `dark.8` | **Sidebar / header bg** | `#0f172a`      | Sidebar, gradient start                  |
| `dark.9` | Deepest background      | `#0a101f`      | Gradient endpoints                       |

### Feature Color Scheme

Non-neutral accent colors used for specific features:

| Feature                      | Color     | Usage                               |
|-----------------------------|-----------|-------------------------------------|
| Inline chat sessions        | cyan/teal | Active session indicator             |
| Related chats (references)  | violet    | Badge count, popover icons           |
| Related content             | indigo    | Link icon, badge count               |
| Model type badge            | teal      |                                     |
| Concept type badge          | grape     |                                     |
| Analysis type badge         | orange    |                                     |

---

## Provider Hierarchy

```
App
└── AuthContext.Provider
    └── ColorThemeProvider              ← theme selection + localStorage
        └── FeatureFlagProvider
            └── ThemedApp               ← buildTheme() + MantineProvider
                └── MantineProvider
                    ├── theme={mantineTheme}
                    ├── cssVariablesResolver={resolver}
                    └── defaultColorScheme="auto"
```

The `ColorThemeProvider` wraps `FeatureFlagProvider` and `MantineProvider`, so theme changes trigger a re-render of the entire Mantine context with the new palette.

---

## Persistence

Theme selections are persisted in `localStorage`:

| Key                      | Value            | Default     |
|-------------------------|------------------|-------------|
| `mill-ui-light-theme`   | Light theme ID   | `'classic'` |
| `mill-ui-dark-theme`    | Dark theme ID    | `'classic'` |

The `ColorThemeProvider` reads these on mount and writes on change. The color scheme itself (light/dark/auto) is managed by Mantine's built-in `ColorSchemeScript` + `defaultColorScheme="auto"`.

---

## Built-in Themes

### Light Themes

| ID         | Name     | Accent swatch | Body bg (light) | Tint | Character                           |
|-----------|----------|---------------|-----------------|------|--------------------------------------|
| `classic` | Classic  | `#14b8a6`     | `#f8fafc`       | —    | Original Tailwind slate (untinted)    |
| `ocean`   | Ocean    | `#14b8a6`     | `#f5fafb`       | 0.10 | Subtle teal wash                      |
| `forest`  | Forest   | `#16a34a`     | `#f5fafa`       | 0.10 | Subtle green wash                     |
| `lavender`| Lavender | `#8b5cf6`     | `#f6f6fc`       | 0.10 | Subtle purple wash                    |
| `sunset`  | Sunset   | `#f59e0b`     | `#f8f9fa`       | 0.04 | Near-neutral, accent-only identity    |

### Dark Themes

| ID         | Name     | Accent swatch | Body bg (dark)  | Tint | Character                            |
|-----------|----------|---------------|-----------------|------|---------------------------------------|
| `classic` | Classic  | `#22d3ee`     | `#1e293b`       | —    | Original Tailwind slate (untinted)     |
| `neon`    | Neon     | `#22d3ee`     | `#171e21`       | 0.15 | Subtle cyan-tinted surfaces            |
| `aurora`  | Aurora   | `#34d399`     | `#181e1e`       | 0.15 | Subtle green-tinted surfaces           |
| `nebula`  | Nebula   | `#c084fc`     | `#1a1722`       | 0.15 | Subtle purple-tinted surfaces          |
| `ember`   | Ember    | `#fb923c`     | `#1b1b1d`       | 0.05 | Near-neutral gray, accent-only identity|

Light and dark themes are independent — any light theme can be paired with any dark theme.

---

## Creating a New Theme

### Step-by-Step

#### 1. Choose an accent color

Pick a single hex color that represents the theme. This becomes the `swatch` and drives the 10-shade accent palette.

**Accent palette guidelines:**
- 10 shades from lightest (index 0, near-white tint) to darkest (index 9, deep shade)
- Index 5–6 is the "hero" shade — the main brand color
- Follow the Tailwind luminance curve for consistent contrast ratios
- Light themes use the palette as `teal`; dark themes use it as `cyan`

#### 2. Generate neutrals and darks

Run the palette generator to create tinted neutrals and darks:

```bash
npx tsx scripts/generate-neutrals.ts
```

Before running, add your theme to the `THEMES` array in the script:

```typescript
const THEMES: ThemeDef[] = [
  // ... existing themes ...
  { id: 'coral', mode: 'light', swatch: '#f43f5e' },                          // uses defaults
  { id: 'coral', mode: 'light', swatch: '#f43f5e', neutralsTint: 0.15 },      // stronger neutrals
  { id: 'coral', mode: 'light', swatch: '#f43f5e', neutralsTint: 0.04, darksTint: 0.05 }, // near-gray
];
```

The `neutralsTint` and `darksTint` fields are optional — omitted values fall back to `DEFAULT_TINT_NEUTRALS` (0.10) and `DEFAULT_TINT_DARKS` (0.15). The script outputs ready-to-paste `neutrals` and `darks` arrays.

#### 3. Add the theme definition to `themes.ts`

Add a new entry to `lightThemes` or `darkThemes`:

```typescript
{
  id: 'coral',
  name: 'Coral',
  mode: 'light',
  swatch: '#f43f5e',
  colors: [
    '#fff1f2', '#ffe4e6', '#fecdd3', '#fda4af', '#fb7185',
    '#f43f5e', '#e11d48', '#be123c', '#9f1239', '#881337',
  ],
  neutrals: [
    // ← paste generated values here
  ],
  darks: [
    // ← paste generated values here
  ],
},
```

That's it — the theme automatically appears in the theme picker.

### Manual Palette Approach (Classic-style)

If you don't want accent tinting, define `neutrals` as the raw Tailwind slate and `darks` as a hand-crafted dark palette. See the `classic` theme for an example.

---

## Generating Tinted Palettes

### The Script

**File:** `scripts/generate-neutrals.ts`

**Usage:**
```bash
npx tsx scripts/generate-neutrals.ts
```

### Algorithm

1. **Base palettes:** The script starts from two base palettes:
   - `BASE_SLATE` — Tailwind slate 50–900 (10 shades) for neutrals
   - `BASE_DARK` — Mantine v7 default dark palette (10 shades) for darks

2. **Tinting (RGB blend):** For each theme accent swatch, the script:
   - Builds a *target* colour with the accent's hue & saturation at `min(baseL, TARGET_L_CAP)` lightness
   - RGB-lerps the base shade toward the target by the tint strength
   - RGB blending avoids hue-rotation artifacts where warm accents (orange, amber) would pass through purple
   - The lightness cap ensures near-white shades get a visibly colored target to blend toward

3. **Output:** Prints the tinted palettes as TypeScript arrays, ready to paste into `themes.ts`.

### Tint Strengths

Default strengths are used unless a theme specifies its own:

| Palette    | Constant                | Default | Rationale |
|------------|------------------------|---------|-----------|
| `neutrals` | `DEFAULT_TINT_NEUTRALS` | **0.10** | Subtle — borders, muted text, surfaces stay close to white/gray |
| `darks`    | `DEFAULT_TINT_DARKS`     | **0.15** | Moderate — dark backgrounds get a perceptible accent tint |

#### Per-Theme Overrides

Each theme in the `THEMES` array can override the defaults via optional `neutralsTint` and `darksTint` properties. Omitted values fall back to the defaults above.

```typescript
const THEMES: ThemeDef[] = [
  { id: 'ocean',  mode: 'light', swatch: '#14b8a6' },                                // uses defaults
  { id: 'sunset', mode: 'light', swatch: '#f59e0b', neutralsTint: 0.04, darksTint: 0.05 }, // nearly gray
  { id: 'nebula', mode: 'dark',  swatch: '#c084fc', darksTint: 0.25 },                // bolder darks
];
```

This lets you dial the tint per theme — warm accents like amber/orange typically look best with lower tint (accent color alone provides identity), while cool accents (teal, purple) can carry a stronger background wash.

#### Which tint affects which mode?

At runtime, `buildTheme()` takes one light theme and one dark theme. Not every palette from each theme is used:

| Theme mode | `neutralsTint` effect | `darksTint` effect |
|------------|----------------------|--------------------|
| **Light**  | **Active** — controls `gray` slot + body bg in light mode | Unused at runtime (darks come from the selected dark theme) |
| **Dark**   | **Active** — overrides `gray` in dark mode (borders, text) | **Active** — overrides `dark` in dark mode (backgrounds, surfaces) |

For light themes, only `neutralsTint` matters. For dark themes, both properties are used. The script still generates all palettes for structural consistency, but you only need to tune the properties that affect the theme's actual mode.

### Lightness Cap (`TARGET_L_CAP = 91`)

Very light base shades (L > 91%) produce near-white blend targets that have almost no visible colour. The script caps the target lightness at 91% so the blend target is saturated enough to produce a noticeable tint. This only affects the 2–3 lightest neutral shades; darker shades are unchanged.

### Example Output

```
// ocean (light) -- accent #14b8a6
neutrals: [
  '#f5fafb', '#eef6f9', '#e1eaf1', '#c9d8e3', '#8fabbc',
  '#5c7e91', '#425c6d', '#2f4758', '#1c2d3c', '#0e1a2a',
],
darks: [
  '#bac9ca', '#9cb2b3', '#839fa0', '#516b6e', '#314145',
  '#273437', '#212b2e', '#171f20', '#121819', '#0e1314',
],

// sunset (light) -- accent #f59e0b [custom: neutrals=0.04, darks=0.05]
neutrals: [
  '#f8f9fa', '#f1f5f7', '#e3e8ef', '#cdd5df', '#98a4b4',
  '#697586', '#4b5665', '#364252', '#202a39', '#111728',
],
```

---

## Component Authoring Rules

### DO

- **Use semantic tokens first:** `var(--mantine-color-body)`, `var(--mantine-color-text)`, `var(--mantine-color-default-border)`, `c="dimmed"`.
- **Use `gray.X` for neutral colors:** Text, borders, icons. These automatically adapt to the selected theme in both light and dark modes.
- **Use `dark.X` for dark-mode surfaces:** `dark-6` for cards, `dark-7` for body (via `body`), `dark-8` for sidebars.
- **Use `teal`/`cyan` for accents:** `isDark ? 'cyan' : 'teal'` for Mantine shorthand, or the CSS vars for inline styles.
- **Check `isDark`** via `const { colorScheme } = useMantineColorScheme(); const isDark = colorScheme === 'dark';`

### DON'T

- **Never use `slate.X`** in new components. The `slate` slot exists only for backward compatibility. Use `gray.X` instead — it's the same values in light mode but gets the correct theme-tinted neutrals in dark mode.
- **Never hardcode hex colors** for backgrounds, text, or borders. Always use Mantine color tokens.
- **Never use `white` or `#fff`** for backgrounds — use `var(--mantine-color-body)` which gives the theme-tinted off-white.
- **Don't import from `themes.ts` in components.** Components should be theme-agnostic. Access colors through Mantine's CSS variables or color props.

### Pattern Quick Reference

```tsx
// ✅ Background — semantic
style={{ backgroundColor: 'var(--mantine-color-body)' }}

// ✅ Background — surface
style={{ backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-0)' }}

// ✅ Border — semantic
style={{ border: '1px solid var(--mantine-color-default-border)' }}

// ✅ Border — popover (lighter in dark)
style={{ border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}` }}

// ✅ Text — heading
<Text c={isDark ? 'gray.1' : 'gray.8'}>

// ✅ Text — body
<Text c={isDark ? 'gray.2' : 'gray.7'}>

// ✅ Text — muted
<Text c="dimmed">

// ✅ Accent
<Button color={isDark ? 'cyan' : 'teal'}>

// ✅ Gradient header
style={{
  background: isDark
    ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
    : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, var(--mantine-color-body) 100%)',
}}

// ❌ Don't — hardcoded colors
style={{ backgroundColor: '#1e293b' }}
style={{ color: 'white' }}

// ❌ Don't — slate references
<Text c="slate.4">
style={{ borderColor: 'var(--mantine-color-slate-7)' }}
```

---

## Semantic vs Explicit Tokens

| Approach        | When to use                                                    |
|----------------|---------------------------------------------------------------|
| **Semantic**    | Background (`body`), text (`text`), borders (`default-border`), muted text (`dimmed`). Use whenever possible — no `isDark` check needed. |
| **Explicit**    | Gradients, accent-colored elements, surface hierarchy, custom icon colors. Use `isDark` ternary with `gray.X`, `dark.X`, `teal`, `cyan`. |

The goal is to minimize `isDark` checks. Semantic tokens handle the vast majority of cases. Explicit tokens are for visual hierarchy (surface layering) and brand differentiation (accent colors).

---

## File Reference

| File                              | Role                                                                |
|----------------------------------|---------------------------------------------------------------------|
| `src/theme/themes.ts`            | Theme palette definitions (`lightThemes[]`, `darkThemes[]`), defaults |
| `src/theme/theme.ts`             | `buildTheme()` — assembles Mantine theme + CSS vars resolver        |
| `src/theme/ThemeContext.tsx`      | React context provider, `useColorTheme()` hook, localStorage        |
| `src/App.tsx`                    | Wires `buildTheme()` output into `<MantineProvider>`               |
| `scripts/generate-neutrals.ts`   | Dev-time script to generate tinted neutral/dark palettes            |

---

*Last updated: February 12, 2026*
