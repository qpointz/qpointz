import { Box, Badge, Group, ScrollArea, Anchor, useMantineColorScheme } from '@mantine/core';
import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineAcademicCap,
  HiOutlineBeaker,
} from 'react-icons/hi2';
import type { InlineChatSession } from '../../types/inlineChat';
import type { ChatMessageArtifact } from '../../types/chat';
import { resolveInlineChatType } from '../chat/artifactPreview/hostIntegrations';
import { InlineChatMessage } from './InlineChatMessage';
import { InlineChatInput } from './InlineChatInput';
import { ThinkingIndicator } from '../chat/ThinkingIndicator';
import { ChatEmptyState } from '../common/ChatEmptyState';
import { useAutoScroll } from '../../hooks/useAutoScroll';
import { isPendingAssistantReply } from '../chat/chatMessageHelpers';

interface InlineChatPanelProps {
  session: InlineChatSession;
  onSend: (message: string) => void;
  onArtifactsChange?: (
    messageId: string,
    artifacts: readonly ChatMessageArtifact[],
  ) => void;
}

const entityTypeIcons: Record<string, React.ComponentType<{ size: number; color?: string }>> = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  COLUMN: HiOutlineViewColumns,
};

function getContextIcon(session: InlineChatSession) {
  if (session.contextType === 'knowledge') return HiOutlineAcademicCap;
  if (session.contextType === 'analysis') return HiOutlineBeaker;
  return entityTypeIcons[session.contextEntityType || ''] || HiOutlineCircleStack;
}

function getContextRoute(session: InlineChatSession): string {
  if (session.contextType === 'model') {
    if (session.contextId === '__model__') return '/model';
    return `/model/${session.contextId.replace(/\./g, '/')}`;
  }
  if (session.contextType === 'analysis') {
    if (session.contextId === '__analysis__') return '/analysis';
    return `/analysis/${session.contextId}`;
  }
  if (session.contextId === '__knowledge__') return '/knowledge';
  return `/knowledge/${session.contextId}`;
}

function getEmptyDescription(contextType: string): string {
  if (contextType === 'model') {
    return 'I can help with schema analysis, data quality, relationships, and documentation.';
  }
  if (contextType === 'analysis') {
    return 'I can help optimize SQL, explain results, suggest joins, and build new queries.';
  }
  return 'I can help refine definitions, suggest related concepts, and improve SQL formulas.';
}

export function InlineChatPanel({ session, onSend, onArtifactsChange }: InlineChatPanelProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const ContextIcon = getContextIcon(session);

  const { viewportRef, scrollToBottom, scrollToBottomIfNear } = useAutoScroll();
  const contentRef = useRef<HTMLDivElement>(null);
  const prevMessageCountRef = useRef(session.messages.length);

  useEffect(() => {
    const content = contentRef.current;
    const viewport = viewportRef.current;
    if (!content || !viewport) return;

    const ro = new ResizeObserver(() => {
      scrollToBottomIfNear('auto');
    });
    ro.observe(content);
    return () => ro.disconnect();
  }, [scrollToBottomIfNear, viewportRef]);

  useEffect(() => {
    const prevCount = prevMessageCountRef.current;
    const countIncreased = session.messages.length > prevCount;
    prevMessageCountRef.current = session.messages.length;

    if (countIncreased && session.messages[prevCount]?.role === 'user') {
      scrollToBottom();
      return;
    }

    requestAnimationFrame(() => scrollToBottomIfNear());
  }, [session.messages, session.isLoading, session.thinkingMessage, scrollToBottom, scrollToBottomIfNear]);

  // Build context path for display
  const contextPath =
    session.contextType === 'model'
      ? session.contextId === '__model__'
        ? 'Data Model'
        : session.contextId.replace(/\./g, ' > ')
      : session.contextType === 'analysis'
        ? session.contextId === '__analysis__'
          ? 'Query Playground'
          : session.contextLabel
        : session.contextLabel;

  const contextRoute = getContextRoute(session);
  const chatType = resolveInlineChatType(session.contextType);
  const conversationId = session.chatId ?? session.id;

  return (
    <Box style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
      {/* Context banner */}
      <Box
        px="xs"
        py={6}
        style={{
          borderBottom: `1px solid var(--mantine-color-default-border)`,
          backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-0)',
        }}
      >
        <Group gap={6} wrap="nowrap">
          <ContextIcon
            size={14}
            color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
          />
          <Anchor
            size="xs"
            c={isDark ? 'cyan.4' : 'teal.7'}
            truncate
            underline="hover"
            style={{ flex: 1, fontFamily: 'monospace', fontSize: '11px', cursor: 'pointer' }}
            onClick={(e) => {
              e.preventDefault();
              navigate(contextRoute);
            }}
          >
            {contextPath}
          </Anchor>
          <Badge
            size="xs"
            variant="light"
            color={isDark ? 'cyan' : 'teal'}
          >
            {session.contextType === 'model'
              ? session.contextEntityType || 'Entity'
              : session.contextType === 'analysis'
                ? session.contextEntityType || 'Query'
                : 'Concept'}
          </Badge>
        </Group>
      </Box>

      {/* Messages */}
      {session.messages.length <= 1 ? (
        <ChatEmptyState
          title={`Ask about ${session.contextLabel}`}
          description={getEmptyDescription(session.contextType)}
          compact
        />
      ) : (
        <ScrollArea style={{ flex: 1 }} viewportRef={viewportRef} type="scroll" scrollbarSize={6}>
          <Box ref={contentRef} p="xs" pb={4}>
            {session.messages.map((msg, index) => {
              if (isPendingAssistantReply(msg, index, session.messages, session.isLoading)) {
                return (
                  <Box key={msg.id} px="xs" pb={6}>
                    <ThinkingIndicator message={session.thinkingMessage} />
                  </Box>
                );
              }

              let precedingUserQuestion: string | undefined;
              for (let i = index - 1; i >= 0; i -= 1) {
                const prior = session.messages[i];
                if (prior?.role === 'user') {
                  precedingUserQuestion = prior.content;
                  break;
                }
              }
              return (
                <InlineChatMessage
                  key={msg.id}
                  message={msg}
                  chatType={chatType}
                  conversationId={conversationId}
                  chatTitle={session.contextLabel}
                  precedingUserQuestion={precedingUserQuestion}
                  onArtifactsChange={onArtifactsChange}
                />
              );
            })}
          </Box>
        </ScrollArea>
      )}

      {/* Input */}
      <InlineChatInput
        onSend={onSend}
        disabled={session.isLoading}
        placeholder={`Ask about ${session.contextLabel}...`}
      />
    </Box>
  );
}
