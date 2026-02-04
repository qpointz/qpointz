import { Box, Text, useMantineColorScheme } from '@mantine/core';
import { useChat } from '../../context/ChatContext';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';

export function ChatArea() {
  const { activeConversation, sendMessage, state } = useChat();
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        backgroundColor: isDark ? 'var(--mantine-color-slate-8)' : 'white',
      }}
    >
      {/* Header */}
      <Box
        p="md"
        style={{
          borderBottom: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-slate-9) 0%, var(--mantine-color-slate-8) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
        }}
      >
        <Text
          size="lg"
          fw={600}
          c={isDark ? 'slate.1' : 'slate.8'}
          style={{
            maxWidth: '900px',
            margin: '0 auto',
          }}
        >
          {activeConversation?.title || 'New Chat'}
        </Text>
      </Box>

      {/* Messages */}
      <Box
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          maxWidth: '900px',
          width: '100%',
          margin: '0 auto',
        }}
      >
        <MessageList
          messages={activeConversation?.messages || []}
          isLoading={state.isLoading}
        />
      </Box>

      {/* Input */}
      <MessageInput onSend={sendMessage} disabled={state.isLoading} />
    </Box>
  );
}
