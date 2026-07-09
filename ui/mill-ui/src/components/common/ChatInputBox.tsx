import { ActionIcon, Box, Textarea, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useEffect, useRef, useState } from 'react';
import { HiArrowUp, HiMicrophone, HiPlus } from 'react-icons/hi2';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { chatAccentColor, composerSurfaceStyle } from '../chat/chatChrome';

interface ChatInputBoxProps {
  onSend: (message: string) => void;
  disabled?: boolean;
  placeholder?: string;
  /** `inline` — narrow drawer composer with full General Chat chrome at smaller padding. */
  variant?: 'default' | 'compact' | 'inline';
}

export function ChatInputBox({
  onSend,
  disabled = false,
  placeholder = 'Type your message...',
  variant = 'default',
}: ChatInputBoxProps) {
  const [value, setValue] = useState('');
  const [focused, setFocused] = useState(false);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const wasDisabledRef = useRef(disabled);
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();

  const canSend = value.trim().length > 0 && !disabled;

  /** After a reply finishes, Mantine does not restore focus — put the caret back in the composer. */
  useEffect(() => {
    const prev = wasDisabledRef.current;
    wasDisabledRef.current = disabled;
    if (prev && !disabled) {
      const id = window.requestAnimationFrame(() => {
        inputRef.current?.focus({ preventScroll: true });
      });
      return () => window.cancelAnimationFrame(id);
    }
    return undefined;
  }, [disabled]);

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

  const compact = variant === 'compact';
  const inline = variant === 'inline';
  const iconColor = isDark ? 'gray.4' : 'gray.5';
  const iconSize = compact ? 'sm' : 'md';
  const iconPx = compact ? 14 : inline ? 15 : 18;
  const sendSize = compact ? 24 : inline ? 28 : 32;
  const accent = chatAccentColor(isDark);

  return (
    <Box
      style={{
        ...composerSurfaceStyle(isDark, (focused && !compact) || (focused && inline)),
        ...(compact
          ? {
              backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-1)',
              border: 'none',
              boxShadow: 'none',
            }
          : {}),
        borderRadius: inline ? 18 : compact ? 16 : 20,
        padding: inline ? '10px 12px' : compact ? '8px 10px' : '12px 14px',
        display: 'flex',
        flexDirection: 'column',
        gap: inline ? '6px' : compact ? '4px' : '8px',
      }}
    >
      {/* Textarea — borderless, transparent background */}
      <Textarea
        ref={inputRef}
        value={value}
        onChange={(e) => setValue(e.currentTarget.value)}
        onKeyDown={handleKeyDown}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        placeholder={placeholder}
        disabled={disabled}
        autosize
        minRows={1}
        maxRows={inline ? 5 : compact ? 3 : 6}
        styles={{
          root: { flex: 1 },
          wrapper: { border: 'none' },
          input: {
            backgroundColor: 'transparent',
            border: 'none',
            padding: inline ? '2px 4px' : compact ? '2px 4px' : '4px 6px',
            fontSize: inline ? '13px' : compact ? '13px' : '15px',
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

          {/* Send — accent when ready, muted when empty */}
          <ActionIcon
            variant="filled"
            color={canSend ? accent : isDark ? 'dark.4' : 'gray.4'}
            size={sendSize}
            radius="xl"
            onClick={handleSend}
            disabled={!canSend}
            aria-label="Send message"
            style={{
              transition: 'transform 150ms ease, opacity 150ms ease',
              opacity: canSend ? 1 : 0.5,
              transform: canSend ? 'scale(1)' : 'scale(0.96)',
            }}
          >
            <HiArrowUp size={iconPx} color="white" />
          </ActionIcon>
        </Box>
      </Box>
    </Box>
  );
}
