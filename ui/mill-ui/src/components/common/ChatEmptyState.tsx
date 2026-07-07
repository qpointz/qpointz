import { Box, Button, Group, Text, useMantineColorScheme } from '@mantine/core';
import { HiOutlineSparkles } from 'react-icons/hi2';
import { chatAccentColor, chatAccentSoftBg, CHAT_CONTENT_MAX_WIDTH } from '../chat/chatChrome';

interface ChatEmptyStateProps {
  title: string;
  description: string;
  /** Compact mode for inline chat (smaller sizing) */
  compact?: boolean;
  /** Optional starter prompts (general chat). */
  suggestions?: string[];
  onSuggestionClick?: (text: string) => void;
}

export function ChatEmptyState({
  title,
  description,
  compact = false,
  suggestions = [],
  onSuggestionClick,
}: ChatEmptyStateProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const iconSize = compact ? 48 : 72;
  const sparkleSize = compact ? 22 : 32;
  const accent = chatAccentColor(isDark);
  const showSuggestions = !compact && suggestions.length > 0 && onSuggestionClick != null;

  return (
    <Box
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: compact ? '24px' : '40px 24px',
        maxWidth: CHAT_CONTENT_MAX_WIDTH,
        margin: '0 auto',
        width: '100%',
      }}
    >
      <Box
        style={{
          width: `${iconSize}px`,
          height: `${iconSize}px`,
          borderRadius: '50%',
          backgroundColor: chatAccentSoftBg(isDark),
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: compact ? '12px' : '24px',
        }}
      >
        <HiOutlineSparkles
          size={sparkleSize}
          color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
        />
      </Box>
      <Text
        size={compact ? 'sm' : 'xl'}
        fw={compact ? 500 : 600}
        mb={compact ? 4 : 'xs'}
        c={isDark ? (compact ? 'gray.2' : 'gray.1') : (compact ? 'gray.7' : 'gray.8')}
        ta="center"
      >
        {title}
      </Text>
      <Text
        size={compact ? 'xs' : 'sm'}
        c={compact ? 'dimmed' : (isDark ? 'gray.4' : 'gray.5')}
        ta="center"
        maw={compact ? 260 : 480}
        lh={1.55}
      >
        {description}
      </Text>
      {showSuggestions ? (
        <Group gap="xs" justify="center" mt="lg" maw={520}>
          {suggestions.map((suggestion) => (
            <Button
              key={suggestion}
              size="compact-sm"
              variant="light"
              color={accent}
              radius="xl"
              onClick={() => onSuggestionClick(suggestion)}
              styles={{
                root: {
                  fontWeight: 450,
                  height: 'auto',
                  padding: '8px 14px',
                  whiteSpace: 'normal',
                  textAlign: 'left',
                  lineHeight: 1.35,
                },
              }}
            >
              {suggestion}
            </Button>
          ))}
        </Group>
      ) : null}
    </Box>
  );
}
