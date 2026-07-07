import { Box, Paper, Text, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import { ArtifactPreviewRouter } from './artifactPreview/ArtifactPreviewRouter';
import type { ChatType } from './artifactPreview/types';
import { AssistantAvatar } from './AssistantAvatar';
import { CHAT_CONTENT_MAX_WIDTH, userBubbleBackground } from './chatChrome';

interface MessageBubbleProps {
  message: Message;
  chatType?: ChatType;
  conversationId?: string;
  chatTitle?: string;
  precedingUserQuestion?: string;
  onArtifactsChange?: (messageId: string, artifacts: NonNullable<Message['artifacts']>) => void;
}

export function MessageBubble({
  message,
  chatType = 'general',
  conversationId = message.conversationId,
  chatTitle,
  precedingUserQuestion,
  onArtifactsChange,
}: MessageBubbleProps) {
  const { colorScheme } = useMantineColorScheme();
  const isUser = message.role === 'user';
  const isDark = colorScheme === 'dark';

  if (isUser) {
    return (
      <Box
        style={{
          display: 'flex',
          justifyContent: 'flex-end',
          marginBottom: 20,
          paddingLeft: 56,
        }}
      >
        <Paper
          shadow="xs"
          p="sm"
          px="md"
          style={{
            background: userBubbleBackground(isDark),
            color: 'white',
            maxWidth: `min(85%, ${CHAT_CONTENT_MAX_WIDTH}px)`,
            borderRadius: '18px 18px 4px 18px',
            boxShadow: isDark
              ? '0 2px 12px rgba(0, 0, 0, 0.35)'
              : '0 2px 10px rgba(18, 184, 166, 0.22)',
          }}
        >
          <Text size="sm" fw={450} lh={1.55} style={{ whiteSpace: 'pre-wrap' }}>
            {message.content}
          </Text>
        </Paper>
      </Box>
    );
  }

  return (
    <Box
      id={`message-${message.id}`}
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: 12,
        marginBottom: 24,
        color: 'var(--mantine-color-text)',
        maxWidth: '100%',
        width: '100%',
      }}
    >
      <AssistantAvatar size={30} />
      <Box style={{ flex: 1, minWidth: 0, paddingTop: 2 }}>
        <ArtifactPreviewRouter
          message={message}
          chatType={chatType}
          conversationId={conversationId}
          chatTitle={chatTitle}
          precedingUserQuestion={precedingUserQuestion}
          onArtifactsChange={
            onArtifactsChange
              ? (artifacts) => onArtifactsChange(message.id, artifacts)
              : undefined
          }
        />
      </Box>
    </Box>
  );
}
