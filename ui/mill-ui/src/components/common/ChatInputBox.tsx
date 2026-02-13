import { ActionIcon, Box, Textarea, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useState } from 'react';
import { HiArrowUp, HiMicrophone, HiPlus } from 'react-icons/hi2';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface ChatInputBoxProps {
  onSend: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
  /** Compact mode for inline chat (single-line, smaller sizing) */
  compact?: boolean;
}

export function ChatInputBox({
  onSend,
  disabled = false,
  placeholder = 'Type your message...',
  compact = false,
}: ChatInputBoxProps) {
  const [value, setValue] = useState('');
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();

  const canSend = value.trim().length > 0 && !disabled;

  const handleSend = () => {
    const trimmed = value.trim();
    if (trimmed && !disabled) {
      onSend(trimmed);
      setValue('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const containerBg = isDark
    ? 'var(--mantine-color-dark-6)'
    : 'var(--mantine-color-gray-1)';

  const iconColor = isDark ? 'gray.4' : 'gray.5';
  const iconSize = compact ? 'sm' : 'md';
  const iconPx = compact ? 14 : 18;
  const sendSize = compact ? 24 : 30;

  return (
    <Box
      style={{
        backgroundColor: containerBg,
        borderRadius: compact ? '16px' : '20px',
        padding: compact ? '8px 10px' : '10px 14px',
        display: 'flex',
        flexDirection: 'column',
        gap: compact ? '4px' : '6px',
      }}
    >
      {/* Textarea — borderless, transparent background */}
      <Textarea
        value={value}
        onChange={(e) => setValue(e.currentTarget.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
        autosize
        minRows={1}
        maxRows={compact ? 3 : 6}
        styles={{
          root: { flex: 1 },
          wrapper: { border: 'none' },
          input: {
            backgroundColor: 'transparent',
            border: 'none',
            padding: compact ? '2px 4px' : '4px 6px',
            fontSize: compact ? '13px' : '14px',
            lineHeight: 1.5,
            color: 'var(--mantine-color-text)',
            '&::placeholder': {
              color: isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)',
            },
            '&:focus': {
              outline: 'none',
              boxShadow: 'none',
              border: 'none',
            },
          },
        }}
      />

      {/* Bottom row: [+] ......... [mic] [send] */}
      <Box
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        {/* Left side */}
        <Box style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          {flags.chatAttachButton && (
            <Tooltip label="Attach" withArrow position="top">
              <ActionIcon
                variant="subtle"
                color={iconColor}
                size={iconSize}
                radius="xl"
                disabled={disabled}
                aria-label="Attach file"
              >
                <HiPlus size={iconPx} />
              </ActionIcon>
            </Tooltip>
          )}
        </Box>

        {/* Right side */}
        <Box style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          {flags.chatDictateButton && (
            <Tooltip label="Dictate" withArrow position="top">
              <ActionIcon
                variant="subtle"
                color={iconColor}
                size={iconSize}
                radius="xl"
                disabled={disabled}
                aria-label="Dictate"
              >
                <HiMicrophone size={iconPx} />
              </ActionIcon>
            </Tooltip>
          )}

          {/* Send — filled circle, inverts dark/light */}
          <ActionIcon
            variant="filled"
            color={isDark ? 'gray.0' : 'dark.9'}
            size={sendSize}
            radius="xl"
            onClick={handleSend}
            disabled={!canSend}
            aria-label="Send message"
            style={{
              transition: 'opacity 150ms ease',
              opacity: canSend ? 1 : 0.35,
            }}
          >
            <HiArrowUp size={iconPx} color={isDark ? 'var(--mantine-color-dark-9)' : 'white'} />
          </ActionIcon>
        </Box>
      </Box>
    </Box>
  );
}
