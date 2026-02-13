import { Box, Text, useMantineColorScheme } from '@mantine/core';
import ReactMarkdown from 'react-markdown';
import { CodeBlock } from './CodeBlock';

interface MessageContentProps {
  content: string;
  /** Compact mode uses smaller text and spacing (inline chat) */
  compact?: boolean;
}

/**
 * Shared markdown message renderer used by both general chat and inline chat.
 * Renders content through ReactMarkdown with theme-aware styling.
 */
export function MessageContent({ content, compact = false }: MessageContentProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const fontSize = compact ? '12px' : '14px';
  const lineHeight = compact ? 1.5 : 1.6;
  const textSize = compact ? 'xs' : 'sm';
  const listMargin = compact ? 16 : 20;
  const listBottom = compact ? 4 : 8;
  const cellPadding = compact ? '4px 6px' : '8px';
  const tableFontSize = compact ? '11px' : '13px';
  const headingMb = compact ? 2 : 'xs';

  return (
    <Box
      className="markdown-content"
      style={{ fontSize, lineHeight }}
    >
      <ReactMarkdown
        components={{
          code({ className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || '');
            const codeString = String(children).replace(/\n$/, '');

            // Fenced code block
            if (match) {
              if (compact) {
                // Compact: lightweight inline block (no syntax highlighting)
                return (
                  <Box
                    my={4}
                    p="xs"
                    style={{
                      backgroundColor: isDark ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.05)',
                      borderRadius: 6,
                      overflowX: 'auto',
                      fontSize: '11px',
                      fontFamily: 'ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace',
                      lineHeight: 1.4,
                      whiteSpace: 'pre',
                    }}
                  >
                    {codeString}
                  </Box>
                );
              }
              return <CodeBlock code={codeString} language={match[1]} />;
            }

            // Inline code
            return (
              <code
                {...props}
                style={{
                  backgroundColor: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)',
                  padding: compact ? '1px 4px' : '2px 6px',
                  borderRadius: compact ? 3 : 4,
                  fontSize: '0.9em',
                }}
              >
                {children}
              </code>
            );
          },
          p({ children }) {
            return (
              <Text component="p" mb={compact ? 4 : 'xs'} size={textSize} style={compact ? { lineHeight: 1.5 } : undefined}>
                {children}
              </Text>
            );
          },
          h1({ children }) {
            return <Text component={compact ? 'h4' : 'h1'} size={compact ? 'xs' : 'lg'} fw={700} mb={headingMb}>{children}</Text>;
          },
          h2({ children }) {
            return <Text component={compact ? 'h4' : 'h2'} size={compact ? 'xs' : 'md'} fw={600} mb={headingMb}>{children}</Text>;
          },
          h3({ children }) {
            return <Text component={compact ? 'h4' : 'h3'} size={compact ? 'xs' : 'sm'} fw={600} mb={headingMb}>{children}</Text>;
          },
          ul({ children }) {
            return <ul style={{ marginLeft: listMargin, marginBottom: listBottom, ...(compact ? { paddingLeft: 0 } : {}) }}>{children}</ul>;
          },
          ol({ children }) {
            return <ol style={{ marginLeft: listMargin, marginBottom: listBottom, ...(compact ? { paddingLeft: 0 } : {}) }}>{children}</ol>;
          },
          li({ children }) {
            return <li style={{ marginBottom: compact ? 2 : 4, ...(compact ? { fontSize: '12px' } : {}) }}>{children}</li>;
          },
          strong({ children }) {
            return <strong style={{ fontWeight: 600 }}>{children}</strong>;
          },
          table({ children }) {
            return (
              <Box style={{ overflowX: 'auto', marginBottom: compact ? 4 : 12 }}>
                <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: tableFontSize }}>
                  {children}
                </table>
              </Box>
            );
          },
          th({ children }) {
            return (
              <th
                style={{
                  border: `1px solid ${isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}`,
                  padding: cellPadding,
                  textAlign: 'left',
                  backgroundColor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
                  ...(compact ? { fontSize: '11px' } : {}),
                }}
              >
                {children}
              </th>
            );
          },
          td({ children }) {
            return (
              <td
                style={{
                  border: `1px solid ${isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}`,
                  padding: cellPadding,
                  ...(compact ? { fontSize: '11px' } : {}),
                }}
              >
                {children}
              </td>
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </Box>
  );
}
