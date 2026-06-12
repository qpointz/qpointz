import { useEffect, useCallback, useRef } from 'react';
import { Box } from '@mantine/core';
import { useLocation, useNavigate } from 'react-router';
import { useChat } from '../../context/ChatContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { ChatExpandHost } from './expand/ChatExpandHost';
import { ChatExpandProvider, useChatExpand } from './expand/useChatExpand';
import { ChatToolbar } from './ChatToolbar';
import { useRunAllChatQueries } from './useRunAllChatQueries';

function ChatAreaBody() {
  const {
    activeConversation,
    sendMessage,
    state,
    initialized,
    updateMessageArtifacts,
    agentProfiles,
    updateConversationProfile,
  } = useChat();
  const flags = useFeatureFlags();
  const { notifyRunAllComplete } = useChatExpand();
  const messageListRef = useRef<HTMLDivElement>(null);

  const scrollToMessage = useCallback((messageId: string) => {
    const el = document.getElementById(`message-${messageId}`);
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, []);

  const { sqlQueryCount, runAllDisabled, runAllLoading, runAllQueries } = useRunAllChatQueries({
    conversation: activeConversation,
    chatSqlExecuteEnabled: flags.chatSqlExecute,
    isLoading: state.isLoading,
    updateMessageArtifacts,
    onRunAllComplete: notifyRunAllComplete,
  });

  const bgColor = 'var(--mantine-color-body)';

  return (
    <Box
      ref={messageListRef}
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
      <ChatToolbar
        title={activeConversation?.title || 'New Chat'}
        profileId={activeConversation?.profileId}
        agentProfiles={agentProfiles}
        profileChangeDisabled={
          state.isLoading ||
          runAllLoading ||
          !activeConversation?.id ||
          activeConversation.id.startsWith('temp-')
        }
        onProfileChange={(nextProfileId) => {
          if (!activeConversation?.id) return;
          void updateConversationProfile(activeConversation.id, nextProfileId);
        }}
        sqlQueryCount={sqlQueryCount}
        runAllDisabled={runAllDisabled}
        runAllLoading={runAllLoading}
        onRunAllQueries={() => {
          void runAllQueries();
        }}
      />

      <Box
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          width: '100%',
          minHeight: 0,
        }}
      >
        <MessageList
          messages={activeConversation?.messages || []}
          isLoading={state.isLoading}
          thinkingMessage={state.thinkingMessage}
          conversationId={activeConversation?.id}
          chatTitle={activeConversation?.title}
          onArtifactsChange={(messageId, artifacts) => {
            if (!activeConversation) return;
            updateMessageArtifacts(activeConversation.id, messageId, artifacts);
          }}
        />
        <ChatExpandHost onScrollToMessage={scrollToMessage} />
      </Box>

      <Box
        style={{
          flexShrink: 0,
          zIndex: 10,
          pointerEvents: 'none',
          background: `linear-gradient(to top, ${bgColor} 50%, transparent 100%)`,
          paddingTop: '24px',
        }}
      >
        <Box style={{ pointerEvents: 'auto' }}>
          <MessageInput onSend={sendMessage} disabled={state.isLoading || !initialized} />
        </Box>
      </Box>
    </Box>
  );
}

export function ChatArea() {
  const { sendMessage, state, initialized } = useChat();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (!initialized || state.isLoading) return;
    const searchQuery = (location.state as { searchQuery?: string } | null)?.searchQuery;
    if (!searchQuery) return;

    navigate(location.pathname, { replace: true, state: {} });
    sendMessage(searchQuery, { newConversation: true });
  }, [initialized, location.state, sendMessage, state.isLoading, navigate, location.pathname]);

  return (
    <ChatExpandProvider>
      <ChatAreaBody />
    </ChatExpandProvider>
  );
}
