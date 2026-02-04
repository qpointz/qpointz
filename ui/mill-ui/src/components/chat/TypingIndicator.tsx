import { Box } from '@mantine/core';
import './TypingIndicator.css';

export function TypingIndicator() {
  return (
    <Box
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '4px',
        padding: '8px 12px',
      }}
    >
      {[0, 1, 2].map((i) => (
        <Box
          key={i}
          className="typing-dot"
          style={{
            animationDelay: `${i * 0.16}s`,
          }}
        />
      ))}
    </Box>
  );
}
