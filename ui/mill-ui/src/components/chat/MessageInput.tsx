import { Box } from '@mantine/core';
import { ChatInputBox } from '../common/ChatInputBox';
import { CHAT_CONTENT_MAX_WIDTH } from './chatChrome';

interface MessageInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

export function MessageInput({ onSend, disabled = false }: MessageInputProps) {
  return (
    <Box px="md" pb="md" pt="xs">
      <Box style={{ maxWidth: CHAT_CONTENT_MAX_WIDTH, margin: '0 auto' }}>
        <ChatInputBox
          onSend={onSend}
          disabled={disabled}
          placeholder="Ask about your data…"
        />
        <Box
          component="p"
          style={{
            margin: '8px 4px 0',
            fontSize: 11,
            lineHeight: 1.4,
            color: 'var(--mantine-color-dimmed)',
            textAlign: 'center',
          }}
        >
          Enter to send · Shift+Enter for a new line
        </Box>
      </Box>
    </Box>
  );
}
