import { Box, Text, NavLink, useMantineColorScheme } from '@mantine/core';
import { useLocation, useNavigate } from 'react-router';
import {
  HiOutlineCog6Tooth,
  HiOutlineCircleStack,
  HiOutlineShieldCheck,
  HiOutlineServerStack,
  HiOutlineSquares2X2,
  HiOutlineRectangleStack,
  HiOutlineWrenchScrewdriver,
} from 'react-icons/hi2';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { FacetTypesListPage } from './model/FacetTypesListPage';
import { FacetTypeEditPage } from './model/FacetTypeEditPage';

type AdminGroup = 'system' | 'model';
type SystemSection = 'data-sources' | 'policies' | 'services' | 'settings';

interface AdminNavItem {
  id: string;
  label: string;
  icon: React.ComponentType<{ size: number }>;
  path: string;
  visible: boolean;
}

function SystemPanel({ section }: { section: SystemSection }) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const items = [
    { id: 'data-sources', label: 'Data Sources', icon: HiOutlineCircleStack },
    { id: 'policies', label: 'Policies', icon: HiOutlineShieldCheck },
    { id: 'services', label: 'Services', icon: HiOutlineServerStack },
    { id: 'settings', label: 'Settings', icon: HiOutlineCog6Tooth },
  ];

  const meta = items.find((i) => i.id === section);
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
  const location = useLocation();
  const segments = location.pathname.replace(/^\/admin\/?/, '').split('/').filter(Boolean);

  const group = (segments[0] as AdminGroup | undefined) ?? 'system';
  const section = segments[1] ?? null;
  const extra = segments.slice(2);

  const systemItems: AdminNavItem[] = [
    { id: 'data-sources', label: 'Data Sources', icon: HiOutlineCircleStack, path: '/admin/system/data-sources', visible: flags.adminDataSources },
    { id: 'policies', label: 'Policies', icon: HiOutlineShieldCheck, path: '/admin/system/policies', visible: flags.adminPolicies },
    { id: 'services', label: 'Services', icon: HiOutlineServerStack, path: '/admin/system/services', visible: flags.adminServices },
    { id: 'settings', label: 'Settings', icon: HiOutlineCog6Tooth, path: '/admin/system/settings', visible: flags.adminSettings },
  ].filter((i) => i.visible);

  const modelItems: AdminNavItem[] = [
    { id: 'facet-types', label: 'Facet Types', icon: HiOutlineRectangleStack, path: '/admin/model/facet-types', visible: flags.adminModelNavEnabled && flags.adminFacetTypesEnabled },
  ].filter((i) => i.visible);

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
          <NavLink
            label="System"
            leftSection={<HiOutlineSquares2X2 size={16} />}
            defaultOpened
            active={group === 'system'}
            childrenOffset={18}
          >
            {systemItems.map((item) => (
              <NavLink
                key={item.id}
                label={item.label}
                leftSection={<item.icon size={16} />}
                active={location.pathname.startsWith(item.path)}
                onClick={() => navigate(item.path)}
                variant="light"
                color={isDark ? 'cyan' : 'teal'}
                style={{ borderRadius: 'var(--mantine-radius-sm)' }}
              />
            ))}
          </NavLink>

          {flags.adminModelNavEnabled && (
            <NavLink
              label="Model"
              leftSection={<HiOutlineRectangleStack size={16} />}
              defaultOpened
              active={group === 'model'}
              childrenOffset={18}
            >
              {modelItems.map((item) => (
                <NavLink
                  key={item.id}
                  label={item.label}
                  leftSection={<item.icon size={16} />}
                  active={location.pathname.startsWith(item.path)}
                  onClick={() => navigate(item.path)}
                  variant="light"
                  color={isDark ? 'cyan' : 'teal'}
                  style={{ borderRadius: 'var(--mantine-radius-sm)' }}
                />
              ))}
            </NavLink>
          )}

          {systemItems.length === 0 && modelItems.length === 0 && (
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
          overflowY: 'auto',
          overflowX: 'hidden',
        }}
      >
        {group === 'model' && section === 'facet-types' && extra.length === 0 && (
          <FacetTypesListPage readOnly={flags.facetTypesReadOnly} />
        )}
        {group === 'model' && section === 'facet-types' && extra.length === 1 && extra[0] === 'new' && (
          <FacetTypeEditPage mode="create" readOnly={flags.facetTypesReadOnly} />
        )}
        {group === 'model' && section === 'facet-types' && extra.length === 2 && extra[1] === 'edit' && (
          <FacetTypeEditPage
            mode="edit"
            typeKey={decodeURIComponent(extra[0] ?? '')}
            readOnly={flags.facetTypesReadOnly}
          />
        )}

        {group === 'system' && section && systemItems.some((i) => i.id === section) && (
          <SystemPanel section={section as SystemSection} />
        )}

        {!(
          (group === 'model' && section === 'facet-types') ||
          (group === 'system' && section && systemItems.some((i) => i.id === section))
        ) && (
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
              Select a section from the sidebar to manage system and model administration.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
