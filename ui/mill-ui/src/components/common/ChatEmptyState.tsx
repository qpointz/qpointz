import { Box, Text, useMantineColorScheme } from '@mantine/core';
import { HiOutlineSparkles } from 'react-icons/hi2';

interface ChatEmptyStateProps {
  title: string;
  description: string;
  /** Compact mode for inline chat (smaller sizing) */
  compact?: boolean;
}

export function ChatEmptyState({ title, description, compact = false }: ChatEmptyStateProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const iconSize = compact ? 48 : 80;
  const sparkleSize = compact ? 22 : 36;

  return (
    <Box
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: compact ? '24px' : '48px',
      }}
    >
      <Box
        style={{
          width: `${iconSize}px`,
          height: `${iconSize}px`,
          borderRadius: '50%',
          backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: compact ? '12px' : '24px',
        }}
      >
        <HiOutlineSparkles
          size={sparkleSize}
          color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
        />
      </Box>
      <Text
        size={compact ? 'sm' : 'xl'}
        fw={compact ? 500 : 600}
        mb={compact ? 4 : 'xs'}
        c={isDark ? (compact ? 'gray.2' : 'gray.1') : (compact ? 'gray.7' : 'gray.8')}
        ta="center"
      >
        {title}
      </Text>
      <Text
        size={compact ? 'xs' : 'sm'}
        c={compact ? 'dimmed' : (isDark ? 'gray.4' : 'gray.5')}
        ta="center"
        maw={compact ? 260 : 400}
      >
        {description}
      </Text>
    </Box>
  );
}
