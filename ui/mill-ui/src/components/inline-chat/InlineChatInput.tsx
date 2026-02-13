import { Box } from '@mantine/core';
import { ChatInputBox } from '../common/ChatInputBox';

interface InlineChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

export function InlineChatInput({ onSend, disabled = false, placeholder }: InlineChatInputProps) {
  return (
    <Box
      px="xs"
      py={8}
      style={{
        borderTop: `1px solid var(--mantine-color-default-border)`,
      }}
    >
      <ChatInputBox
        onSend={onSend}
        disabled={disabled}
        placeholder={placeholder || 'Ask a question...'}
        compact
      />
    </Box>
  );
}
