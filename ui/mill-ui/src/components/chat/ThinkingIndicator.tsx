import { Box, Text, useMantineColorScheme } from '@mantine/core';
import { TypingIndicator } from './TypingIndicator';
import { AssistantAvatar } from './AssistantAvatar';

interface ThinkingIndicatorProps {
  /** Progress / tool-call line from SSE; omit for dots-only wait state */
  message?: string | null;
}

/**
 * In-thread assistant wait state: animated dots plus optional progress text.
 * Renders where the assistant reply will appear and is replaced once content arrives.
 */
export function ThinkingIndicator({ message }: ThinkingIndicatorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const label = message?.trim() || 'Thinking…';

  return (
    <Box
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: 12,
        maxWidth: '100%',
        width: '100%',
        minHeight: 30,
      }}
    >
      <AssistantAvatar size={30} />
      <Box
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          flex: 1,
          minWidth: 0,
          paddingTop: 4,
        }}
      >
        <TypingIndicator compact />
        <Text
          size="sm"
          c={isDark ? 'gray.4' : 'gray.6'}
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
          {label}
        </Text>
      </Box>
    </Box>
  );
}
