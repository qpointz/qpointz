import { Box, Group, ActionIcon, Button, Tooltip, Menu, Avatar, Text, useMantineColorScheme } from '@mantine/core';
import { useLocation, useNavigate } from 'react-router';
import { useMemo } from 'react';
import {
  HiOutlineChatBubbleLeftRight,
  HiOutlineSquare3Stack3D,
  HiOutlineAcademicCap,
  HiOutlineBeaker,
  HiOutlineLink,
  HiOutlineUserCircle,
  HiOutlineWrenchScrewdriver,
  HiOutlineArrowRightOnRectangle,
  HiOutlineSun,
  HiOutlineMoon,
  HiCheck,
} from 'react-icons/hi2';
import { useColorTheme } from '../../theme/ThemeContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { useAuth, APP_NAME } from '../../App';
import type { FeatureFlags } from '../../features/defaults';
import { GlobalSearch } from './GlobalSearch';

/** Must match SIDEBAR_WIDTH in CollapsibleSidebar.tsx */
const SIDEBAR_WIDTH = 280;

const mainNavItems = [
  { path: '/model', label: 'Model', icon: HiOutlineSquare3Stack3D, flag: 'viewModel' as keyof FeatureFlags },
  { path: '/knowledge', label: 'Knowledge', icon: HiOutlineAcademicCap, flag: 'viewKnowledge' as keyof FeatureFlags },
  { path: '/analysis', label: 'Analysis', icon: HiOutlineBeaker, flag: 'viewAnalysis' as keyof FeatureFlags },
  { path: '/chat', label: 'Chat', icon: HiOutlineChatBubbleLeftRight, flag: 'viewChat' as keyof FeatureFlags },
  { path: '/connect', label: 'Connect', icon: HiOutlineLink, flag: 'viewConnect' as keyof FeatureFlags },
];

const rightNavItems = [
  { path: '/admin', label: 'Admin', icon: HiOutlineWrenchScrewdriver, flag: 'viewAdmin' as keyof FeatureFlags },
];

