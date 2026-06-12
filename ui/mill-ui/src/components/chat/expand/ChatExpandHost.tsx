import { useEffect } from 'react';
import { Box } from '@mantine/core';
import { resolveExpandComponent } from './expandRegistry';
import { useChatExpand } from './useChatExpand';

interface ChatExpandHostProps {
  /** Scroll target element id (message id) after close. */
  onScrollToMessage?: (messageId: string) => void;
}

export function ChatExpandHost({ onScrollToMessage }: ChatExpandHostProps) {
  const { expand, closeExpand } = useChatExpand();

  useEffect(() => {
    if (!expand) return;
    return () => {
      if (expand.messageId) {
        onScrollToMessage?.(expand.messageId);
      }
    };
  }, [expand, onScrollToMessage]);

  if (!expand) return null;

  const Component = resolveExpandComponent(expand.kind, expand.chatType);
  if (!Component) return null;

  const handleClose = () => {
    const messageId = expand.messageId;
    closeExpand();
    window.requestAnimationFrame(() => onScrollToMessage?.(messageId));
  };

  return (
    <Box style={{ position: 'absolute', inset: 0, zIndex: 15, background: 'var(--mantine-color-body)' }}>
      <Component payload={expand} onClose={handleClose} />
    </Box>
  );
}
