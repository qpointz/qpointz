import { Box, Text, Transition, useMantineColorScheme } from '@mantine/core';
import { RingsLoader } from '../common/RingsLoader';

interface ThinkingIndicatorProps {
  message: string | null;
}

export function ThinkingIndicator({ message }: ThinkingIndicatorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Transition mounted={!!message} transition="slide-up" duration={200}>
      {(styles) => (
        <Box
          style={{
            ...styles,
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            maxWidth: '900px',
            margin: '0 auto',
            width: '100%',
            paddingLeft: 'var(--mantine-spacing-md)',
            paddingRight: 'var(--mantine-spacing-md)',
            paddingBottom: '6px',
          }}
        >
          <RingsLoader size={16} />
          <Text
            size="xs"
            c={isDark ? 'gray.4' : 'gray.5'}
            style={{
              fontStyle: 'italic',
              userSelect: 'none',
            }}
          >
            {message}
          </Text>
        </Box>
      )}
    </Transition>
  );
}
