import {
  Box,
  Text,
  ActionIcon,
  Button,
  Tooltip,
  Badge,
  Group,
  Popover,
  ScrollArea,
  useMantineColorScheme,
} from '@mantine/core';
import { useState, useCallback, useRef } from 'react';
import { useLocation } from 'react-router';
import {
  HiOutlineChatBubbleLeftRight,
  HiOutlineChatBubbleOvalLeft,
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineAcademicCap,
  HiOutlineLink,
  HiOutlineLightBulb,
  HiOutlineBeaker,
  HiXMark,
  HiChevronRight,
  HiChevronDown,
  HiOutlineArrowUturnLeft,
  HiOutlineArrowTopRightOnSquare,
  HiOutlineTrash,
} from 'react-icons/hi2';
import { useNavigate } from 'react-router';
import { useInlineChat } from '../../context/InlineChatContext';
import { useChatReferences } from '../../context/ChatReferencesContext';
import { useRelatedContent } from '../../context/RelatedContentContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { RelatedModelTree } from '../common/RelatedModelTree';
import { InlineChatPanel } from './InlineChatPanel';
import type { InlineChatSession } from '../../types/inlineChat';
import type { RelatedContentRef, RelatedContentType } from '../../types/relatedContent';

const DRAWER_WIDTH = 380;

const entityTypeIcons: Record<string, React.ComponentType<{ size: number; color?: string }>> = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  ATTRIBUTE: HiOutlineViewColumns,
};

function getSessionIcon(session: InlineChatSession) {
  if (session.contextType === 'knowledge') return HiOutlineAcademicCap;
  return entityTypeIcons[session.contextEntityType || ''] || HiOutlineCircleStack;
}

/** Determine the current route's context type to group "most relevant" sessions */
function useRouteContextType(pathname: string): 'model' | 'knowledge' | null {
  if (pathname === '/model' || pathname.startsWith('/model/')) return 'model';
  if (pathname === '/knowledge' || pathname.startsWith('/knowledge/')) return 'knowledge';
  return null;
}

/** Extract the current context ID from the pathname (mirrors AppHeader logic) */
function getRouteContextId(pathname: string): string | null {
  if (pathname === '/model') return '__model__';
  if (pathname.startsWith('/model/')) {
    const segments = pathname.replace('/model/', '').split('/').filter(Boolean);
    return segments.length > 0 ? segments.join('.') : '__model__';
  }
  if (pathname === '/knowledge') return '__knowledge__';
  if (pathname.startsWith('/knowledge/')) {
    const id = pathname.replace('/knowledge/', '').split('/').filter(Boolean)[0];
    return id || '__knowledge__';
  }
  return null;
}

interface SessionListItemProps {
  session: InlineChatSession;
  isActive: boolean;
  isDark: boolean;
  onSelect: () => void;
  onClose: () => void;
}

