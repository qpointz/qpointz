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

  const conversationIds = state.conversations.map((c) => c.id).join('\0');
  const firstConversationId = state.conversations[0]?.id ?? null;

  useEffect(() => {
    if (!initialized || !paramId) {
      return;
    }
    const exists = state.conversations.some((c) => c.id === paramId);
    if (exists) {
      ensureActiveConversation(paramId);
      return;
    }
    const targetId = state.activeConversationId ?? firstConversationId;
    if (targetId) {
      ensureActiveConversation(targetId);
      navigate(`/chat/${targetId}`, { replace: true });
      return;
    }
    navigate('/chat', { replace: true });
    // conversationIds: re-run when membership changes, not on every transcript MERGE.
    // eslint-disable-next-line react-hooks/exhaustive-deps -- conversations read for membership only
  }, [
    initialized,
    paramId,
    ensureActiveConversation,
    navigate,
    state.activeConversationId,
    conversationIds,
    firstConversationId,
  ]);

  useEffect(() => {
    if (!initialized || paramId) {
      return;
    }
    const active = state.activeConversationId;
    if (active?.startsWith('temp-')) {
      return;
    }
    const targetId = active ?? firstConversationId;
    if (targetId) {
      ensureActiveConversation(targetId);
      navigate(`/chat/${targetId}`, { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- firstConversationId tracks list head
  }, [initialized, navigate, paramId, state.activeConversationId, firstConversationId, ensureActiveConversation]);

  useEffect(() => {
    if (!initialized || !isRestChatBackendActive() || state.conversations.length > 0) {
      return;
    }
    void refreshChatList();
  }, [initialized, refreshChatList, state.conversations.length]);

  return null;
}
