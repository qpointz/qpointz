import { ActionIcon, Box, Center, Loader, ScrollArea, Stack, Text, Transition, useMantineColorScheme } from '@mantine/core';
import { HiArrowDown } from 'react-icons/hi2';
import { useEffect, useRef } from 'react';
import type { Message } from '../../types/chat';
import { MessageBubble } from './MessageBubble';
import { ThinkingIndicator } from './ThinkingIndicator';
import { ChatEmptyState } from '../common/ChatEmptyState';
import { useAutoScroll } from '../../hooks/useAutoScroll';
import { isPendingAssistantReply } from './chatMessageHelpers';
import {
  CHAT_BOTTOM_PADDING,
  CHAT_CONTENT_MAX_WIDTH,
  chatAccentColor,
  chatTranscriptBackground,
} from './chatChrome';

const GENERAL_CHAT_SUGGESTIONS = [
  'What tables are available?',
  'Summarize revenue by region',
  'Explain this schema to me',
];

interface MessageListProps {
  messages: Message[];
  isLoading: boolean;
  /** True while REST transcript for the active chat has not been merged yet. */
  isTranscriptLoading?: boolean;
  thinkingMessage?: string | null;
  conversationId?: string;
  chatTitle?: string;
  onArtifactsChange?: (messageId: string, artifacts: NonNullable<Message['artifacts']>) => void;
  onSuggestionClick?: (text: string) => void;
  /** When false, hides the floating scroll-to-bottom control (e.g. artifact expand overlay). */
  showScrollToBottomAffix?: boolean;
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
  isTranscriptLoading = false,
  thinkingMessage = null,
  conversationId,
  chatTitle,
  onArtifactsChange,
  onSuggestionClick,
  showScrollToBottomAffix = true,
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

  if (isTranscriptLoading && messages.length === 0) {
    return (
      <Center style={{ flex: 1 }} py="xl">
        <Stack align="center" gap="sm">
          <Loader size="sm" />
          <Text size="sm" c="dimmed">
            Loading conversation…
          </Text>
        </Stack>
      </Center>
    );
  }

  if (messages.length === 0) {
    return (
      <ChatEmptyState
        title="How can I help you today?"
        description="Ask questions about your data, explore schemas, run SQL, and turn results into charts — all in one conversation."
        suggestions={GENERAL_CHAT_SUGGESTIONS}
        onSuggestionClick={onSuggestionClick}
      />
    );
  }

  return (
    <Box
      style={{
        flex: 1,
        position: 'relative',
        overflow: 'hidden',
        background: chatTranscriptBackground(isDark),
      }}
    >
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
            paddingTop: 'var(--mantine-spacing-md)',
            paddingBottom: `${CHAT_BOTTOM_PADDING}px`,
            maxWidth: CHAT_CONTENT_MAX_WIDTH,
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
                    marginBottom: 24,
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

      <Transition mounted={showScrollToBottomAffix && showScrollBottom} transition="slide-up" duration={200}>
        {(styles) => (
          <ActionIcon
            variant="filled"
            color={chatAccentColor(isDark)}
            size="lg"
            radius="xl"
            onClick={() => scrollToBottom()}
            style={{
              ...styles,
              position: 'absolute',
              bottom: `${CHAT_BOTTOM_PADDING + 12}px`,
              right: '24px',
              zIndex: 20,
              boxShadow: isDark
                ? '0 4px 14px rgba(0, 0, 0, 0.45)'
                : '0 4px 14px rgba(0, 0, 0, 0.12)',
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
