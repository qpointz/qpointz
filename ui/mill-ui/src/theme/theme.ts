import { createTheme, type CSSVariablesResolver, type MantineColorsTuple } from '@mantine/core';
import type { ColorTheme } from './themes';

/** Mantine named color slots remapped to grey when both themes are monochrome. */
const MANTINE_COLOR_SLOTS = [
  'red', 'pink', 'grape', 'violet', 'indigo', 'blue', 'cyan', 'teal', 'green', 'lime', 'yellow', 'orange',
] as const;

function isMonochromePair(lightTheme: ColorTheme, darkTheme: ColorTheme): boolean {
  return lightTheme.monochrome === true && darkTheme.monochrome === true;
}

function buildMantineColors(lightTheme: ColorTheme, darkTheme: ColorTheme) {
  const lightNeutrals = lightTheme.neutrals;
  const darkPalette = darkTheme.darks;
  const lightAccent = lightTheme.colors;
  const darkAccent = darkTheme.colors;

  const base = {
    gray: lightNeutrals,
    dark: darkPalette,
    slate: lightNeutrals,
    teal: lightAccent,
    cyan: darkAccent,
  };

  if (!isMonochromePair(lightTheme, darkTheme)) {
    return base;
  }

  const greySlots: Record<string, MantineColorsTuple> = {};
  for (const slot of MANTINE_COLOR_SLOTS) {
    greySlots[slot] = slot === 'cyan' ? darkAccent : lightAccent;
  }

  return { ...base, ...greySlots };
}

/**
 * Build a Mantine theme + CSS variables resolver from two ColorTheme objects.
 *
 * Palette mapping:
 * - `teal` slot  ← lightTheme.colors  (light-mode accent)
 * - `cyan` slot  ← darkTheme.colors   (dark-mode accent)
 * - `gray` slot  ← lightTheme.neutrals (default; overridden in dark by CSS vars)
 * - `dark` slot  ← darkTheme.darks     (dark-mode bg/surfaces)
 * - `slate` slot ← lightTheme.neutrals (kept for explicit `color="slate"` usage)
 *
 * The returned `cssVariablesResolver` adjusts `gray` and body per color scheme
 * so light mode uses the light theme's neutrals and dark mode uses the dark theme's.
 * Pass it to `<MantineProvider cssVariablesResolver={resolver}>`.
 */
export function buildTheme(lightTheme: ColorTheme, darkTheme: ColorTheme) {
  const lightNeutrals = lightTheme.neutrals;
  const darkNeutrals = darkTheme.neutrals;
  const darkPalette = darkTheme.darks;

  // CSS variables resolver — passed separately to MantineProvider -----------
  const resolver: CSSVariablesResolver = () => {
    const light: Record<string, string> = {
      // Tinted body background instead of pure white
      '--mantine-color-body': lightNeutrals[0],
    };

    const dark: Record<string, string> = {};

    // Override gray palette in dark mode with the dark theme's neutrals
    for (let i = 0; i < 10; i++) {
      dark[`--mantine-color-gray-${i}`] = darkNeutrals[i]!;
    }

    // Override dark palette with tinted darks
    for (let i = 0; i < 10; i++) {
      dark[`--mantine-color-dark-${i}`] = darkPalette[i]!;
    }

    return { variables: {}, light, dark };
  };

  const theme = createTheme({
    primaryColor: isMonochromePair(lightTheme, darkTheme) ? 'gray' : 'teal',
    colors: buildMantineColors(lightTheme, darkTheme),
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    headings: {
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    },
    radius: {
      xs: '4px',
      sm: '6px',
      md: '8px',
      lg: '12px',
      xl: '16px',
    },
    defaultRadius: 'md',
    components: {
      Button: {
        defaultProps: {
          radius: 'md',
        },
      },
      ActionIcon: {
        defaultProps: {
          radius: 'md',
        },
      },
      TextInput: {
        defaultProps: {
          radius: 'md',
        },
      },
      Textarea: {
        defaultProps: {
          radius: 'md',
        },
      },
      Paper: {
        defaultProps: {
          radius: 'lg',
        },
      },
    },
  });

  return { theme, resolver };
}

// Default theme (Classic light + Classic dark) for backwards compatibility
import { findLightTheme, findDarkTheme, DEFAULT_LIGHT_THEME_ID, DEFAULT_DARK_THEME_ID } from './themes';

const defaultBuild = buildTheme(
  findLightTheme(DEFAULT_LIGHT_THEME_ID),
  findDarkTheme(DEFAULT_DARK_THEME_ID),
);
export const theme = defaultBuild.theme;
export const defaultCssVariablesResolver = defaultBuild.resolver;
