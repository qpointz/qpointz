import { Box, Text, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import { MessageContent } from '../common/MessageContent';

interface InlineChatMessageProps {
  message: Message;
}

export function InlineChatMessage({ message }: InlineChatMessageProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const isUser = message.role === 'user';

  const userBg = isDark ? 'var(--mantine-color-cyan-8)' : 'var(--mantine-color-teal-6)';
  const assistantBg = isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-1)';
  const userColor = 'white';
  const assistantColor = 'var(--mantine-color-text)';

  return (
    <Box
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 6,
        paddingLeft: isUser ? 32 : 0,
        paddingRight: isUser ? 0 : 32,
      }}
    >
      <Box
        px="xs"
        py={6}
        style={{
          backgroundColor: isUser ? userBg : assistantBg,
          color: isUser ? userColor : assistantColor,
          borderRadius: isUser ? '10px 10px 3px 10px' : '10px 10px 10px 3px',
          maxWidth: '100%',
          minWidth: 0,
        }}
      >
        {isUser ? (
          <Text size="xs" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.4 }}>
            {message.content}
          </Text>
        ) : (
          <MessageContent content={message.content} compact />
        )}
      </Box>
    </Box>
  );
}
