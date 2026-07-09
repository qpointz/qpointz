import { useEffect } from 'react';
import type { InlineChatContextType } from '../../types/inlineChat';
import { useInlineChat } from '../../context/InlineChatContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

export interface InlineChatHostBinding {
  contextType: InlineChatContextType;
  contextId: string;
  /** When false, host binding is skipped (e.g. inline chat disabled for this view). */
  enabled?: boolean;
}

/**
 * Binds the inline drawer to the current host: activates that host's session and opens the drawer
 * when a session exists; otherwise hides the drawer without destroying sessions.
 */
export function useInlineChatHostBinding({
  contextType,
  contextId,
  enabled = true,
}: InlineChatHostBinding): void {
  const flags = useFeatureFlags();
  const {
    state,
    getSessionByContext,
    setActiveSession,
    openDrawer,
    closeDrawer,
  } = useInlineChat();

  useEffect(() => {
    if (!flags.inlineChatEnabled || !enabled) {
      return;
    }

    const session = getSessionByContext(contextType, contextId);
    if (session) {
      if (state.activeSessionId !== session.id) {
        setActiveSession(session.id);
      }
      if (!state.isDrawerOpen) {
        openDrawer();
      }
      return;
    }

    if (state.isDrawerOpen) {
      closeDrawer();
    }
  }, [
    contextId,
    contextType,
    closeDrawer,
    enabled,
    flags.inlineChatEnabled,
    getSessionByContext,
    openDrawer,
    setActiveSession,
    state.activeSessionId,
    state.isDrawerOpen,
  ]);
}
