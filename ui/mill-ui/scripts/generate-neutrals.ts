/**
 * Dev-time script to generate tinted neutral palettes for each theme.
 *
 * Usage:  npx tsx scripts/generate-neutrals.ts
 *
 * Algorithm:
 *   1. Take the base slate neutral palette and Mantine dark palette (10 shades each)
 *   2. For each theme, build a target colour (accent hue + saturation @ capped lightness)
 *   3. RGB-lerp each base shade toward the target by the tint strength
 *   4. Print static MantineColorsTuple arrays ready to paste into themes.ts
 *
 * Key parameters:
 *   - DEFAULT_TINT_NEUTRALS  (0.10) — default tint for borders/text grays
 *   - DEFAULT_TINT_DARKS     (0.15) — default tint for dark-mode backgrounds
 *   - TARGET_L_CAP           (91)   — caps target lightness so near-white shades get visible colour
 *
 * Each theme can override tint strengths via `neutralsTint` and `darksTint`
 * properties. Omitted values fall back to the defaults above.
 *
 * No runtime dependency -- this script is never bundled.
 */

// -- Base palettes ---------------------------------------------------------

// Base slate palette (Tailwind slate 50-900) — used for `gray` overrides
const BASE_SLATE = [
  '#f8fafc', // 0  (lightest)
  '#f1f5f9', // 1
  '#e2e8f0', // 2
  '#cbd5e1', // 3
  '#94a3b8', // 4
  '#64748b', // 5
  '#475569', // 6
  '#334155', // 7
  '#1e293b', // 8
  '#0f172a', // 9  (darkest)
];

// Mantine v7 default dark palette — used for `dark` overrides in dark mode
const BASE_DARK = [
  '#C1C2C5', // 0  (light text on dark bg)
  '#A6A7AB', // 1
  '#909296', // 2
  '#5C5F66', // 3
  '#373A40', // 4  (borders)
  '#2C2E33', // 5
  '#25262B', // 6  (surface/card bg)
  '#1A1B1E', // 7  (body bg)
  '#141517', // 8
  '#101113', // 9  (darkest)
];

// -- Theme definitions (id + accent swatch + optional per-theme tint) ------

interface ThemeDef {
  id: string;
  mode: 'light' | 'dark';
  swatch: string;
  /** Override neutrals tint strength (defaults to DEFAULT_TINT_NEUTRALS) */
  neutralsTint?: number;
  /** Override darks tint strength (defaults to DEFAULT_TINT_DARKS) */
  darksTint?: number;
}

/**
 * Default tint strengths — used when a theme doesn't specify its own.
 * Neutrals are used for borders/text, so keep them moderate.
 * Darks are large background surfaces — push them stronger so themes look distinct.
 */
const DEFAULT_TINT_NEUTRALS = 0.10;
const DEFAULT_TINT_DARKS = 0.15;

const THEMES: ThemeDef[] = [
  // Light themes
  { id: 'ocean',    mode: 'light', swatch: '#14b8a6' },
  { id: 'forest',   mode: 'light', swatch: '#16a34a' },
  { id: 'lavender', mode: 'light', swatch: '#8b5cf6' },
  { id: 'sunset',   mode: 'light', swatch: '#f59e0b', neutralsTint: 0.04, darksTint: 0.05 },
  // Dark themes
  { id: 'neon',     mode: 'dark',  swatch: '#22d3ee' },
  { id: 'aurora',   mode: 'dark',  swatch: '#34d399' },
  { id: 'nebula',   mode: 'dark',  swatch: '#c084fc' },
  { id: 'ember',    mode: 'dark',  swatch: '#fb923c', neutralsTint: 0.04, darksTint: 0.05 },
];

// -- Color math helpers (pure HSL, no deps) --------------------------------

function hexToRgb(hex: string): [number, number, number] {
  const h = hex.replace('#', '');
  return [
    parseInt(h.slice(0, 2), 16),
    parseInt(h.slice(2, 4), 16),
    parseInt(h.slice(4, 6), 16),
  ];
}

function rgbToHex(r: number, g: number, b: number): string {
  const toHex = (n: number) => Math.round(Math.max(0, Math.min(255, n))).toString(16).padStart(2, '0');
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
}

function rgbToHsl(r: number, g: number, b: number): [number, number, number] {
  r /= 255; g /= 255; b /= 255;
  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  const l = (max + min) / 2;
  let h = 0, s = 0;

  if (max !== min) {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
    if (max === r) h = ((g - b) / d + (g < b ? 6 : 0)) / 6;
    else if (max === g) h = ((b - r) / d + 2) / 6;
    else h = ((r - g) / d + 4) / 6;
  }

  return [h * 360, s * 100, l * 100];
}

