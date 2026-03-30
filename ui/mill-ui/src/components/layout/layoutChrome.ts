/**
 * Shared layout metrics for the app chrome, explorer sidebars, and pane toolbars.
 * Keep {@link SIDEBAR_WIDTH} in sync with the brand column in {@link AppHeader}.
 */
export const SIDEBAR_WIDTH = 280;

/** Single shared height for the explorer toolbar row (sidebar title strip + {@link ViewPaneHeader}). */
export const PANE_TOOLBAR_HEIGHT = 44;

/** Left edge hover strip when the explorer sidebar is collapsed (expand pill). */
export const SIDEBAR_HOVER_ZONE_WIDTH = 24;

export const explorerBorderColor = 'var(--mantine-color-default-border)';

/** Bottom rule for explorer toolbar row and {@link CollapsibleSidebar} header — same as AppHeader divider. */
export const explorerToolbarBottomBorder = `1px solid ${explorerBorderColor}`;

export function explorerSidebarBackground(isDark: boolean): string {
  return isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
}
