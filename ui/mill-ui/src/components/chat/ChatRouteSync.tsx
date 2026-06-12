import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router';
import { useChat } from '../../context/ChatContext';
import { isRestChatBackendActive } from '../../services/chatService';

/**
 * Synchronizes general-chat URL segments (`/chat`, `/chat/:conversationId`) with the active
 * conversation and inserts a loading stub when the id is not yet in the sidebar (deep link).
 *
 * When `:conversationId` is present, the route param is the source of truth for selection.
 */
export function ChatRouteSync() {
  const { conversationId: paramId } = useParams();
  const navigate = useNavigate();
  const { state, initialized, ensureActiveConversation, syncChatRouteConversationParam, refreshChatList } =
    useChat();

  useEffect(() => {
    syncChatRouteConversationParam(paramId);
  }, [paramId, syncChatRouteConversationParam]);

  useEffect(() => {
    if (!initialized || !paramId) {
      return;
    }
    const exists = state.conversations.some((c) => c.id === paramId);
    if (exists) {
      ensureActiveConversation(paramId);
      return;
    }
    const targetId = state.activeConversationId ?? state.conversations[0]?.id ?? null;
    if (targetId) {
      ensureActiveConversation(targetId);
      navigate(`/chat/${targetId}`, { replace: true });
      return;
    }
    navigate('/chat', { replace: true });
  }, [
    initialized,
    paramId,
    ensureActiveConversation,
    navigate,
    state.activeConversationId,
    state.conversations,
  ]);

  useEffect(() => {
    if (!initialized || paramId) {
      return;
    }
    const active = state.activeConversationId;
    if (active?.startsWith('temp-')) {
      return;
    }
    const targetId = active ?? state.conversations[0]?.id ?? null;
    if (targetId) {
      ensureActiveConversation(targetId);
      navigate(`/chat/${targetId}`, { replace: true });
    }
  }, [initialized, navigate, paramId, state.activeConversationId, state.conversations, ensureActiveConversation]);

  useEffect(() => {
    if (!initialized || !isRestChatBackendActive() || state.conversations.length > 0) {
      return;
    }
    void refreshChatList();
  }, [initialized, refreshChatList, state.conversations.length]);

  return null;
}