function SessionListItem({ session, isActive, isDark, onSelect, onClose }: SessionListItemProps) {
  const Icon = getSessionIcon(session);
  const [confirmOpen, setConfirmOpen] = useState(false);

  return (
    <Box
      px="xs"
      py={6}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        borderRadius: 6,
        cursor: 'pointer',
        backgroundColor: isActive
          ? isDark
            ? 'var(--mantine-color-cyan-9)'
            : 'var(--mantine-color-teal-1)'
          : 'transparent',
        transition: 'background-color 150ms ease',
      }}
      onClick={onSelect}
    >
      <Icon
        size={14}
        color={
          isActive
            ? isDark
              ? 'var(--mantine-color-cyan-4)'
              : 'var(--mantine-color-teal-6)'
            : isDark
              ? 'var(--mantine-color-gray-4)'
              : 'var(--mantine-color-gray-5)'
        }
      />
      <Box style={{ flex: 1, minWidth: 0 }}>
        <Text
          size="xs"
          fw={isActive ? 500 : 400}
          c={isActive ? (isDark ? 'cyan.4' : 'teal.7') : (isDark ? 'gray.2' : 'gray.7')}
          truncate
        >
          {session.contextLabel}
        </Text>
        <Text size="xs" c="dimmed" truncate style={{ fontSize: '10px' }}>
          {session.contextType === 'model'
            ? session.contextId === '__model__'
              ? 'Data Model'
              : session.contextId.replace(/\./g, ' > ')
            : session.contextEntityType || 'Concept'}
        </Text>
      </Box>
      <Popover
        opened={confirmOpen}
        onChange={setConfirmOpen}
        position="left"
        shadow="md"
        withArrow
        arrowSize={8}
      >
        <Popover.Target>
          <ActionIcon
            size={18}
            variant="transparent"
            color={confirmOpen ? 'red' : 'gray'}
            onClick={(e) => {
              e.stopPropagation();
              setConfirmOpen((o) => !o);
            }}
            style={{ opacity: confirmOpen ? 1 : 0.5, flexShrink: 0 }}
          >
            <HiXMark size={12} />
          </ActionIcon>
        </Popover.Target>
        <Popover.Dropdown
          p="xs"
          style={{
            backgroundColor: 'var(--mantine-color-body)',
            border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
          }}
          onClick={(e) => e.stopPropagation()}
        >
          <Text size="xs" fw={500} c={isDark ? 'gray.2' : 'gray.7'} mb={8}>
            Close this chat?
          </Text>
          <Group gap="xs" justify="flex-end">
            <Button
              size="compact-xs"
              variant="subtle"
              color="gray"
              onClick={(e) => {
                e.stopPropagation();
                setConfirmOpen(false);
              }}
            >
              Cancel
            </Button>
            <Button
              size="compact-xs"
              variant="filled"
              color="red"
              onClick={(e) => {
                e.stopPropagation();
                setConfirmOpen(false);
                onClose();
              }}
            >
              Close
            </Button>
          </Group>
        </Popover.Dropdown>
      </Popover>
    </Box>
  );
}

/** Color for each related content type */
const relContentTypeColors: Record<RelatedContentType, string> = {
  model: 'teal',
  concept: 'grape',
  analysis: 'orange',
};

const relContentTypeLabels: Record<RelatedContentType, string> = {
  model: 'Model',
  concept: 'Concept',
  analysis: 'Analysis',
};

/** Icon resolver for a related content ref */
function RelContentRefIcon({ type, entityType, size, color }: {
  type: RelatedContentType;
  entityType?: string;
  size: number;
  color: string;
}) {
  if (type === 'model') {
    if (entityType === 'ATTRIBUTE') return <HiOutlineViewColumns size={size} color={color} style={{ flexShrink: 0 }} />;
    return <HiOutlineTableCells size={size} color={color} style={{ flexShrink: 0 }} />;
  }
  if (type === 'concept') return <HiOutlineLightBulb size={size} color={color} style={{ flexShrink: 0 }} />;
  if (type === 'analysis') return <HiOutlineBeaker size={size} color={color} style={{ flexShrink: 0 }} />;
  return <HiOutlineLink size={size} color={color} style={{ flexShrink: 0 }} />;
}

/** Navigate to the right view for a related ref */
function getRelContentNavigationPath(ref: RelatedContentRef): string {
  if (ref.type === 'model') return '/model/' + ref.id.replace(/\./g, '/');
  if (ref.type === 'concept') return `/knowledge/${ref.id}`;
  if (ref.type === 'analysis') return `/analysis/${ref.id}`;
  return '/';
}

