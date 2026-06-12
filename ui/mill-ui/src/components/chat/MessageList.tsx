import { ActionIcon, Box, ScrollArea, Stack, Transition, useMantineColorScheme } from '@mantine/core';
import { HiArrowDown } from 'react-icons/hi2';
import { useEffect, useRef } from 'react';
import type { Message } from '../../types/chat';
import { MessageBubble } from './MessageBubble';
import { ThinkingIndicator } from './ThinkingIndicator';
import { ChatEmptyState } from '../common/ChatEmptyState';
import { useAutoScroll } from '../../hooks/useAutoScroll';
import { isPendingAssistantReply } from './chatMessageHelpers';

/** Height reserved for the floating bottom input overlay */
const BOTTOM_PADDING = 120;

interface MessageListProps {
  messages: Message[];
  isLoading: boolean;
  thinkingMessage?: string | null;
  conversationId?: string;
  chatTitle?: string;
  onArtifactsChange?: (messageId: string, artifacts: NonNullable<Message['artifacts']>) => void;
}

function precedingUserQuestion(messages: Message[], index: number): string | undefined {
  for (let i = index - 1; i >= 0; i -= 1) {
    const msg = messages[i];
    if (msg?.role === 'user') return msg.content;
  }
  return undefined;
}

export function MessageList({
  messages,
  isLoading,
  thinkingMessage = null,
  conversationId,
  chatTitle,
  onArtifactsChange,
}: MessageListProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const {
    viewportRef,
    showScrollBottom,
    scrollToBottom,
    scrollToBottomIfNear,
    handleScroll,
  } = useAutoScroll();
  const stackRef = useRef<HTMLDivElement>(null);
  const prevMessageCountRef = useRef(messages.length);

  useEffect(() => {
    const stack = stackRef.current;
    const viewport = viewportRef.current;
    if (!stack || !viewport) return;

    const ro = new ResizeObserver(() => {
      scrollToBottomIfNear('auto');
    });
    ro.observe(stack);
    return () => ro.disconnect();
  }, [scrollToBottomIfNear, viewportRef]);

  useEffect(() => {
    const prevCount = prevMessageCountRef.current;
    const countIncreased = messages.length > prevCount;
    prevMessageCountRef.current = messages.length;

    if (countIncreased && messages[prevCount]?.role === 'user') {
      scrollToBottom();
      return;
    }

    requestAnimationFrame(() => scrollToBottomIfNear());
  }, [messages, isLoading, thinkingMessage, scrollToBottom, scrollToBottomIfNear]);

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
          ref={stackRef}
          gap={0}
          px="md"
          style={{
            paddingTop: 'var(--mantine-spacing-sm)',
            paddingBottom: `${BOTTOM_PADDING}px`,
            maxWidth: '900px',
            margin: '0 auto',
            width: '100%',
          }}
        >
          {messages.map((message, index) => {
            if (isPendingAssistantReply(message, index, messages, isLoading)) {
              return (
                <Box
                  key={message.id}
                  id={`message-${message.id}`}
                  style={{
                    display: 'flex',
                    justifyContent: 'flex-start',
                    marginBottom: '16px',
                    maxWidth: '100%',
                    width: '100%',
                  }}
                >
                  <ThinkingIndicator message={thinkingMessage} />
                </Box>
              );
            }

            return (
              <MessageBubble
                key={message.id}
                message={message}
                conversationId={conversationId ?? message.conversationId}
                chatTitle={chatTitle}
                precedingUserQuestion={precedingUserQuestion(messages, index)}
                onArtifactsChange={onArtifactsChange}
              />
            );
          })}
        </Stack>
      </ScrollArea>

      <Transition mounted={showScrollBottom} transition="slide-up" duration={200}>
        {(styles) => (
          <ActionIcon
            variant="filled"
            color={isDark ? 'cyan' : 'teal'}
            size="lg"
            radius="xl"
            onClick={() => scrollToBottom()}
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
