import { Box, Paper, Text, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import { ArtifactPreviewRouter } from './artifactPreview/ArtifactPreviewRouter';
import type { ChatType } from './artifactPreview/types';

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

  const userBgColor = isDark ? 'var(--mantine-color-cyan-7)' : 'var(--mantine-color-teal-6)';

  if (isUser) {
    return (
      <Box
        style={{
          display: 'flex',
          justifyContent: 'flex-end',
          marginBottom: '12px',
          paddingLeft: '48px',
        }}
      >
        <Paper
          shadow="sm"
          p="sm"
          style={{
            backgroundColor: userBgColor,
            color: 'white',
            maxWidth: '100%',
            borderRadius: '16px 16px 4px 16px',
          }}
        >
          <Text size="sm" style={{ whiteSpace: 'pre-wrap' }}>
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
        justifyContent: 'flex-start',
        marginBottom: '16px',
        color: 'var(--mantine-color-text)',
        maxWidth: '100%',
        width: '100%',
      }}
    >
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
  );
}
