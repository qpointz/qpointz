import { Box, useMantineColorScheme, type BoxProps } from '@mantine/core';
import {
  PANE_TOOLBAR_HEIGHT,
  explorerSidebarBackground,
  explorerToolbarBottomBorder,
} from './layoutChrome';

export type ViewPaneHeaderProps = BoxProps & {
  children?: React.ReactNode;
};

/**
 * Right-hand cell of the explorer toolbar row under {@link AppHeader}.
 * Same background as the sidebar title strip so the bar reads as one continuous band.
 */
export function ViewPaneHeader({ children, style, ...rest }: ViewPaneHeaderProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box
      style={{
        flex: 1,
        minWidth: 0,
        height: PANE_TOOLBAR_HEIGHT,
        boxSizing: 'border-box',
        borderBottom: explorerToolbarBottomBorder,
        backgroundColor: explorerSidebarBackground(isDark),
        display: 'flex',
        alignItems: 'center',
        paddingLeft: 'var(--mantine-spacing-sm)',
        paddingRight: 'var(--mantine-spacing-sm)',
        gap: 'var(--mantine-spacing-xs)',
        ...style,
      }}
      {...rest}
    >
      {children}
    </Box>
  );
}
