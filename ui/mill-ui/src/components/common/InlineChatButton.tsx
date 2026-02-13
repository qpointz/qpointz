import { useState } from 'react';
import {
  ActionIcon,
  Tooltip,
  Indicator,
  Badge,
  Box,
  Text,
  Divider,
  Popover,
  ScrollArea,
  Button,
  Group,
  useMantineColorScheme,
} from '@mantine/core';
import {
  HiOutlineChatBubbleLeftRight,
  HiOutlineChatBubbleOvalLeft,
  HiOutlinePlus,
  HiOutlineArrowTopRightOnSquare,
} from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { useInlineChat } from '../../context/InlineChatContext';
import { useChatReferences } from '../../context/ChatReferencesContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import type { InlineChatContextType } from '../../types/inlineChat';

interface InlineChatButtonProps {
  contextType: InlineChatContextType;
  contextId: string;
  contextLabel: string;
  contextEntityType?: string;
}

export function InlineChatButton({
  contextType,
  contextId,
  contextLabel,
  contextEntityType,
}: InlineChatButtonProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const { startSession, getSessionByContextId, openDrawer, setActiveSession } = useInlineChat();
  const { refs } = useChatReferences(contextType, contextId);
  const [popoverOpen, setPopoverOpen] = useState(false);

  // Global kill-switch (inline chat)
  if (!flags.inlineChatEnabled) return null;

  // Context-type level flags
  if (contextType === 'model') {
    if (!flags.inlineChatModelContext) return null;
    if (contextEntityType === 'SCHEMA' && !flags.inlineChatModelSchema) return null;
    if (contextEntityType === 'TABLE' && !flags.inlineChatModelTable) return null;
    if (contextEntityType === 'ATTRIBUTE' && !flags.inlineChatModelColumn) return null;
  }
  if (contextType === 'knowledge' && !flags.inlineChatKnowledgeContext) return null;
  if (contextType === 'analysis' && !flags.inlineChatAnalysisContext) return null;

  const existingSession = getSessionByContextId(contextId);
  const hasActiveChat = !!existingSession;
  const hasRelatedChats = refs.length > 0;

  const handleClick = () => {
    if (existingSession) {
      // Has inline session → open drawer
      setActiveSession(existingSession.id);
      openDrawer();
    } else if (hasRelatedChats) {
      // No inline session, but has related chats → show popover
      setPopoverOpen((o) => !o);
    } else {
      // No inline session, no related chats → start new inline session
      startSession(contextType, contextId, contextLabel, contextEntityType);
    }
  };

  const handleStartInlineChat = () => {
    setPopoverOpen(false);
    startSession(contextType, contextId, contextLabel, contextEntityType);
  };

  const handleNavigateToChat = (conversationId: string) => {
    setPopoverOpen(false);
    navigate(`/chat`);
    // Note: conversationId can be used when backend navigation is implemented
    void conversationId;
  };

  const tooltip = hasActiveChat
    ? `Open chat for ${contextLabel}`
    : hasRelatedChats
      ? `${refs.length} related conversation${refs.length > 1 ? 's' : ''}`
      : `Start chat about ${contextLabel}`;

  return (
    <Popover
      opened={popoverOpen}
      onChange={setPopoverOpen}
      position="bottom-end"
      width={280}
      shadow="md"
      withArrow
      arrowSize={8}
    >
      <Popover.Target>
        <Tooltip label={tooltip} withArrow disabled={popoverOpen}>
          <Box style={{ position: 'relative', display: 'inline-flex' }}>
            <Indicator
              disabled={!hasActiveChat}
              size={8}
              color="red"
              offset={4}
              processing
            >
              <ActionIcon
                variant="subtle"
                color={isDark ? 'gray.4' : 'gray.6'}
                size="lg"
                onClick={handleClick}
              >
                <HiOutlineChatBubbleLeftRight size={20} />
              </ActionIcon>
            </Indicator>
            {/* Related chats count badge */}
            {hasRelatedChats && (
              <Badge
                size="xs"
                variant="filled"
                color="violet"
                circle
                style={{
                  position: 'absolute',
                  bottom: -2,
                  right: -2,
                  minWidth: 16,
                  height: 16,
                  padding: 0,
                  fontSize: '10px',
                  pointerEvents: 'none',
                  zIndex: 1,
                }}
              >
                {refs.length}
              </Badge>
            )}
          </Box>
        </Tooltip>
      </Popover.Target>

      <Popover.Dropdown
        p={0}
        style={{
          backgroundColor: 'var(--mantine-color-body)',
          border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
        }}
      >
        {/* Start inline chat action */}
        <Box p="xs">
          <Button
            variant="light"
            color={isDark ? 'cyan' : 'teal'}
            size="compact-sm"
            fullWidth
            leftSection={<HiOutlinePlus size={14} />}
            onClick={handleStartInlineChat}
            styles={{
              root: { fontWeight: 500 },
            }}
          >
            Start chat about {contextLabel}
          </Button>
        </Box>

        <Divider color={isDark ? 'gray.7' : 'gray.3'} />

        {/* Related conversations list */}
        <Box px="xs" pt={6} pb={4}>
          <Group gap={4} mb={6}>
            <HiOutlineChatBubbleOvalLeft
              size={12}
              color={isDark ? 'var(--mantine-color-violet-4)' : 'var(--mantine-color-violet-6)'}
            />
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} style={{ fontSize: '10px', letterSpacing: '0.5px' }}>
              Related conversations ({refs.length})
            </Text>
          </Group>
          <ScrollArea.Autosize mah={200} type="scroll" scrollbarSize={6}>
            {refs.map((ref) => (
              <Box
                key={ref.id}
                px="xs"
                py={4}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  borderRadius: 4,
                  cursor: 'pointer',
                  transition: 'background-color 150ms ease',
                }}
                onClick={() => handleNavigateToChat(ref.id)}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = isDark
                    ? 'var(--mantine-color-dark-6)'
                    : 'var(--mantine-color-gray-1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <HiOutlineChatBubbleOvalLeft
                  size={14}
                  color={isDark ? 'var(--mantine-color-violet-4)' : 'var(--mantine-color-violet-6)'}
                  style={{ flexShrink: 0 }}
                />
                <Text
                  size="xs"
                  c={isDark ? 'gray.2' : 'gray.7'}
                  truncate
                  style={{ flex: 1 }}
                >
                  {ref.title}
                </Text>
                <HiOutlineArrowTopRightOnSquare
                  size={12}
                  color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
                  style={{ flexShrink: 0 }}
                />
              </Box>
            ))}
          </ScrollArea.Autosize>
        </Box>
      </Popover.Dropdown>
    </Popover>
  );
}
