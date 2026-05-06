import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router';
import { useChat } from '../../context/ChatContext';

/**
 * Synchronizes general-chat URL segments (`/chat`, `/chat/:conversationId`) with the active
 * conversation and inserts a loading stub when the id is not yet in the sidebar (deep link).
 */
export function ChatRouteSync() {
  const { conversationId: paramId } = useParams();
  const navigate = useNavigate();
  const { state, initialized, ensureActiveConversation, syncChatRouteConversationParam } = useChat();

  useEffect(() => {
    syncChatRouteConversationParam(paramId);
  }, [paramId, syncChatRouteConversationParam]);

  useEffect(() => {
    if (!initialized || !paramId) {
      return;
    }
    ensureActiveConversation(paramId);
  }, [initialized, paramId, ensureActiveConversation]);

  useEffect(() => {
    if (!initialized) {
      return;
    }
    const active = state.activeConversationId;
    if (active?.startsWith('temp-')) {
      return;
    }
    if (active && paramId !== active) {
      navigate(`/chat/${active}`, { replace: true });
      return;
    }
    if (!active && paramId) {
      navigate('/chat', { replace: true });
    }
  }, [initialized, navigate, paramId, state.activeConversationId]);

  return null;
}
