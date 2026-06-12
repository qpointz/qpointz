import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import type { ChatMessageArtifact, Message } from '../../../types/chat';
import type { ArtefactKind, ChatType } from '../artifactPreview/types';

export interface ChatExpandPayload {
  messageId: string;
  turnId: string;
  chatType: ChatType;
  kind: ArtefactKind;
  sql: string;
  executionId?: string;
  message: Message;
  precedingUserQuestion?: string;
  chatTitle?: string;
  artifacts?: readonly ChatMessageArtifact[];
  onArtifactsChange?: (artifacts: ChatMessageArtifact[]) => void;
}

interface ChatExpandContextValue {
  expand: ChatExpandPayload | null;
  openExpand: (payload: ChatExpandPayload) => void;
  closeExpand: () => void;
  /** Bumped after toolbar Run all completes — condensed previews switch to Data tab. */
  runAllTick: number;
  notifyRunAllComplete: () => void;
}

const ChatExpandContext = createContext<ChatExpandContextValue | null>(null);

export function ChatExpandProvider({ children }: { children: ReactNode }) {
  const [expand, setExpand] = useState<ChatExpandPayload | null>(null);
  const [runAllTick, setRunAllTick] = useState(0);

  const openExpand = useCallback((payload: ChatExpandPayload) => {
    setExpand(payload);
  }, []);

  const closeExpand = useCallback(() => {
    setExpand(null);
  }, []);

  const notifyRunAllComplete = useCallback(() => {
    setRunAllTick((tick) => tick + 1);
  }, []);

  const value = useMemo(
    () => ({ expand, openExpand, closeExpand, runAllTick, notifyRunAllComplete }),
    [expand, openExpand, closeExpand, runAllTick, notifyRunAllComplete],
  );

  return <ChatExpandContext.Provider value={value}>{children}</ChatExpandContext.Provider>;
}

export function useChatExpand(): ChatExpandContextValue {
  const ctx = useContext(ChatExpandContext);
  if (!ctx) {
    return {
      expand: null,
      openExpand: () => undefined,
      closeExpand: () => undefined,
      runAllTick: 0,
      notifyRunAllComplete: () => undefined,
    };
  }
  return ctx;
}
