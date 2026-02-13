import { Box, Text, NavLink, useMantineColorScheme } from '@mantine/core';
import { useParams, useNavigate } from 'react-router';
import {
  HiOutlineServerStack,
  HiOutlineCommandLine,
  HiOutlineCodeBracket,
  HiOutlineLink,
} from 'react-icons/hi2';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

type ConnectSection = 'services' | 'python' | 'java';

interface ConnectNavItem {
  id: ConnectSection;
  label: string;
  icon: React.ComponentType<{ size: number }>;
  flagKey: 'connectServices' | 'connectPython' | 'connectJava';
}

const connectNavItems: ConnectNavItem[] = [
  { id: 'services', label: 'Services', icon: HiOutlineServerStack, flagKey: 'connectServices' },
  { id: 'python', label: 'Python', icon: HiOutlineCommandLine, flagKey: 'connectPython' },
  { id: 'java', label: 'Java', icon: HiOutlineCodeBracket, flagKey: 'connectJava' },
];

function ConnectPanel({ section }: { section: ConnectSection }) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const meta = connectNavItems.find((i) => i.id === section);
  const Icon = meta?.icon ?? HiOutlineServerStack;
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
        Connect and configure {label.toLowerCase()} integrations for your workspace. This section is under construction.
      </Text>
    </Box>
  );
}

export function ConnectLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const params = useParams<{ section?: string }>();

  const visibleItems = connectNavItems.filter((item) => flags[item.flagKey]);

  const activeSection = (params.section as ConnectSection) || null;

  const handleSelect = (item: ConnectNavItem) => {
    navigate(`/connect/${item.id}`);
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
      <CollapsibleSidebar icon={HiOutlineLink} title="Connect">
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
              label={<Text size="sm">{item.label}</Text>}
              leftSection={<item.icon size={14} />}
              active={activeSection === item.id}
              onClick={() => handleSelect(item)}
              variant="light"
              color={isDark ? 'cyan' : 'teal'}
              style={{ borderRadius: 6, padding: '4px 8px' }}
            />
          ))}
          {visibleItems.length === 0 && (
            <Text size="xs" c="dimmed" ta="center" py="md">
              No connect sections enabled.
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
          <ConnectPanel section={activeSection} />
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
              <HiOutlineLink
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
              Connect
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Select a section from the sidebar to configure services, Python, and Java integrations.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
