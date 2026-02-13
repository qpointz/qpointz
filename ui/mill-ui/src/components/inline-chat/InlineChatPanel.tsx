import { Box, Badge, Group, ScrollArea, Anchor, useMantineColorScheme } from '@mantine/core';
import { useNavigate } from 'react-router';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineAcademicCap,
  HiOutlineBeaker,
} from 'react-icons/hi2';
import type { InlineChatSession } from '../../types/inlineChat';
import { InlineChatMessage } from './InlineChatMessage';
import { InlineChatInput } from './InlineChatInput';
import { TypingIndicator } from '../chat/TypingIndicator';
import { ChatEmptyState } from '../common/ChatEmptyState';
import { useAutoScroll } from '../../hooks/useAutoScroll';

interface InlineChatPanelProps {
  session: InlineChatSession;
  onSend: (message: string) => void;
}

const entityTypeIcons: Record<string, React.ComponentType<{ size: number; color?: string }>> = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  ATTRIBUTE: HiOutlineViewColumns,
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

export function InlineChatPanel({ session, onSend }: InlineChatPanelProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const ContextIcon = getContextIcon(session);

  const { viewportRef } = useAutoScroll({
    deps: [session.messages, session.isLoading],
  });

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
          <Box p="xs" pb={4}>
            {session.messages.map((msg) => (
              <InlineChatMessage key={msg.id} message={msg} />
            ))}
            {session.isLoading &&
              session.messages.length > 0 &&
              session.messages[session.messages.length - 1]?.content === '' && (
                <Box style={{ display: 'flex', justifyContent: 'flex-start', marginLeft: 4 }}>
                  <TypingIndicator />
                </Box>
              )}
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