export function AppHeader() {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const location = useLocation();
  const navigate = useNavigate();
  const flags = useFeatureFlags();
  const { logout } = useAuth();
  const {
    lightThemeId,
    darkThemeId,
    lightThemes: availableLightThemes,
    darkThemes: availableDarkThemes,
    setLightTheme,
    setDarkTheme,
  } = useColorTheme();

  const currentThemes = isDark ? availableDarkThemes : availableLightThemes;
  const activeThemeId = isDark ? darkThemeId : lightThemeId;
  const setTheme = isDark ? setDarkTheme : setLightTheme;

  // Filter navigation items based on feature flags
  const mainItems = useMemo(
    () => mainNavItems.filter((item) => flags[item.flag]),
    [flags],
  );
  const rightItems = useMemo(
    () => rightNavItems.filter((item) => flags[item.flag]),
    [flags],
  );

  return (
    <Box
      h={56}
      px="md"
      style={{
        borderBottom: `1px solid var(--mantine-color-default-border)`,
        backgroundColor: 'var(--mantine-color-body)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
    >
      {/* Logo / Brand — clickable, navigates to Home */}
      <Group
        gap="xs"
        wrap="nowrap"
        style={{
          minWidth: SIDEBAR_WIDTH,
          maxWidth: SIDEBAR_WIDTH + 80,
          flexShrink: 0,
          cursor: 'pointer',
          paddingRight: 16,
        }}
        onClick={() => navigate('/home')}
      >
        <img
          src={`${import.meta.env.BASE_URL}mill.svg`}
          alt="Mill logo"
          style={{
            width: 32,
            height: 32,
            flexShrink: 0,
          }}
        />
        <Text fw={600} size="lg" c={isDark ? 'gray.1' : 'gray.8'} lineClamp={1} style={{ whiteSpace: 'nowrap' }}>
          {APP_NAME}
        </Text>
      </Group>

      {/* Navigation */}
      <Group gap={4} wrap="nowrap" style={{ flex: 1, marginLeft: 24, justifyContent: 'space-between' }}>
        {/* Main nav items — left-aligned */}
        <Group gap={4} wrap="nowrap">
          {mainItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const activeColor = isDark ? 'cyan' : 'teal';
            const inactiveColor = isDark ? 'gray.4' : 'gray.6';
            return (
              <Button
                key={item.path}
                variant={isActive ? 'light' : 'subtle'}
                color={isActive ? activeColor : inactiveColor}
                size="compact-sm"
                leftSection={<item.icon size={16} />}
                onClick={() => navigate(item.path)}
              >
                {item.label}
              </Button>
            );
          })}
          {/* Spacer + Global Search icon in nav row */}
          {flags.headerGlobalSearch && (
            <>
              <Box
                style={{
                  width: 1,
                  height: 20,
                  backgroundColor: 'var(--mantine-color-default-border)',
                  marginLeft: 8,
                  marginRight: 4,
                  flexShrink: 0,
                }}
              />
              <GlobalSearch />
            </>
          )}
        </Group>
        {/* Right-aligned nav items (Admin) */}
        {rightItems.length > 0 && (
          <Group gap={4} wrap="nowrap">
            {rightItems.map((item) => {
              const isActive = location.pathname.startsWith(item.path);
              const activeColor = isDark ? 'cyan' : 'teal';
              const inactiveColor = isDark ? 'gray.4' : 'gray.6';
              return (
                <Button
                  key={item.path}
                  variant={isActive ? 'light' : 'subtle'}
                  color={isActive ? activeColor : inactiveColor}
                  size="compact-sm"
                  leftSection={<item.icon size={16} />}
                  onClick={() => navigate(item.path)}
                >
                  {item.label}
                </Button>
              );
            })}
          </Group>
        )}
      </Group>

      {/* Right side actions */}
      <Group gap={4} wrap="nowrap" style={{ justifyContent: 'flex-end' }}>
        {/* User Profile Menu */}
        {flags.headerUserProfile && (
          <Menu shadow="md" width={220} position="bottom-end" withArrow>
            <Menu.Target>
              <ActionIcon
                variant="subtle"
                color={isDark ? 'gray.4' : 'gray.6'}
                size="lg"
              >
                <HiOutlineUserCircle size={22} />
              </ActionIcon>
            </Menu.Target>
            <Menu.Dropdown>
              {/* Profile section */}
              <Box px="sm" py="xs">
                <Group gap="sm" wrap="nowrap">
                  <Avatar size={36} radius="xl" color={isDark ? 'cyan' : 'teal'}>
                    DC
                  </Avatar>
                  <Box style={{ flex: 1, minWidth: 0 }}>
                    <Text size="sm" fw={500} lineClamp={1} c={isDark ? 'gray.1' : 'gray.8'}>
                      Demo User
                    </Text>
                    <Text size="xs" c="dimmed" lineClamp={1}>
                      demo@datachat.io
                    </Text>
                  </Box>
                </Group>
              </Box>

              <Menu.Divider />

              {/* Theme row: light/dark toggle + color swatches */}
              {flags.headerThemeSwitcher && (
                <Box px="sm" py={6}>
                  <Group justify="space-between" align="center" wrap="nowrap">
                    <Group
                      gap={6}
                      wrap="nowrap"
                      style={{ cursor: 'pointer' }}
                      onClick={() => toggleColorScheme()}
                    >
                      {isDark ? <HiOutlineSun size={14} /> : <HiOutlineMoon size={14} />}
                      <Text size="sm">{isDark ? 'Light' : 'Dark'}</Text>
                    </Group>
                    <Group gap={6} wrap="nowrap">
                      {currentThemes.map((t) => {
                        const isActive = t.id === activeThemeId;
                        return (
                          <Tooltip key={t.id} label={t.name} withArrow position="bottom">
                            <Box
                              onClick={() => setTheme(t.id)}
                              style={{
                                width: 18,
                                height: 18,
                                borderRadius: '50%',
                                backgroundColor: t.swatch,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                outline: isActive ? '2px solid' : 'none',
                                outlineColor: isActive
                                  ? isDark ? 'var(--mantine-color-dark-2)' : 'var(--mantine-color-gray-7)'
                                  : undefined,
                                outlineOffset: 2,
                                transition: 'outline 150ms ease',
                              }}
                            >
                              {isActive && <HiCheck size={10} color="white" />}
                            </Box>
                          </Tooltip>
                        );
                      })}
                    </Group>
                  </Group>
                </Box>
              )}
              {flags.viewProfile && (
                <Menu.Item
                  leftSection={<HiOutlineUserCircle size={14} />}
                  onClick={() => navigate('/profile')}
                >
                  Profile
                </Menu.Item>
              )}

              <Menu.Divider />

              <Menu.Item
                leftSection={<HiOutlineArrowRightOnRectangle size={14} />}
                color="red"
                onClick={() => {
                  logout();
                  navigate('/');
                }}
              >
                Log out
              </Menu.Item>
            </Menu.Dropdown>
          </Menu>
        )}
      </Group>
    </Box>
  );
}
