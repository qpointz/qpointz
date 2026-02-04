import { Box, ScrollArea, Text, Stack, useMantineColorScheme } from '@mantine/core';
import { useEffect, useRef } from 'react';
import { HiOutlineSparkles } from 'react-icons/hi2';
import type { Message } from '../../types/chat';
import { MessageBubble } from './MessageBubble';
import { TypingIndicator } from './TypingIndicator';

interface MessageListProps {
  messages: Message[];
  isLoading: boolean;
}

export function MessageList({ messages, isLoading }: MessageListProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const viewportRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (viewportRef.current) {
      viewportRef.current.scrollTo({
        top: viewportRef.current.scrollHeight,
        behavior: 'smooth',
      });
    }
  }, [messages, isLoading]);

  if (messages.length === 0) {
    return (
      <Box
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '48px',
        }}
      >
        <Box
          style={{
            width: '80px',
            height: '80px',
            borderRadius: '50%',
            backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '24px',
          }}
        >
          <HiOutlineSparkles
            size={36}
            color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
          />
        </Box>
        <Text
          size="xl"
          fw={600}
          mb="xs"
          c={isDark ? 'slate.1' : 'slate.8'}
        >
          How can I help you today?
        </Text>
        <Text
          size="sm"
          c={isDark ? 'slate.4' : 'slate.5'}
          ta="center"
          maw={400}
        >
          Start a conversation by typing a message below. I can help with coding, answer questions, and assist with various tasks.
        </Text>
      </Box>
    );
  }

  return (
    <ScrollArea
      style={{ flex: 1 }}
      viewportRef={viewportRef}
      type="scroll"
      scrollbarSize={8}
    >
      <Stack gap={0} p="md">
        {messages.map((message) => (
          <MessageBubble key={message.id} message={message} />
        ))}
        {isLoading && messages[messages.length - 1]?.content === '' && (
          <Box style={{ display: 'flex', justifyContent: 'flex-start' }}>
            <TypingIndicator />
          </Box>
        )}
      </Stack>
    </ScrollArea>
  );
}
