import type { ReactNode } from 'react';
import { useLocation } from 'react-router';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { useInlineChat } from '../../context/InlineChatContext';
import { InlineChatDrawer } from '../inline-chat/InlineChatDrawer';
import { InlineChatRouteHostSync } from '../inline-chat/InlineChatRouteHostSync';
import { HorizontalSplitPane } from './HorizontalSplitPane';

interface AppBodyWithInlineChatProps {
  children: ReactNode;
}

/**
 * Main app body with optional resizable inline chat column when the drawer is open.
 */
export function AppBodyWithInlineChat({ children }: AppBodyWithInlineChatProps) {
  const flags = useFeatureFlags();
  const { state } = useInlineChat();
  const location = useLocation();

  const isGeneralChatRoute =
    location.pathname === '/chat' || location.pathname.startsWith('/chat/');
  const useSplitLayout =
    flags.inlineChatEnabled
    && !isGeneralChatRoute
    && state.isDrawerOpen
    && state.sessions.length > 0;

  if (!flags.inlineChatEnabled) {
    return <>{children}</>;
  }

  const body = (
    <>
      <InlineChatRouteHostSync />
      {children}
      <InlineChatDrawer />
    </>
  );

  if (!useSplitLayout) {
    return body;
  }

  return (
    <>
      <InlineChatRouteHostSync />
      <HorizontalSplitPane
        left={children}
        right={<InlineChatDrawer embedded />}
        initialRightPx={380}
        minLeftPx={280}
        minRightPx={260}
        maxRightFraction={0.5}
        storageKey="mill.inline-chat.drawer-width"
      />
    </>
  );
}