function hslToRgb(h: number, s: number, l: number): [number, number, number] {
  h /= 360; s /= 100; l /= 100;

  if (s === 0) {
    const v = Math.round(l * 255);
    return [v, v, v];
  }

  const hue2rgb = (p: number, q: number, t: number) => {
    if (t < 0) t += 1;
    if (t > 1) t -= 1;
    if (t < 1 / 6) return p + (q - p) * 6 * t;
    if (t < 1 / 2) return q;
    if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
    return p;
  };

  const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
  const p = 2 * l - q;

  return [
    Math.round(hue2rgb(p, q, h + 1 / 3) * 255),
    Math.round(hue2rgb(p, q, h) * 255),
    Math.round(hue2rgb(p, q, h - 1 / 3) * 255),
  ];
}

/**
 * Cap for the target lightness when building the blend target.
 * Very light base shades (L > 88%) produce near-white targets that have
 * almost no visible colour. By capping at 88%, the target is saturated enough
 * to produce a noticeable tint even for near-white backgrounds.
 * (Only affects the 2–3 lightest neutral shades; darker shades are unchanged.)
 */
const TARGET_L_CAP = 91;

/**
 * Mix a base color toward an accent hue via RGB blending.
 *
 * 1. Build a *target* colour that has the accent's hue & saturation at
 *    `min(baseL, TARGET_L_CAP)` lightness.
 * 2. RGB-lerp the base toward the target by `strength`.
 *
 * RGB blending avoids the hue-rotation artifact where warm accents
 * (orange, amber) pass through purple when the base has a cool hue.
 * The lightness cap ensures near-white shades still get a visible tint.
 */
function tintToward(baseHex: string, accentHex: string, strength: number): string {
  const [br, bg, bb] = hexToRgb(baseHex);
  const [, , bl] = rgbToHsl(br, bg, bb);
  const [ar, ag, ab] = hexToRgb(accentHex);
  const [ah, as_val] = rgbToHsl(ar, ag, ab);

  // Target: accent hue + accent saturation @ capped lightness
  const targetL = Math.min(bl, TARGET_L_CAP);
  const [tr, tg, tb] = hslToRgb(ah, as_val, targetL);

  // RGB lerp
  const r = br + (tr - br) * strength;
  const g = bg + (tg - bg) * strength;
  const b = bb + (tb - bb) * strength;

  return rgbToHex(r, g, b);
}

// -- Generate ---------------------------------------------------------------

function generateNeutrals(accentSwatch: string, strength: number): string[] {
  return BASE_SLATE.map((shade) => tintToward(shade, accentSwatch, strength));
}

function generateDarks(accentSwatch: string, strength: number): string[] {
  return BASE_DARK.map((shade) => tintToward(shade, accentSwatch, strength));
}

/** Resolve per-theme tint strengths, falling back to defaults */
function resolveTints(theme: ThemeDef): { neutrals: number; darks: number } {
  return {
    neutrals: theme.neutralsTint ?? DEFAULT_TINT_NEUTRALS,
    darks: theme.darksTint ?? DEFAULT_TINT_DARKS,
  };
}

// -- Output -----------------------------------------------------------------

console.log('// Generated palettes -- paste into themes.ts');
console.log(`// Default tint strengths: neutrals=${DEFAULT_TINT_NEUTRALS}, darks=${DEFAULT_TINT_DARKS}`);
console.log(`// TARGET_L_CAP=${TARGET_L_CAP}`);
console.log('');

for (const theme of THEMES) {
  const tints = resolveTints(theme);
  const neutrals = generateNeutrals(theme.swatch, tints.neutrals);
  const darks = generateDarks(theme.swatch, tints.darks);
  const overrides = theme.neutralsTint != null || theme.darksTint != null
    ? ` [custom: neutrals=${tints.neutrals}, darks=${tints.darks}]`
    : '';
  console.log(`// ${theme.id} (${theme.mode}) -- accent ${theme.swatch}${overrides}`);
  console.log(`neutrals: [\n  ${neutrals.map((c) => `'${c}'`).join(',\n  ')},\n],`);
  console.log(`darks: [\n  ${darks.map((c) => `'${c}'`).join(',\n  ')},\n],`);
  console.log('');
}

// Compact comparison tables
console.log('// ---- Neutrals (gray override) ----');
console.log('// shade:  0        1        2        3        4        5        6        7        8        9');
console.log(`// slate:  ${BASE_SLATE.join('  ')}`);
for (const theme of THEMES) {
  const tints = resolveTints(theme);
  const neutrals = generateNeutrals(theme.swatch, tints.neutrals);
  const tintLabel = theme.neutralsTint != null ? `*${tints.neutrals}` : '';
  console.log(`// ${(theme.id + tintLabel).padEnd(12)}: ${neutrals.join('  ')}`);
}
console.log('');
console.log('// ---- Darks (dark palette override) ----');
console.log('// shade:  0        1        2        3        4        5        6        7        8        9');
console.log(`// base :  ${BASE_DARK.join('  ')}`);
for (const theme of THEMES) {
  const tints = resolveTints(theme);
  const darks = generateDarks(theme.swatch, tints.darks);
  const tintLabel = theme.darksTint != null ? `*${tints.darks}` : '';
  console.log(`// ${(theme.id + tintLabel).padEnd(12)}: ${darks.join('  ')}`);
}
