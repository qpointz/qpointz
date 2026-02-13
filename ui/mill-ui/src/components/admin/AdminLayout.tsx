import { Box, Text, NavLink, useMantineColorScheme } from '@mantine/core';
import { useParams, useNavigate } from 'react-router';
import {
  HiOutlineCog6Tooth,
  HiOutlineCircleStack,
  HiOutlineShieldCheck,
  HiOutlineServerStack,
  HiOutlineWrenchScrewdriver,
} from 'react-icons/hi2';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

type AdminSection = 'data-sources' | 'policies' | 'services' | 'settings';

interface AdminNavItem {
  id: AdminSection;
  label: string;
  icon: React.ComponentType<{ size: number }>;
  flagKey: 'adminDataSources' | 'adminPolicies' | 'adminServices' | 'adminSettings';
}

const adminNavItems: AdminNavItem[] = [
  { id: 'data-sources', label: 'Data Sources', icon: HiOutlineCircleStack, flagKey: 'adminDataSources' },
  { id: 'policies', label: 'Policies', icon: HiOutlineShieldCheck, flagKey: 'adminPolicies' },
  { id: 'services', label: 'Services', icon: HiOutlineServerStack, flagKey: 'adminServices' },
  { id: 'settings', label: 'Settings', icon: HiOutlineCog6Tooth, flagKey: 'adminSettings' },
];

function AdminPanel({ section }: { section: AdminSection }) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const meta = adminNavItems.find((i) => i.id === section);
  const Icon = meta?.icon ?? HiOutlineCog6Tooth;
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
        Manage and configure {label.toLowerCase()} for your workspace. This section is under construction.
      </Text>
    </Box>
  );
}

export function AdminLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const params = useParams<{ section?: string }>();

  const visibleItems = adminNavItems.filter((item) => flags[item.flagKey]);

  const activeSection = (params.section as AdminSection) || null;

  const handleSelect = (item: AdminNavItem) => {
    navigate(`/admin/${item.id}`);
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
      <CollapsibleSidebar icon={HiOutlineCog6Tooth} title="Admin">
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
              No admin sections enabled.
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
          <AdminPanel section={activeSection} />
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
              <HiOutlineWrenchScrewdriver
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
              Admin Area
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Select a section from the sidebar to manage data sources, policies, and services.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
