/**
 * Shared layout metrics for the app chrome, explorer sidebars, and pane toolbars.
 * Keep {@link SIDEBAR_WIDTH} in sync with the brand column in {@link AppHeader}.
 */
import type { CSSProperties } from 'react';
export const SIDEBAR_WIDTH = 280;

/** Single shared height for the explorer toolbar row (sidebar title strip + {@link ViewPaneHeader}). */
export const PANE_TOOLBAR_HEIGHT = 44;

/**
 * Fixed height for main-pane content headers ({@link ContentPaneHeader}): model entity,
 * analysis query editor, and general chat toolbar.
 */
export const CONTENT_PANE_HEADER_HEIGHT = 72;

/** Icon tile size inside {@link ContentPaneHeader}. */
export const CONTENT_PANE_ICON_TILE_SIZE = 40;

/** Horizontal padding for {@link ContentPaneHeader}. */
export const CONTENT_PANE_HEADER_PADDING_X = 'var(--mantine-spacing-md)';

export function contentPaneHeaderBackground(isDark: boolean): string {
  return isDark
    ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
    : 'linear-gradient(135deg, var(--mantine-color-gray-0) 0%, white 100%)';
}

export function contentPaneHeaderShellStyle(isDark: boolean): CSSProperties {
  return {
    height: CONTENT_PANE_HEADER_HEIGHT,
    boxSizing: 'border-box',
    borderBottom: explorerToolbarBottomBorder,
    background: contentPaneHeaderBackground(isDark),
    flexShrink: 0,
    display: 'flex',
    alignItems: 'center',
    paddingLeft: CONTENT_PANE_HEADER_PADDING_X,
    paddingRight: CONTENT_PANE_HEADER_PADDING_X,
    minWidth: 0,
    overflow: 'hidden',
  };
}

/** Left edge hover strip when the explorer sidebar is collapsed (expand pill). */
export const SIDEBAR_HOVER_ZONE_WIDTH = 24;

export const explorerBorderColor = 'var(--mantine-color-default-border)';

/** Bottom rule for explorer toolbar row and {@link CollapsibleSidebar} header — same as AppHeader divider. */
export const explorerToolbarBottomBorder = `1px solid ${explorerBorderColor}`;

export function explorerSidebarBackground(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
}
