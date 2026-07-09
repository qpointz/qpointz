import { useEffect } from 'react';
import { useLocation } from 'react-router';
import { useInlineChat } from '../../context/InlineChatContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import {
  resolveInlineChatRouteContextId,
  resolveInlineChatRouteContextType,
} from './inlineChatRouteContext';

/**
 * Binds the inline drawer to the current route for Model and Knowledge hosts.
 * Analysis binding is owned by {@link QueryPlayground} (`activeQueryId`).
 * Closes the drawer when navigating away from inline-capable routes.
 */
export function InlineChatRouteHostSync(): null {
  const flags = useFeatureFlags();
  const location = useLocation();
  const {
    state,
    getSessionByContext,
    setActiveSession,
    openDrawer,
    closeDrawer,
  } = useInlineChat();

  useEffect(() => {
    if (!flags.inlineChatEnabled) {
      return;
    }

    const contextType = resolveInlineChatRouteContextType(location.pathname);
    if (!contextType) {
      if (state.isDrawerOpen) {
        closeDrawer();
      }
      return;
    }

    if (contextType === 'analysis') {
      return;
    }

    const contextId = resolveInlineChatRouteContextId(location.pathname);
    if (!contextId) {
      if (state.isDrawerOpen) {
        closeDrawer();
      }
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
    closeDrawer,
    flags.inlineChatEnabled,
    getSessionByContext,
    location.pathname,
    openDrawer,
    setActiveSession,
    state.activeSessionId,
    state.isDrawerOpen,
  ]);

  return null;
}
