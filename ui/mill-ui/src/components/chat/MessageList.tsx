import { ActionIcon, Box, ScrollArea, Stack, Transition, useMantineColorScheme } from '@mantine/core';
import { HiArrowDown } from 'react-icons/hi2';
import type { Message } from '../../types/chat';
import { MessageBubble } from './MessageBubble';
import { TypingIndicator } from './TypingIndicator';
import { ChatEmptyState } from '../common/ChatEmptyState';
import { useAutoScroll } from '../../hooks/useAutoScroll';

/** Height reserved for the pinned top toolbar overlay */
const TOP_PADDING = 52;
/** Height reserved for the floating bottom input overlay */
const BOTTOM_PADDING = 120;

interface MessageListProps {
  messages: Message[];
  isLoading: boolean;
}

export function MessageList({ messages, isLoading }: MessageListProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const { viewportRef, showScrollBottom, scrollToBottom, handleScroll } = useAutoScroll({
    deps: [messages, isLoading],
  });

  if (messages.length === 0) {
    return (
      <ChatEmptyState
        title="How can I help you today?"
        description="Start a conversation by typing a message below. I can help with coding, answer questions, and assist with various tasks."
      />
    );
  }

  return (
    <Box style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
      <ScrollArea
        style={{ height: '100%' }}
        viewportRef={viewportRef}
        type="scroll"
        scrollbarSize={8}
        onScrollPositionChange={handleScroll}
      >
        <Stack
          gap={0}
          px="md"
          style={{
            paddingTop: `${TOP_PADDING}px`,
            paddingBottom: `${BOTTOM_PADDING}px`,
            maxWidth: '900px',
            margin: '0 auto',
            width: '100%',
          }}
        >
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

      {/* Scroll-to-bottom affix button */}
      <Transition mounted={showScrollBottom} transition="slide-up" duration={200}>
        {(styles) => (
          <ActionIcon
            variant="filled"
            color={isDark ? 'cyan' : 'teal'}
            size="lg"
            radius="xl"
            onClick={scrollToBottom}
            style={{
              ...styles,
              position: 'absolute',
              bottom: `${BOTTOM_PADDING + 12}px`,
              right: '24px',
              zIndex: 20,
              boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
            }}
            aria-label="Scroll to bottom"
          >
            <HiArrowDown size={18} />
          </ActionIcon>
        )}
      </Transition>
    </Box>
  );
}
