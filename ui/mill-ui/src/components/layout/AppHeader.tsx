import { Box, Group, Tabs, ActionIcon, Tooltip, useMantineColorScheme, Text } from '@mantine/core';
import { useLocation, useNavigate } from 'react-router';
import {
  HiOutlineChatBubbleLeftRight,
  HiOutlineCircleStack,
  HiOutlineLightBulb,
  HiOutlineSun,
  HiOutlineMoon,
} from 'react-icons/hi2';

const navItems = [
  { path: '/chat', label: 'Chat', icon: HiOutlineChatBubbleLeftRight },
  { path: '/data-model', label: 'Data Model', icon: HiOutlineCircleStack },
  { path: '/context', label: 'Context', icon: HiOutlineLightBulb },
];

export function AppHeader() {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const location = useLocation();
  const navigate = useNavigate();

  // Determine active tab from current path
  const activeTab = navItems.find(item => location.pathname.startsWith(item.path))?.path || '/chat';

  return (
    <Box
      h={56}
      px="md"
      style={{
        borderBottom: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
        backgroundColor: isDark ? 'var(--mantine-color-slate-9)' : 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
    >
      {/* Logo / Brand */}
      <Group gap="xs">
        <Box
          style={{
            width: 32,
            height: 32,
            borderRadius: 8,
            background: isDark
              ? 'linear-gradient(135deg, var(--mantine-color-cyan-7) 0%, var(--mantine-color-teal-8) 100%)'
              : 'linear-gradient(135deg, var(--mantine-color-teal-5) 0%, var(--mantine-color-teal-7) 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <HiOutlineChatBubbleLeftRight size={18} color="white" />
        </Box>
        <Text fw={600} size="lg" c={isDark ? 'slate.1' : 'slate.8'}>
          DataChat
        </Text>
      </Group>

      {/* Navigation Tabs */}
      <Tabs
        value={activeTab}
        onChange={(value) => value && navigate(value)}
        variant="pills"
        styles={{
          root: {
            flex: 1,
            maxWidth: 400,
            marginLeft: 40,
          },
          list: {
            gap: 4,
          },
          tab: {
            fontWeight: 500,
            '&[data-active]': {
              backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
              color: isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-7)',
            },
          },
        }}
      >
        <Tabs.List>
          {navItems.map((item) => (
            <Tabs.Tab
              key={item.path}
              value={item.path}
              leftSection={<item.icon size={16} />}
            >
              {item.label}
            </Tabs.Tab>
          ))}
        </Tabs.List>
      </Tabs>

      {/* Theme Toggle */}
      <Tooltip label={isDark ? 'Light mode' : 'Dark mode'} withArrow>
        <ActionIcon
          variant="subtle"
          color={isDark ? 'slate.4' : 'slate.6'}
          size="lg"
          onClick={() => toggleColorScheme()}
        >
          {isDark ? <HiOutlineSun size={20} /> : <HiOutlineMoon size={20} />}
        </ActionIcon>
      </Tooltip>
    </Box>
  );
}
