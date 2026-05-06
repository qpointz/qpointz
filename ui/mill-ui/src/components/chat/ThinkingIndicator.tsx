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
            alignItems: 'flex-start',
            gap: '10px',
            maxWidth: '900px',
            margin: '0 auto',
            width: '100%',
            paddingLeft: 'var(--mantine-spacing-md)',
            paddingRight: 'var(--mantine-spacing-md)',
            paddingBottom: '8px',
          }}
        >
          <Box
            style={{
              flexShrink: 0,
              paddingTop: '1px',
              lineHeight: 0,
            }}
          >
            <RingsLoader size={16} />
          </Box>
          <Text
            size="xs"
            c={isDark ? 'gray.4' : 'gray.5'}
            style={{
              flex: 1,
              minWidth: 0,
              fontStyle: 'italic',
              userSelect: 'none',
              lineHeight: 1.45,
              whiteSpace: 'pre-wrap',
              overflowWrap: 'anywhere',
            }}
          >
            {message}
          </Text>
        </Box>
      )}
    </Transition>
  );
}
