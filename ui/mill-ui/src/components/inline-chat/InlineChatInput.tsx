import { Box } from '@mantine/core';
import { ChatInputBox } from '../common/ChatInputBox';

interface InlineChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

/** Inline drawer composer — General Chat styling without a separate footer strip. */
export function InlineChatInput({ onSend, disabled = false, placeholder }: InlineChatInputProps) {
  return (
    <Box px="xs" pb="sm" pt={4}>
      <ChatInputBox
        onSend={onSend}
        disabled={disabled}
        placeholder={placeholder || 'Ask a question...'}
        variant="inline"
      />
    </Box>
  );
}
