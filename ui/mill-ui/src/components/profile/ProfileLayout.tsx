import { Box, Text, NavLink, Avatar, Group, useMantineColorScheme } from '@mantine/core';
import { useParams, useNavigate } from 'react-router';
import {
  HiOutlineUserCircle,
  HiOutlineCog6Tooth,
  HiOutlineLockClosed,
  HiOutlineIdentification,
} from 'react-icons/hi2';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

type ProfileSection = 'general' | 'settings' | 'access';

interface ProfileNavItem {
  id: ProfileSection;
  label: string;
  icon: React.ComponentType<{ size: number }>;
  flagKey: 'profileGeneral' | 'profileSettings' | 'profileAccess';
}

const profileNavItems: ProfileNavItem[] = [
  { id: 'general', label: 'General', icon: HiOutlineIdentification, flagKey: 'profileGeneral' },
  { id: 'settings', label: 'Settings', icon: HiOutlineCog6Tooth, flagKey: 'profileSettings' },
  { id: 'access', label: 'Access', icon: HiOutlineLockClosed, flagKey: 'profileAccess' },
];

function ProfilePanel({ section }: { section: ProfileSection }) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const meta = profileNavItems.find((i) => i.id === section);
  const Icon = meta?.icon ?? HiOutlineUserCircle;
  const label = meta?.label ?? section;

  return (
    <Box
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Box
        style={{
          width: 80,
          height: 80,
          borderRadius: '50%',
          backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 24,
        }}
      >
        <Icon size={36} />
      </Box>
      <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
        {label}
      </Text>
      <Text size="sm" c="dimmed" ta="center" maw={400}>
        Manage your {label.toLowerCase()} preferences. This section is under construction.
      </Text>
    </Box>
  );
}

export function ProfileLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const params = useParams<{ section?: string }>();

  const visibleItems = profileNavItems.filter((item) => flags[item.flagKey]);

  const activeSection = (params.section as ProfileSection) || null;

  const handleSelect = (item: ProfileNavItem) => {
    navigate(`/profile/${item.id}`);
  };

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar */}
      <CollapsibleSidebar icon={HiOutlineUserCircle} title="Profile">
        {/* User identity card at top */}
        <Box
          px="sm"
          py="md"
          style={{
            borderBottom: `1px solid var(--mantine-color-default-border)`,
          }}
        >
          <Group gap="sm" wrap="nowrap">
            <Avatar size={40} radius="xl" color={isDark ? 'cyan' : 'teal'}>
              DC
            </Avatar>
            <Box style={{ flex: 1, minWidth: 0 }}>
              <Text size="sm" fw={600} lineClamp={1} c={isDark ? 'gray.1' : 'gray.8'}>
                Demo User
              </Text>
              <Text size="xs" c="dimmed" lineClamp={1}>
                demo@datachat.io
              </Text>
            </Box>
          </Group>
        </Box>

        {/* Navigation */}
        <Box
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: 'var(--mantine-spacing-xs)',
          }}
        >
          {visibleItems.map((item) => (
            <NavLink
              key={item.id}
              label={item.label}
              leftSection={<item.icon size={16} />}
              active={activeSection === item.id}
              onClick={() => handleSelect(item)}
              variant="light"
              color={isDark ? 'cyan' : 'teal'}
              style={{ borderRadius: 'var(--mantine-radius-sm)' }}
            />
          ))}
          {visibleItems.length === 0 && (
            <Text size="xs" c="dimmed" ta="center" py="md">
              No profile sections enabled.
            </Text>
          )}
        </Box>
      </CollapsibleSidebar>

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: 'var(--mantine-color-body)',
          overflow: 'hidden',
        }}
      >
        {activeSection && visibleItems.some((i) => i.id === activeSection) ? (
          <ProfilePanel section={activeSection} />
        ) : (
          <Box
            style={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Box
              style={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: 24,
              }}
            >
              <HiOutlineUserCircle
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
              User Profile
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Select a section from the sidebar to manage your profile, settings, and preferences.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
