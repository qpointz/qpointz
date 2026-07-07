import type { ActionIconProps } from '@mantine/core';
import { useMantineColorScheme, type TabsStylesNames } from '@mantine/core';
import type { CSSProperties } from 'react';
import { chatAccentColor } from '../chatChrome';
import classes from './ChatArtifactCard.module.css';

/** Shared outline icon size for artefact toolbars (tabs + action bar). */
export const ARTIFACT_TOOLBAR_ICON_SIZE = 14;

/** Default Mantine color name for idle toolbar icons. */
export function artifactToolbarIconColor(isDark: boolean): string {
  return isDark ? 'gray.4' : 'gray.5';
}

/** Mantine color name for the active view tab. */
export function artifactToolbarActiveColor(isDark: boolean): 'teal' | 'cyan' {
  return chatAccentColor(isDark);
}

/** CSS color for react-icons on idle tabs. */
export function artifactToolbarIconCss(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)';
}

/** CSS color for react-icons on the active tab. */
export function artifactToolbarActiveIconCss(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)';
}

export const artifactToolbarActionIconProps: Pick<ActionIconProps, 'variant' | 'size'> = {
  variant: 'subtle',
  size: 'sm',
};

const artifactTabClassNames = {
  list: classes.artifactTabList,
  tab: classes.artifactTab,
};

export function buildArtifactTabsStyles(): Partial<Record<TabsStylesNames, CSSProperties>> {
  return {
    root: { display: 'block' },
    panel: { paddingTop: 0 },
    tabLabel: { fontWeight: 400, lineHeight: 1 },
  };
}

export interface ArtifactTabsChrome {
  styles: Partial<Record<TabsStylesNames, CSSProperties>>;
  classNames: typeof artifactTabClassNames;
}

/** Theme-aware Tabs chrome for SQL/facet artefact view switchers. */
export function useArtifactTabsStyles(): ArtifactTabsChrome {
  useMantineColorScheme(); // subscribe to scheme changes for CSS module dark rules
  return {
    styles: buildArtifactTabsStyles(),
    classNames: artifactTabClassNames,
  };
}
