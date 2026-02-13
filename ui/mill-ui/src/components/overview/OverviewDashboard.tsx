import { Box, Text, SimpleGrid, Paper, Group, Button, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import {
  HiOutlineSquare3Stack3D,
  HiOutlineTableCells,
  HiOutlineAcademicCap,
  HiOutlineBeaker,
  HiOutlineChatBubbleLeftRight,
  HiOutlineWrenchScrewdriver,
} from 'react-icons/hi2';
import { statsService } from '../../services/api';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import type { DashboardStats } from '../../types/stats';

interface StatCardProps {
  label: string;
  value: number;
  icon: React.ComponentType<{ size: number; color?: string }>;
  path: string;
  isDark: boolean;
}

function StatCard({ label, value, icon: Icon, path, isDark }: StatCardProps) {
  const navigate = useNavigate();

  return (
    <Paper
      shadow="xs"
      p="lg"
      radius="lg"
      style={{
        cursor: 'pointer',
        border: `1px solid var(--mantine-color-default-border)`,
        backgroundColor: 'var(--mantine-color-body)',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease',
      }}
      onClick={() => navigate(path)}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = isDark
          ? '0 4px 12px rgba(0,0,0,0.3)'
          : '0 4px 12px rgba(0,0,0,0.1)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '';
      }}
    >
      <Group justify="space-between" align="flex-start">
        <Box>
          <Text size="xs" c="dimmed" fw={600} tt="uppercase" lts={0.5}>
            {label}
          </Text>
          <Text size="xl" fw={700} mt={4} c={isDark ? 'gray.1' : 'gray.8'}>
            {value}
          </Text>
        </Box>
        <Box
          style={{
            width: 44,
            height: 44,
            borderRadius: '50%',
            backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Icon
            size={22}
            color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
          />
        </Box>
      </Group>
    </Paper>
  );
}

export function OverviewDashboard() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const flags = useFeatureFlags();

  const [dashboardStats, setDashboardStats] = useState<DashboardStats>({
    schemaCount: 0,
    tableCount: 0,
    conceptCount: 0,
    queryCount: 0,
  });

  useEffect(() => {
    statsService.getStats().then(setDashboardStats).catch(() => {
      // Silently fall back to zeros
    });
  }, []);

  const stats = [
    { label: 'Schemas', value: dashboardStats.schemaCount, icon: HiOutlineSquare3Stack3D, path: '/model' },
    { label: 'Tables', value: dashboardStats.tableCount, icon: HiOutlineTableCells, path: '/model' },
    { label: 'Concepts', value: dashboardStats.conceptCount, icon: HiOutlineAcademicCap, path: '/knowledge' },
    { label: 'Queries', value: dashboardStats.queryCount, icon: HiOutlineBeaker, path: '/analysis' },
  ];

  const quickLinks = [
    { label: 'Model', icon: HiOutlineSquare3Stack3D, path: '/model' },
    { label: 'Knowledge', icon: HiOutlineAcademicCap, path: '/knowledge' },
    { label: 'Analysis', icon: HiOutlineBeaker, path: '/analysis' },
    { label: 'Chat', icon: HiOutlineChatBubbleLeftRight, path: '/chat' },
    ...(flags.viewAdmin ? [{ label: 'Admin', icon: HiOutlineWrenchScrewdriver, path: '/admin' }] : []),
  ];

  return (
    <Box
      style={{
        height: '100%',
        overflow: 'auto',
        backgroundColor: 'var(--mantine-color-body)',
      }}
    >
      {/* Header */}
      <Box
        px="xl"
        py="xl"
        style={{
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, var(--mantine-color-body) 100%)',
          borderBottom: `1px solid ${isDark ? 'var(--mantine-color-dark-5)' : 'var(--mantine-color-gray-3)'}`,
        }}
      >
        <Box maw={800} mx="auto">
          <Text size="xl" fw={700} c={isDark ? 'gray.1' : 'gray.8'}>
            Welcome to DataChat
          </Text>
          <Text size="sm" c="dimmed" mt={4}>
            Your data knowledge hub. Browse schemas, explore business concepts, run queries, and chat with AI.
          </Text>
        </Box>
      </Box>

      {/* Content */}
      <Box px="xl" py="lg" maw={800} mx="auto">
        {/* Stat Cards */}
        <Text size="xs" fw={600} c="dimmed" tt="uppercase" lts={0.5} mb="sm">
          At a Glance
        </Text>
        <SimpleGrid cols={{ base: 2, sm: 4 }} spacing="md" mb="xl">
          {stats.map((stat) => (
            <StatCard key={stat.label} {...stat} isDark={isDark} />
          ))}
        </SimpleGrid>

        {/* Quick Links */}
        <Text size="xs" fw={600} c="dimmed" tt="uppercase" lts={0.5} mb="sm">
          Quick Links
        </Text>
        <Group gap="sm">
          {quickLinks.map((link) => (
            <Button
              key={link.path}
              variant="light"
              color={isDark ? 'cyan' : 'teal'}
              leftSection={<link.icon size={16} />}
              onClick={() => navigate(link.path)}
            >
              {link.label}
            </Button>
          ))}
        </Group>
      </Box>
    </Box>
  );
}
