import { Box, Text, Button, Group, useMantineColorScheme } from '@mantine/core';
import { useNavigate } from 'react-router';
import { HiOutlineShieldExclamation, HiOutlineHome } from 'react-icons/hi2';

interface AccessDeniedPageProps {
  /** Optional message override, e.g. "You don't have access to this data source" */
  message?: string;
}

export function AccessDeniedPage({ message }: AccessDeniedPageProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();

  return (
    <Box
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 40,
      }}
    >
      <Box
        style={{
          width: 96,
          height: 96,
          borderRadius: '50%',
          backgroundColor: isDark ? 'var(--mantine-color-red-9)' : 'var(--mantine-color-red-0)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 24,
        }}
      >
        <HiOutlineShieldExclamation
          size={44}
          color={isDark ? 'var(--mantine-color-red-4)' : 'var(--mantine-color-red-6)'}
        />
      </Box>

      <Text
        size="lg"
        fw={700}
        c={isDark ? 'gray.2' : 'gray.7'}
        style={{ letterSpacing: 2, textTransform: 'uppercase' }}
        mb={4}
      >
        403
      </Text>

      <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
        Access denied
      </Text>

      <Text size="sm" c="dimmed" ta="center" maw={420} mb="xl">
        {message || 'You don\u2019t have permission to view this resource. Contact your administrator if you believe this is an error.'}
      </Text>

      <Group gap="sm">
        <Button
          variant="light"
          color={isDark ? 'cyan' : 'teal'}
          leftSection={<HiOutlineHome size={16} />}
          onClick={() => navigate('/home')}
        >
          Go to Home
        </Button>
        <Button
          variant="subtle"
          color="gray"
          onClick={() => navigate(-1)}
        >
          Go back
        </Button>
      </Group>
    </Box>
  );
}