/** Popover that shows related content refs in the drawer header */
function RelatedContentDrawerPopover({
  refs,
  isOpen,
  onToggle,
  isDark,
  navigate: nav,
}: {
  refs: RelatedContentRef[];
  isOpen: boolean;
  onToggle: (open: boolean) => void;
  isDark: boolean;
  navigate: ReturnType<typeof useNavigate>;
}) {
  const modelRefs = refs.filter((r) => r.type === 'model');
  const nonModelRefs = refs.filter((r) => r.type !== 'model');

  return (
    <Popover
      opened={isOpen}
      onChange={onToggle}
      position="bottom-end"
      width={260}
      shadow="md"
      withArrow
      arrowSize={8}
    >
      <Popover.Target>
        <Tooltip label={`${refs.length} related item${refs.length > 1 ? 's' : ''}`} withArrow disabled={isOpen}>
          <Box style={{ position: 'relative', display: 'inline-flex' }}>
            <ActionIcon
              size="xs"
              variant="subtle"
              color={isOpen ? 'indigo' : isDark ? 'gray.4' : 'gray.5'}
              onClick={() => onToggle(!isOpen)}
            >
              <HiOutlineLink size={14} />
            </ActionIcon>
            <Badge
              size="xs"
              variant="filled"
              color="indigo"
              circle
              style={{
                position: 'absolute',
                top: -4,
                right: -4,
                minWidth: 14,
                height: 14,
                padding: 0,
                fontSize: '9px',
                pointerEvents: 'none',
              }}
            >
              {refs.length}
            </Badge>
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
        <Box px="xs" pt={6} pb={4}>
          <Group gap={4} mb={6}>
            <HiOutlineLink
              size={12}
              color={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
            />
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} style={{ fontSize: '10px', letterSpacing: '0.5px' }}>
              Related ({refs.length})
            </Text>
          </Group>
          <ScrollArea.Autosize mah={200} type="scroll" scrollbarSize={6}>
            {/* Model refs → compact tree */}
            {modelRefs.length > 0 && (
              <RelatedModelTree
                refs={modelRefs}
                isDark={isDark}
                iconColor={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
                onNavigate={(fullId) => {
                  onToggle(false);
                  nav('/model/' + fullId.replace(/\./g, '/'));
                }}
                compact
              />
            )}
            {/* Non-model refs → flat list */}
            {nonModelRefs.map((ref) => (
              <Box
                key={ref.id}
                px={4}
                py={4}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 6,
                  borderRadius: 4,
                  cursor: 'pointer',
                  transition: 'background-color 150ms ease',
                }}
                onClick={() => {
                  onToggle(false);
                  nav(getRelContentNavigationPath(ref));
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = isDark
                    ? 'var(--mantine-color-dark-6)'
                    : 'var(--mantine-color-gray-1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <RelContentRefIcon
                  type={ref.type}
                  entityType={ref.entityType}
                  size={12}
                  color={isDark ? 'var(--mantine-color-indigo-4)' : 'var(--mantine-color-indigo-6)'}
                />
                <Text
                  size="xs"
                  c={isDark ? 'gray.2' : 'gray.7'}
                  truncate
                  style={{ flex: 1, fontSize: '11px' }}
                >
                  {ref.title}
                </Text>
                <Badge
                  size="xs"
                  variant="light"
                  color={relContentTypeColors[ref.type] || 'gray'}
                  style={{ flexShrink: 0 }}
                >
                  {relContentTypeLabels[ref.type] || ref.type}
                </Badge>
                <HiOutlineArrowTopRightOnSquare
                  size={10}
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

const HOVER_ZONE_WIDTH = 24;

function CollapsedRightPill({
  sessions,
  isDark,
  onOpen,
}: {
  sessions: InlineChatSession[];
  isDark: boolean;
  onOpen: () => void;
}) {
  const [pillVisible, setPillVisible] = useState(false);
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleMouseEnter = useCallback(() => {
    if (hideTimer.current) {
      clearTimeout(hideTimer.current);
      hideTimer.current = null;
    }
    setPillVisible(true);
  }, []);

  const handleMouseLeave = useCallback(() => {
    hideTimer.current = setTimeout(() => {
      setPillVisible(false);
    }, 400);
  }, []);

  return (
    <Box
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      style={{
        position: 'fixed',
        right: 0,
        top: 0,
        width: HOVER_ZONE_WIDTH,
        height: '100%',
        zIndex: 100,
      }}
    >
      <Tooltip
        label={`${sessions.length} active chat${sessions.length > 1 ? 's' : ''}`}
        position="left"
        disabled={!pillVisible}
      >
        <ActionIcon
          size={40}
          variant="filled"
          color={isDark ? 'cyan' : 'teal'}
          onClick={onOpen}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          style={{
            position: 'absolute',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            borderRadius: '8px 0 0 8px',
            boxShadow: isDark
              ? '0 2px 8px rgba(0,0,0,0.4)'
              : '0 2px 8px rgba(0,0,0,0.15)',
            opacity: pillVisible ? 1 : 0,
            transition: 'opacity 200ms ease',
            pointerEvents: pillVisible ? 'auto' : 'none',
          }}
        >
          <Box style={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <HiOutlineChatBubbleLeftRight size={18} />
            {sessions.length > 1 && (
              <Badge
                size="xs"
                variant="filled"
                color="red"
                circle
                style={{
                  position: 'absolute',
                  top: -8,
                  right: -8,
                  minWidth: 16,
                  height: 16,
                  padding: 0,
                  fontSize: '10px',
                  opacity: pillVisible ? 1 : 0,
                  transition: 'opacity 200ms ease',
                }}
              >
                {sessions.length}
              </Badge>
            )}
          </Box>
        </ActionIcon>
      </Tooltip>
    </Box>
  );
}

export function InlineChatDrawer() {
  const {
    state,
    activeSession,
    closeSession,
    closeAllSessions,
    setActiveSession,
    sendMessage,
    openDrawer,
    closeDrawer,
  } = useInlineChat();
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const location = useLocation();
  const { refs: relatedChats } = useChatReferences(
    activeSession?.contextType ?? null,
    activeSession?.contextId ?? null,
  );
  const { refs: relatedContent } = useRelatedContent(
    activeSession?.contextType ?? null,
    activeSession?.contextId ?? null,
  );
  const [listOpen, setListOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [confirmCloseSessionOpen, setConfirmCloseSessionOpen] = useState(false);
  const [relatedChatsOpen, setRelatedChatsOpen] = useState(false);
  const [relatedContentOpen, setRelatedContentOpen] = useState(false);

  const handleConfirmCloseAll = useCallback(() => {
    closeAllSessions();
    setConfirmOpen(false);
    setListOpen(false);
  }, [closeAllSessions]);

  // Reset confirmation when list popover closes
  const handleListChange = useCallback((opened: boolean) => {
    setListOpen(opened);
    if (!opened) setConfirmOpen(false);
  }, []);

  const { sessions, isDrawerOpen } = state;
  const routeContextType = useRouteContextType(location.pathname);
  const routeContextId = getRouteContextId(location.pathname);

  // Inline chat is not available on the general chat route
  const isGeneralChatRoute = location.pathname === '/chat' || location.pathname.startsWith('/chat/');
  if (isGeneralChatRoute) return null;

  // Session for the currently viewed entity/concept (if any)
  const currentContextSession = routeContextId
    ? sessions.find((s) => s.contextId === routeContextId)
    : undefined;
  // Show "Open current" when there IS a session for the current context but it's NOT the active one
  const showOpenCurrent = !!currentContextSession && currentContextSession.id !== state.activeSessionId;

  // Split sessions into "most relevant" and "recent" (only when grouping is enabled)
  const relevantSessions = flags.inlineChatSessionGrouping && routeContextType
    ? sessions.filter((s) => s.contextType === routeContextType)
    : [];
  const recentSessions = (flags.inlineChatSessionGrouping && routeContextType
    ? sessions.filter((s) => s.contextType !== routeContextType)
    : [...sessions]
  ).sort((a, b) => b.createdAt - a.createdAt);

  // Nothing to render at all
  if (sessions.length === 0 && !isDrawerOpen) return null;

  // Collapsed pill: sessions exist but drawer is hidden
  // Pill is invisible until mouse approaches the right edge
  if (!isDrawerOpen && sessions.length > 0) {
    return <CollapsedRightPill sessions={sessions} isDark={isDark} onOpen={openDrawer} />;
  }

  const handleSelectSession = (sessionId: string) => {
    setActiveSession(sessionId);
    setListOpen(false);
  };

  // Open drawer
  return (
    <Box
      style={{
        width: DRAWER_WIDTH,
        height: '100%',
        borderLeft: '1px solid var(--mantine-color-default-border)',
        backgroundColor: 'var(--mantine-color-body)',
        display: 'flex',
        flexDirection: 'column',
        flexShrink: 0,
      }}
    >
      {/* Drawer header */}
      <Box
        px="xs"
        py={6}
        style={{
          borderBottom: '1px solid var(--mantine-color-default-border)',
          backgroundColor: isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)',
        }}
      >
        <Group justify="space-between" wrap="nowrap">
          <Group gap={6} wrap="nowrap" style={{ flex: 1, minWidth: 0 }}>
            <HiOutlineChatBubbleLeftRight
              size={14}
              color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
            />

            {/* Session switcher: active session name + dropdown */}
            {flags.inlineChatMultiSession && sessions.length > 1 ? (
              <Popover
                opened={listOpen}
                onChange={handleListChange}
                position="bottom-start"
                width={DRAWER_WIDTH - 16}
                shadow="md"
                withArrow
                arrowSize={8}
              >
                <Popover.Target>
                  <Group
                    gap={4}
                    wrap="nowrap"
                    style={{ cursor: 'pointer', flex: 1, minWidth: 0 }}
                    onClick={() => setListOpen((o) => !o)}
                  >
                    <Text
                      size="xs"
                      fw={600}
                      c={isDark ? 'gray.2' : 'gray.7'}
                      truncate
                    >
                      {activeSession?.contextLabel || 'Context Chats'}
                    </Text>
                    <Badge size="xs" variant="light" color="gray">
                      {sessions.length}
                    </Badge>
                    <HiChevronDown
                      size={12}
                      color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'}
                      style={{
                        transition: 'transform 150ms ease',
                        transform: listOpen ? 'rotate(180deg)' : 'none',
                        flexShrink: 0,
                      }}
                    />
                  </Group>
                </Popover.Target>

                <Popover.Dropdown
                  p={0}
                  style={{
                    backgroundColor: 'var(--mantine-color-body)',
                    border: '1px solid var(--mantine-color-default-border)',
                  }}
                >
                  <ScrollArea.Autosize mah={360} type="scroll" scrollbarSize={6}>
                    <Box p={6}>
                      {/* Most relevant group */}
                      {relevantSessions.length > 0 && (
                        <>
                          <Text
                            size="xs"
                            c="dimmed"
                            tt="uppercase"
                            fw={600}
                            px="xs"
                            py={4}
                            style={{ fontSize: '10px', letterSpacing: '0.5px' }}
                          >
                            Most relevant
                          </Text>
                          {relevantSessions.map((session) => (
                            <SessionListItem
                              key={session.id}
                              session={session}
                              isActive={session.id === state.activeSessionId}
                              isDark={isDark}
                              onSelect={() => handleSelectSession(session.id)}
                              onClose={() => closeSession(session.id)}
                            />
                          ))}
                        </>
                      )}

                      {/* Recent group */}
                      {recentSessions.length > 0 && (
                        <>
                          <Text
                            size="xs"
                            c="dimmed"
                            tt="uppercase"
                            fw={600}
                            px="xs"
                            py={4}
                            mt={relevantSessions.length > 0 ? 6 : 0}
                            style={{ fontSize: '10px', letterSpacing: '0.5px' }}
                          >
                            Recent
                          </Text>
                          {recentSessions.map((session) => (
                            <SessionListItem
                              key={session.id}
                              session={session}
                              isActive={session.id === state.activeSessionId}
                              isDark={isDark}
                              onSelect={() => handleSelectSession(session.id)}
                              onClose={() => closeSession(session.id)}
                            />
                          ))}
                        </>
                      )}
                    </Box>
                  </ScrollArea.Autosize>

                  {/* Footer: Open current (left) + Close all (right) */}
                  <Group
                    px="xs"
                    py={4}
                    justify="space-between"
                    wrap="nowrap"
                    style={{
                      borderTop: `1px solid var(--mantine-color-default-border)`,
                    }}
                  >
                    {showOpenCurrent ? (
                      <Tooltip label="Switch to current context chat" withArrow position="top">
                        <ActionIcon
                          size="sm"
                          variant="subtle"
                          color={isDark ? 'cyan' : 'teal'}
                          onClick={() => handleSelectSession(currentContextSession!.id)}
                        >
                          <HiOutlineArrowUturnLeft size={14} />
                        </ActionIcon>
                      </Tooltip>
                    ) : (
                      <span />
                    )}
                    <Popover
                      opened={confirmOpen}
                      onChange={setConfirmOpen}
                      position="top-end"
                      shadow="md"
                      withArrow
                      arrowSize={8}
                    >
                      <Popover.Target>
                        <Tooltip label="Close all chats" withArrow position="top" disabled={confirmOpen}>
                          <ActionIcon
                            size="sm"
                            variant="subtle"
                            color={confirmOpen ? 'red' : 'gray'}
                            onClick={() => setConfirmOpen((o) => !o)}
                          >
                            <HiOutlineTrash size={14} />
                          </ActionIcon>
                        </Tooltip>
                      </Popover.Target>
                      <Popover.Dropdown
                        p="xs"
                        style={{
                          backgroundColor: 'var(--mantine-color-body)',
                          border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
                        }}
                      >
                        <Text size="xs" fw={500} c={isDark ? 'gray.2' : 'gray.7'} mb={8}>
                          Close all {sessions.length} chats?
                        </Text>
                        <Group gap="xs" justify="flex-end">
                          <Button
                            size="compact-xs"
                            variant="subtle"
                            color="gray"
                            onClick={() => setConfirmOpen(false)}
                          >
                            Cancel
                          </Button>
                          <Button
                            size="compact-xs"
                            variant="filled"
                            color="red"
                            onClick={handleConfirmCloseAll}
                          >
                            Close all
                          </Button>
                        </Group>
                      </Popover.Dropdown>
                    </Popover>
                  </Group>
                </Popover.Dropdown>
              </Popover>
            ) : (
              <>
                <Text size="xs" fw={600} c={isDark ? 'gray.2' : 'gray.7'}>
                  {activeSession?.contextLabel || 'Context Chats'}
                </Text>
              </>
            )}
          </Group>

          <Group gap={4} wrap="nowrap">
            {/* Related content (model, concepts, analyses) */}
            {flags.relatedContentEnabled && flags.relatedContentInDrawer && relatedContent.length > 0 && (
              <RelatedContentDrawerPopover
                refs={relatedContent}
                isOpen={relatedContentOpen}
                onToggle={setRelatedContentOpen}
                isDark={isDark}
                navigate={navigate}
              />
            )}
            {/* Related general chats */}
            {flags.chatReferencesEnabled && relatedChats.length > 0 && (
              <Popover
                opened={relatedChatsOpen}
                onChange={setRelatedChatsOpen}
                position="bottom-end"
                width={260}
                shadow="md"
                withArrow
                arrowSize={8}
              >
                <Popover.Target>
                  <Tooltip label={`${relatedChats.length} related chat${relatedChats.length > 1 ? 's' : ''}`} withArrow disabled={relatedChatsOpen}>
                    <Box style={{ position: 'relative', display: 'inline-flex' }}>
                      <ActionIcon
                        size="xs"
                        variant="subtle"
                        color={relatedChatsOpen ? 'violet' : isDark ? 'gray.4' : 'gray.5'}
                        onClick={() => setRelatedChatsOpen((o) => !o)}
                      >
                        <HiOutlineChatBubbleOvalLeft size={14} />
                      </ActionIcon>
                      <Badge
                        size="xs"
                        variant="filled"
                        color="violet"
                        circle
                        style={{
                          position: 'absolute',
                          top: -4,
                          right: -4,
                          minWidth: 14,
                          height: 14,
                          padding: 0,
                          fontSize: '9px',
                          pointerEvents: 'none',
                        }}
                      >
                        {relatedChats.length}
                      </Badge>
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
                  <Box px="xs" pt={6} pb={4}>
                    <Group gap={4} mb={6}>
                      <HiOutlineChatBubbleOvalLeft
                        size={12}
                        color={isDark ? 'var(--mantine-color-violet-4)' : 'var(--mantine-color-violet-6)'}
                      />
                      <Text size="xs" c="dimmed" tt="uppercase" fw={600} style={{ fontSize: '10px', letterSpacing: '0.5px' }}>
                        Chats ({relatedChats.length})
                      </Text>
                    </Group>
                    <ScrollArea.Autosize mah={200} type="scroll" scrollbarSize={6}>
                      {relatedChats.map((ref) => (
                        <Box
                          key={ref.id}
                          px={4}
                          py={4}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 6,
                            borderRadius: 4,
                            cursor: 'pointer',
                            transition: 'background-color 150ms ease',
                          }}
                          onClick={() => {
                            setRelatedChatsOpen(false);
                            navigate('/chat');
                          }}
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
                            size={12}
                            color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'}
                            style={{ flexShrink: 0 }}
                          />
                          <Text
                            size="xs"
                            c={isDark ? 'gray.2' : 'gray.7'}
                            truncate
                            style={{ flex: 1, fontSize: '11px' }}
                          >
                            {ref.title}
                          </Text>
                          <HiOutlineArrowTopRightOnSquare
                            size={10}
                            color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
                            style={{ flexShrink: 0 }}
                          />
                        </Box>
                      ))}
                    </ScrollArea.Autosize>
                  </Box>
                </Popover.Dropdown>
              </Popover>
            )}
            {/* Close current session (with confirmation) */}
            {activeSession && (
              <Popover
                opened={confirmCloseSessionOpen}
                onChange={setConfirmCloseSessionOpen}
                position="bottom-end"
                shadow="md"
                withArrow
                arrowSize={8}
              >
                <Popover.Target>
                  <Tooltip label="Close this chat" withArrow disabled={confirmCloseSessionOpen}>
                    <ActionIcon
                      size="xs"
                      variant="subtle"
                      color={confirmCloseSessionOpen ? 'red' : 'gray'}
                      onClick={() => setConfirmCloseSessionOpen((o) => !o)}
                    >
                      <HiXMark size={14} />
                    </ActionIcon>
                  </Tooltip>
                </Popover.Target>
                <Popover.Dropdown
                  p="xs"
                  style={{
                    backgroundColor: 'var(--mantine-color-body)',
                    border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
                  }}
                >
                  <Text size="xs" fw={500} c={isDark ? 'gray.2' : 'gray.7'} mb={8}>
                    Close this chat?
                  </Text>
                  <Group gap="xs" justify="flex-end">
                    <Button
                      size="compact-xs"
                      variant="subtle"
                      color="gray"
                      onClick={() => setConfirmCloseSessionOpen(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                      size="compact-xs"
                      variant="filled"
                      color="red"
                      onClick={() => {
                        closeSession(activeSession.id);
                        setConfirmCloseSessionOpen(false);
                      }}
                    >
                      Close
                    </Button>
                  </Group>
                </Popover.Dropdown>
              </Popover>
            )}
            {/* Hide drawer */}
            <Tooltip label="Hide panel">
              <ActionIcon
                size="xs"
                variant="subtle"
                color="gray"
                onClick={closeDrawer}
              >
                <HiChevronRight size={14} />
              </ActionIcon>
            </Tooltip>
          </Group>
        </Group>
      </Box>

      {/* Active session panel */}
      <Box style={{ flex: 1, minHeight: 0 }}>
        {activeSession ? (
          <InlineChatPanel
            session={activeSession}
            onSend={(content) => sendMessage(activeSession.id, content)}
          />
        ) : (
          <Box
            style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Text size="sm" c="dimmed">
              No active chat session
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
