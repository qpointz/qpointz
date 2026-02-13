import { useEffect } from 'react';
import { Box, Group, Text, useMantineColorScheme } from '@mantine/core';
import { useLocation, useNavigate } from 'react-router';
import { useChat } from '../../context/ChatContext';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { ThinkingIndicator } from './ThinkingIndicator';

export function ChatArea() {
  const { activeConversation, sendMessage, state, initialized } = useChat();
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const location = useLocation();
  const navigate = useNavigate();

  const bgColor = 'var(--mantine-color-body)';

  // Auto-send a message when navigated here with a searchQuery in router state
  // (e.g. from the "Ask in Chat" button in global search).
  // Wait for `initialized` so the localStorage hydration completes first —
  // otherwise LOAD_CONVERSATIONS can overwrite the freshly-created conversation.
  // We clear the router state immediately so this only fires once per navigation.
  useEffect(() => {
    if (!initialized || state.isLoading) return;
    const searchQuery = (location.state as { searchQuery?: string } | null)?.searchQuery;
    if (!searchQuery) return;

    // Clear router state first to prevent re-firing on re-renders
    navigate(location.pathname, { replace: true, state: {} });

    // Always create a fresh conversation for search-originated chats
    sendMessage(searchQuery, { newConversation: true });
  }, [initialized, location.state, sendMessage, state.isLoading, navigate, location.pathname]);

  return (
    <Box
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        backgroundColor: bgColor,
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Top toolbar — pinned, gradient fade into background */}
      <Box
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          zIndex: 10,
          pointerEvents: 'none',
          background: `linear-gradient(to bottom, ${bgColor} 40%, transparent 100%)`,
        }}
      >
        <Group
          justify="space-between"
          align="center"
          px="md"
          py="xs"
          style={{ pointerEvents: 'auto' }}
        >
          <Text
            size="sm"
            fw={600}
            c={isDark ? 'gray.3' : 'gray.6'}
          >
            {activeConversation?.title || 'New Chat'}
          </Text>
          {/* Slot for future controls (model switcher, related objects, etc.) */}
          <Group gap="xs">
          </Group>
        </Group>
      </Box>

      {/* Message area — full height, full width, scrollable */}
      <Box
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          width: '100%',
        }}
      >
        <MessageList
          messages={activeConversation?.messages || []}
          isLoading={state.isLoading}
        />
      </Box>

      {/* Bottom overlay — thinking indicator + floating input */}
      <Box
        style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          zIndex: 10,
          pointerEvents: 'none',
          background: `linear-gradient(to top, ${bgColor} 50%, transparent 100%)`,
          paddingTop: '24px',
        }}
      >
        <Box style={{ pointerEvents: 'auto' }}>
          {/* Thinking status — sits above the input box */}
          <ThinkingIndicator message={state.thinkingMessage} />
          <MessageInput onSend={sendMessage} disabled={state.isLoading} />
        </Box>
      </Box>
    </Box>
  );
}
