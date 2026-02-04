import { ActionIcon, Box, Textarea, useMantineColorScheme } from '@mantine/core';
import { useState, useRef, useEffect } from 'react';
import { HiPaperAirplane } from 'react-icons/hi2';

interface MessageInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

export function MessageInput({ onSend, disabled = false }: MessageInputProps) {
  const [value, setValue] = useState('');
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 200)}px`;
    }
  }, [value]);

  const handleSend = () => {
    const trimmed = value.trim();
    if (trimmed && !disabled) {
      onSend(trimmed);
      setValue('');
      if (textareaRef.current) {
        textareaRef.current.style.height = 'auto';
      }
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <Box
      p="md"
      style={{
        borderTop: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
        backgroundColor: isDark ? 'var(--mantine-color-slate-8)' : 'white',
      }}
    >
      <Box
        style={{
          display: 'flex',
          alignItems: 'flex-end',
          gap: '12px',
          maxWidth: '900px',
          margin: '0 auto',
        }}
      >
        <Textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => setValue(e.currentTarget.value)}
          onKeyDown={handleKeyDown}
          placeholder="Type your message..."
          disabled={disabled}
          autosize
          minRows={1}
          maxRows={6}
          style={{ flex: 1 }}
          styles={{
            input: {
              backgroundColor: isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-0)',
              border: `1px solid ${isDark ? 'var(--mantine-color-slate-6)' : 'var(--mantine-color-gray-3)'}`,
              '&:focus': {
                borderColor: isDark ? 'var(--mantine-color-cyan-6)' : 'var(--mantine-color-teal-6)',
              },
            },
          }}
        />
        <ActionIcon
          size="lg"
          variant="filled"
          color={isDark ? 'cyan' : 'teal'}
          onClick={handleSend}
          disabled={!value.trim() || disabled}
          style={{
            marginBottom: '2px',
          }}
        >
          <HiPaperAirplane size={18} />
        </ActionIcon>
      </Box>
      <Box ta="center" mt="xs">
        <span
          style={{
            fontSize: '11px',
            color: isDark ? 'var(--mantine-color-slate-5)' : 'var(--mantine-color-gray-5)',
          }}
        >
          Press Enter to send, Shift + Enter for new line
        </span>
      </Box>
    </Box>
  );
}
