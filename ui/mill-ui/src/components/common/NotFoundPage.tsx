import { Box, Text, Button, Group, useMantineColorScheme } from '@mantine/core';
import { useNavigate } from 'react-router';
import { HiOutlineExclamationTriangle, HiOutlineHome } from 'react-icons/hi2';

interface NotFoundPageProps {
  /** Optional message override, e.g. "Table 'foo' not found" */
  message?: string;
}

export function NotFoundPage({ message }: NotFoundPageProps) {
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
          backgroundColor: isDark ? 'var(--mantine-color-orange-9)' : 'var(--mantine-color-orange-0)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 24,
        }}
      >
        <HiOutlineExclamationTriangle
          size={44}
          color={isDark ? 'var(--mantine-color-orange-4)' : 'var(--mantine-color-orange-6)'}
        />
      </Box>

      <Text
        size="lg"
        fw={700}
        c={isDark ? 'gray.2' : 'gray.7'}
        style={{ letterSpacing: 2, textTransform: 'uppercase' }}
        mb={4}
      >
        404
      </Text>

      <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
        Page not found
      </Text>

      <Text size="sm" c="dimmed" ta="center" maw={420} mb="xl">
        {message || 'The resource you are looking for doesn\u2019t exist or may have been moved. Check the URL and try again.'}
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
