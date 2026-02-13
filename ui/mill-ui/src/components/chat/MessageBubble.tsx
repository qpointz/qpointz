import { Box, Paper, Text, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import { MessageContent } from '../common/MessageContent';

interface MessageBubbleProps {
  message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const { colorScheme } = useMantineColorScheme();
  const isUser = message.role === 'user';
  const isDark = colorScheme === 'dark';

  const userBgColor = isDark ? 'var(--mantine-color-cyan-7)' : 'var(--mantine-color-teal-6)';
  const assistantTextColor = 'var(--mantine-color-text)';
  const userTextColor = 'white';

  // User messages keep the bubble style
  if (isUser) {
    return (
      <Box
        style={{
          display: 'flex',
          justifyContent: 'flex-end',
          marginBottom: '12px',
          paddingLeft: '48px',
        }}
      >
        <Paper
          shadow="sm"
          p="sm"
          style={{
            backgroundColor: userBgColor,
            color: userTextColor,
            maxWidth: '100%',
            borderRadius: '16px 16px 4px 16px',
          }}
        >
          <Text size="sm" style={{ whiteSpace: 'pre-wrap' }}>
            {message.content}
          </Text>
        </Paper>
      </Box>
    );
  }

  // Assistant messages — borderless, blends into chat content
  return (
    <Box
      style={{
        display: 'flex',
        justifyContent: 'flex-start',
        marginBottom: '16px',
        color: assistantTextColor,
        maxWidth: '100%',
        width: '100%',
      }}
    >
      <MessageContent content={message.content} />
    </Box>
  );
}
