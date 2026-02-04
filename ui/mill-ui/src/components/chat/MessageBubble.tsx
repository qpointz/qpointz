import { Box, Paper, Text, useMantineColorScheme } from '@mantine/core';
import ReactMarkdown from 'react-markdown';
import type { Message } from '../../types/chat';
import { CodeBlock } from '../common/CodeBlock';

interface MessageBubbleProps {
  message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const { colorScheme } = useMantineColorScheme();
  const isUser = message.role === 'user';
  const isDark = colorScheme === 'dark';

  const userBgColor = isDark ? 'var(--mantine-color-cyan-7)' : 'var(--mantine-color-teal-6)';
  const assistantBgColor = isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-1)';
  const userTextColor = 'white';
  const assistantTextColor = isDark ? 'var(--mantine-color-slate-0)' : 'var(--mantine-color-slate-8)';

  return (
    <Box
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: '12px',
        paddingLeft: isUser ? '48px' : '0',
        paddingRight: isUser ? '0' : '48px',
      }}
    >
      <Paper
        shadow="sm"
        p="sm"
        style={{
          backgroundColor: isUser ? userBgColor : assistantBgColor,
          color: isUser ? userTextColor : assistantTextColor,
          maxWidth: '100%',
          borderRadius: isUser ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
        }}
      >
        {isUser ? (
          <Text size="sm" style={{ whiteSpace: 'pre-wrap' }}>
            {message.content}
          </Text>
        ) : (
          <Box
            className="markdown-content"
            style={{
              fontSize: '14px',
              lineHeight: 1.6,
            }}
          >
            <ReactMarkdown
              components={{
                code({ className, children, ...props }) {
                  const match = /language-(\w+)/.exec(className || '');
                  const codeString = String(children).replace(/\n$/, '');
                  
                  // Check if it's a code block (has language) or inline code
                  if (match) {
                    return <CodeBlock code={codeString} language={match[1]} />;
                  }
                  
                  // Inline code
                  return (
                    <code
                      {...props}
                      style={{
                        backgroundColor: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        fontSize: '0.9em',
                      }}
                    >
                      {children}
                    </code>
                  );
                },
                p({ children }) {
                  return <Text component="p" mb="xs" size="sm">{children}</Text>;
                },
                h1({ children }) {
                  return <Text component="h1" size="lg" fw={700} mb="xs">{children}</Text>;
                },
                h2({ children }) {
                  return <Text component="h2" size="md" fw={600} mb="xs">{children}</Text>;
                },
                h3({ children }) {
                  return <Text component="h3" size="sm" fw={600} mb="xs">{children}</Text>;
                },
                ul({ children }) {
                  return <ul style={{ marginLeft: '20px', marginBottom: '8px' }}>{children}</ul>;
                },
                ol({ children }) {
                  return <ol style={{ marginLeft: '20px', marginBottom: '8px' }}>{children}</ol>;
                },
                li({ children }) {
                  return <li style={{ marginBottom: '4px' }}>{children}</li>;
                },
                strong({ children }) {
                  return <strong style={{ fontWeight: 600 }}>{children}</strong>;
                },
                table({ children }) {
                  return (
                    <Box style={{ overflowX: 'auto', marginBottom: '12px' }}>
                      <table
                        style={{
                          borderCollapse: 'collapse',
                          width: '100%',
                          fontSize: '13px',
                        }}
                      >
                        {children}
                      </table>
                    </Box>
                  );
                },
                th({ children }) {
                  return (
                    <th
                      style={{
                        border: `1px solid ${isDark ? 'var(--mantine-color-slate-5)' : 'var(--mantine-color-gray-4)'}`,
                        padding: '8px',
                        textAlign: 'left',
                        backgroundColor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
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
                        border: `1px solid ${isDark ? 'var(--mantine-color-slate-5)' : 'var(--mantine-color-gray-4)'}`,
                        padding: '8px',
                      }}
                    >
                      {children}
                    </td>
                  );
                },
              }}
            >
              {message.content}
            </ReactMarkdown>
          </Box>
        )}
      </Paper>
    </Box>
  );
}
