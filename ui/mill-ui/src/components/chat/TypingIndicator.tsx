import { Box, useMantineColorScheme } from '@mantine/core';
import './TypingIndicator.css';
import { chatAccentCss } from './chatChrome';

interface TypingIndicatorProps {
  /** Tighter layout for inline use beside thinking text */
  compact?: boolean;
}

export function TypingIndicator({ compact = false }: TypingIndicatorProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: compact ? '3px' : '4px',
        padding: compact ? 0 : '8px 12px',
        flexShrink: 0,
        ['--chat-typing-dot' as string]: chatAccentCss(isDark),
      }}
      aria-hidden="true"
    >
      {[0, 1, 2].map((i) => (
        <Box
          key={i}
          className="typing-dot"
          style={{
            animationDelay: `${i * 0.16}s`,
            width: compact ? '6px' : '8px',
            height: compact ? '6px' : '8px',
          }}
        />
      ))}
    </Box>
  );
}
