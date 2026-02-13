import { Box, Group, Text, ActionIcon, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useState, useCallback, useRef } from 'react';
import { HiOutlineChevronLeft } from 'react-icons/hi2';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

const SIDEBAR_WIDTH = 280;
const HOVER_ZONE_WIDTH = 24;

interface CollapsibleSidebarProps {
  icon: React.ComponentType<{ size: number; color?: string }>;
  title: string;
  headerRight?: React.ReactNode;
  children: React.ReactNode;
  defaultCollapsed?: boolean;
}

export function CollapsibleSidebar({
  icon: Icon,
  title,
  headerRight,
  children,
  defaultCollapsed = false,
}: CollapsibleSidebarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const canCollapse = flags.sidebarCollapsible;
  const [collapsed, setCollapsed] = useState(canCollapse ? defaultCollapsed : false);
  const [pillVisible, setPillVisible] = useState(false);
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const borderColor = 'var(--mantine-color-default-border)';
  const bgColor = isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
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

  const header = (
    <Box
      px="sm"
      py="xs"
      style={{
        borderBottom: `1px solid ${borderColor}`,
        flexShrink: 0,
      }}
    >
      <Group justify="space-between" align="center" wrap="nowrap">
        <Group gap="xs" wrap="nowrap" style={{ flex: 1, minWidth: 0 }}>
          <Icon size={14} color={iconColor} />
          <Text size="sm" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
            {title}
          </Text>
        </Group>
        <Group gap={4} wrap="nowrap" style={{ flexShrink: 0 }}>
          {headerRight}
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
  );

  // Expanded: normal sidebar in the flex layout
  if (!collapsed || !canCollapse) {
    return (
      <Box
        style={{
          width: SIDEBAR_WIDTH,
          height: '100%',
          borderRight: `1px solid ${borderColor}`,
          backgroundColor: bgColor,
          display: 'flex',
          flexDirection: 'column',
          flexShrink: 0,
        }}
      >
        {header}
        <Box style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
          {children}
        </Box>
      </Box>
    );
  }

  // Collapsed: fixed hover zone at left edge (out of flex layout, zero width); pill fades in on hover
  return (
    <Box
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      style={{
        position: 'fixed',
        left: 0,
        top: 0,
        width: HOVER_ZONE_WIDTH,
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
  );
}
