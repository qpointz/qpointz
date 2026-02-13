import { createContext, useContext, useState, useCallback, useMemo, type ReactNode } from 'react';
import {
  lightThemes,
  darkThemes,
  findLightTheme,
  findDarkTheme,
  DEFAULT_LIGHT_THEME_ID,
  DEFAULT_DARK_THEME_ID,
  type ColorTheme,
} from './themes';

const LS_LIGHT_KEY = 'mill-ui-light-theme';
const LS_DARK_KEY = 'mill-ui-dark-theme';

function readLS(key: string, fallback: string): string {
  try {
    return localStorage.getItem(key) || fallback;
  } catch {
    return fallback;
  }
}

function writeLS(key: string, value: string) {
  try {
    localStorage.setItem(key, value);
  } catch {
    // ignore
  }
}

interface ColorThemeContextValue {
  lightThemeId: string;
  darkThemeId: string;
  lightTheme: ColorTheme;
  darkTheme: ColorTheme;
  lightThemes: ColorTheme[];
  darkThemes: ColorTheme[];
  setLightTheme: (id: string) => void;
  setDarkTheme: (id: string) => void;
}

const ColorThemeContext = createContext<ColorThemeContextValue | null>(null);

export function ColorThemeProvider({ children }: { children: ReactNode }) {
  const [lightId, setLightId] = useState(() => readLS(LS_LIGHT_KEY, DEFAULT_LIGHT_THEME_ID));
  const [darkId, setDarkId] = useState(() => readLS(LS_DARK_KEY, DEFAULT_DARK_THEME_ID));

  const handleSetLight = useCallback((id: string) => {
    setLightId(id);
    writeLS(LS_LIGHT_KEY, id);
  }, []);

  const handleSetDark = useCallback((id: string) => {
    setDarkId(id);
    writeLS(LS_DARK_KEY, id);
  }, []);

  const value = useMemo<ColorThemeContextValue>(() => ({
    lightThemeId: lightId,
    darkThemeId: darkId,
    lightTheme: findLightTheme(lightId),
    darkTheme: findDarkTheme(darkId),
    lightThemes,
    darkThemes,
    setLightTheme: handleSetLight,
    setDarkTheme: handleSetDark,
  }), [lightId, darkId, handleSetLight, handleSetDark]);

  return (
    <ColorThemeContext.Provider value={value}>
      {children}
    </ColorThemeContext.Provider>
  );
}

export function useColorTheme(): ColorThemeContextValue {
  const ctx = useContext(ColorThemeContext);
  if (!ctx) throw new Error('useColorTheme must be used inside ColorThemeProvider');
  return ctx;
}
