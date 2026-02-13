import { Box } from '@mantine/core';
import { ChatInputBox } from '../common/ChatInputBox';

interface MessageInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

export function MessageInput({ onSend, disabled = false }: MessageInputProps) {
  return (
    <Box
      px="md"
      pb="md"
      pt="xs"
    >
      <Box
        style={{
          maxWidth: '900px',
          margin: '0 auto',
        }}
      >
        <ChatInputBox
          onSend={onSend}
          disabled={disabled}
          placeholder="Type your message..."
        />
      </Box>
    </Box>
  );
}
