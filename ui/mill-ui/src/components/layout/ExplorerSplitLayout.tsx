import { Box, Group, Text, ActionIcon, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useState, useCallback, useRef, type ReactNode } from 'react';
import { HiOutlineChevronLeft } from 'react-icons/hi2';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import {
  SIDEBAR_WIDTH,
  PANE_TOOLBAR_HEIGHT,
  SIDEBAR_HOVER_ZONE_WIDTH,
  explorerBorderColor,
  explorerSidebarBackground,
  explorerToolbarBottomBorder,
} from './layoutChrome';
import { ViewPaneHeader } from './ViewPaneHeader';

export interface ExplorerSplitLayoutProps {
  icon: React.ComponentType<{ size: number; color?: string }>;
  title: string;
  /** Shown in the left toolbar cell after the title (e.g. badges, “new” actions). */
  sidebarHeaderRight?: ReactNode;
  /** Toolbar content for the main pane (scope selectors, toggles, …). */
  viewPaneHeader?: ReactNode;
  /** Scrollable sidebar body below the shared toolbar row. */
  sidebarBody: ReactNode;
  /** Main pane below the toolbar row. */
  main: ReactNode;
  defaultCollapsed?: boolean;
}

/**
 * Two-row explorer shell: (1) one continuous toolbar band — sidebar title + collapse | view-pane
 * actions; (2) sidebar tree + main content. Collapse behaviour matches legacy {@link CollapsibleSidebar}
 * (hover pill). Chat {@link AppShell} keeps using {@link CollapsibleSidebar} until voluntarily migrated.
 *
 * When collapsed, the toolbar row is only {@link ViewPaneHeader} across the usable width so there is
 * no empty left header cell (WI-107).
 */
export function ExplorerSplitLayout({
  icon: Icon,
  title,
  sidebarHeaderRight,
  viewPaneHeader,
  sidebarBody,
  main,
  defaultCollapsed = false,
}: ExplorerSplitLayoutProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const canCollapse = flags.sidebarCollapsible;
  const [collapsed, setCollapsed] = useState(canCollapse ? defaultCollapsed : false);
  const [pillVisible, setPillVisible] = useState(false);
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const bgColor = explorerSidebarBackground(isDark);
  const iconColor = isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)';

  const handleCollapse = useCallback(() => {
    setCollapsed(true);
  }, []);

  const handleExpand = useCallback(() => {
    setCollapsed(false);
    setPillVisible(false);
  }, []);

  const handleMouseEnter = useCallback(() => {
    if (hideTimer.current) {
      clearTimeout(hideTimer.current);
      hideTimer.current = null;
    }
    setPillVisible(true);
  }, []);

  const handleMouseLeave = useCallback(() => {
    hideTimer.current = setTimeout(() => {
      setPillVisible(false);
    }, 400);
  }, []);

  const toolbarRowExpanded = (
    <Group
      wrap="nowrap"
      align="stretch"
      gap={0}
      style={{
        height: PANE_TOOLBAR_HEIGHT,
        flexShrink: 0,
        backgroundColor: bgColor,
      }}
    >
      <Box
        px="sm"
        style={{
          width: SIDEBAR_WIDTH,
          flexShrink: 0,
          boxSizing: 'border-box',
          display: 'flex',
          alignItems: 'center',
          borderRight: `1px solid ${explorerBorderColor}`,
          borderBottom: explorerToolbarBottomBorder,
        }}
      >
        <Group justify="space-between" align="center" wrap="nowrap" style={{ width: '100%', minWidth: 0 }}>
          <Group gap="xs" wrap="nowrap" style={{ flex: 1, minWidth: 0 }}>
            <Icon size={14} color={iconColor} />
            <Text size="sm" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
              {title}
            </Text>
          </Group>
          <Group gap={4} wrap="nowrap" style={{ flexShrink: 0 }}>
            {sidebarHeaderRight}
            {canCollapse && (
              <Tooltip label="Hide sidebar" withArrow position="right">
                <ActionIcon
                  size="sm"
                  variant="subtle"
                  color={isDark ? 'gray.4' : 'gray.5'}
                  onClick={handleCollapse}
                >
                  <HiOutlineChevronLeft size={14} />
                </ActionIcon>
              </Tooltip>
            )}
          </Group>
        </Group>
      </Box>
      <ViewPaneHeader style={{ flex: 1 }}>{viewPaneHeader}</ViewPaneHeader>
    </Group>
  );

  const hoverPill =
    canCollapse && collapsed ? (
      <Box
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        style={{
          position: 'fixed',
          left: 0,
          top: 0,
          width: SIDEBAR_HOVER_ZONE_WIDTH,
          height: '100%',
          zIndex: 100,
        }}
      >
        <Tooltip label={`Show ${title}`} position="right" withArrow disabled={!pillVisible}>
          <ActionIcon
            size={40}
            variant="filled"
            color={isDark ? 'cyan' : 'teal'}
            onClick={handleExpand}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            style={{
              position: 'absolute',
              left: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              zIndex: 50,
              borderRadius: '0 8px 8px 0',
              boxShadow: isDark
                ? '2px 0 8px rgba(0,0,0,0.4)'
                : '2px 0 8px rgba(0,0,0,0.15)',
              opacity: pillVisible ? 1 : 0,
              transition: 'opacity 200ms ease',
              pointerEvents: pillVisible ? 'auto' : 'none',
            }}
          >
            <Icon size={18} />
          </ActionIcon>
        </Tooltip>
      </Box>
    ) : null;

  if (!collapsed || !canCollapse) {
    return (
      <Box
        style={{
          display: 'flex',
          flex: 1,
          minHeight: 0,
          minWidth: 0,
          height: '100%',
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <Box
          style={{
            display: 'flex',
            flexDirection: 'column',
            flex: 1,
            minWidth: 0,
            minHeight: 0,
            height: '100%',
            overflow: 'hidden',
          }}
        >
          {toolbarRowExpanded}
          <Box style={{ display: 'flex', flex: 1, minHeight: 0, overflow: 'hidden', alignItems: 'stretch' }}>
            <Box
              style={{
                width: SIDEBAR_WIDTH,
                flexShrink: 0,
                alignSelf: 'stretch',
                minHeight: 0,
                borderRight: `1px solid ${explorerBorderColor}`,
                backgroundColor: bgColor,
                display: 'flex',
                flexDirection: 'column',
              }}
            >
              <Box style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                {sidebarBody}
              </Box>
            </Box>
            <Box
              style={{
                flex: 1,
                minWidth: 0,
                minHeight: 0,
                backgroundColor: 'var(--mantine-color-body)',
                overflow: 'hidden',
              }}
            >
              {main}
            </Box>
          </Box>
        </Box>
      </Box>
    );
  }

  return (
    <Box
      style={{
        display: 'flex',
        flex: 1,
        minHeight: 0,
        minWidth: 0,
        height: '100%',
        overflow: 'hidden',
        position: 'relative',
      }}
    >
      {hoverPill}
      <Box
        style={{
          flex: 1,
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          height: '100%',
          overflow: 'hidden',
        }}
      >
        <ViewPaneHeader>{viewPaneHeader}</ViewPaneHeader>
        <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden', backgroundColor: 'var(--mantine-color-body)' }}>
          {main}
        </Box>
      </Box>
    </Box>
  );
}
