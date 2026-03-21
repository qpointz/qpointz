import { Box, Text, NavLink, Avatar, Group, useMantineColorScheme, TextInput, Button, Select, Stack } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { useParams, useNavigate } from 'react-router';
import { useState } from 'react';
import {
  HiOutlineUserCircle,
  HiOutlineCog6Tooth,
  HiOutlineLockClosed,
  HiOutlineIdentification,
} from 'react-icons/hi2';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { useAuth } from '../../App';
import type { UserProfileResponse } from '../../services/authService';

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

const LOCALE_OPTIONS = [
  { value: 'en', label: 'English' },
  { value: 'fr', label: 'French' },
  { value: 'de', label: 'German' },
  { value: 'es', label: 'Spanish' },
  { value: 'ja', label: 'Japanese' },
];

/** General section — editable display name and email. */
function GeneralSection({ profile }: { profile: UserProfileResponse }) {
  const { updateProfile } = useAuth();
  const [displayName, setDisplayName] = useState(profile.displayName ?? '');
  const [email, setEmail] = useState(profile.email ?? '');
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await updateProfile({
        displayName: displayName || undefined,
        email: email || undefined,
      });
      notifications.show({
        title: 'Profile saved',
        message: 'Your general profile has been updated.',
        color: 'teal',
      });
    } catch {
      notifications.show({
        title: 'Save failed',
        message: 'Could not update your profile. Please try again.',
        color: 'red',
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Stack gap="md" maw={480} p="xl">
      <Text size="lg" fw={600}>General</Text>
      <TextInput
        label="Display name"
        placeholder="Your display name"
        value={displayName}
        onChange={(e) => setDisplayName(e.currentTarget.value)}
      />
      <TextInput
        label="Email"
        placeholder="you@example.com"
        value={email}
        onChange={(e) => setEmail(e.currentTarget.value)}
      />
      <Button onClick={handleSave} loading={saving} color="teal" data-testid="general-save-btn">
        Save
      </Button>
    </Stack>
  );
}

/** Settings section — locale selection. */
function SettingsSection({ profile }: { profile: UserProfileResponse }) {
  const { updateProfile } = useAuth();
  const [locale, setLocale] = useState<string | null>(profile.locale ?? null);
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await updateProfile({ locale: locale ?? undefined });
      notifications.show({
        title: 'Settings saved',
        message: 'Your locale preference has been updated.',
        color: 'teal',
      });
    } catch {
      notifications.show({
        title: 'Save failed',
        message: 'Could not update your settings. Please try again.',
        color: 'red',
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Stack gap="md" maw={480} p="xl">
      <Text size="lg" fw={600}>Settings</Text>
      <Select
        label="Locale"
        placeholder="Select a locale"
        data={LOCALE_OPTIONS}
        value={locale}
        onChange={setLocale}
        data-testid="locale-select"
      />
      <Button onClick={handleSave} loading={saving} color="teal" data-testid="settings-save-btn">
        Save
      </Button>
    </Stack>
  );
}

/** Access section — placeholder for personal access tokens. */
function AccessSection() {
  return (
    <Stack gap="md" maw={480} p="xl">
      <Text size="lg" fw={600}>Access</Text>
      <Text size="sm" c="dimmed" data-testid="access-placeholder">
        Personal access tokens — coming soon.
      </Text>
    </Stack>
  );
}

function ProfilePanel({ section, profile }: { section: ProfileSection; profile: UserProfileResponse | null }) {
  if (section === 'general') {
    return profile ? <GeneralSection profile={profile} /> : null;
  }
  if (section === 'settings') {
    return profile ? <SettingsSection profile={profile} /> : null;
  }
  if (section === 'access') {
    return <AccessSection />;
  }
  return null;
}

export function ProfileLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const params = useParams<{ section?: string }>();
  const { user } = useAuth();

  const profile = user?.profile ?? null;

  const displayName = profile?.displayName ?? user?.displayName ?? null;
  const email = profile?.email ?? user?.email ?? null;
  const initials = displayName
    ? displayName.split(' ').map((w) => w[0]).join('').substring(0, 2).toUpperCase()
    : 'U';

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
              {initials}
            </Avatar>
            <Box style={{ flex: 1, minWidth: 0 }}>
              <Text size="sm" fw={600} lineClamp={1} c={isDark ? 'gray.1' : 'gray.8'}>
                {displayName ?? 'Anonymous'}
              </Text>
              <Text size="xs" c="dimmed" lineClamp={1}>
                {email ?? ''}
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
          <ProfilePanel section={activeSection} profile={profile} />
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
