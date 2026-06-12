import { Box, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import type { ChatType } from '../chat/artifactPreview/types';
import { ArtifactPreviewRouter } from '../chat/artifactPreview/ArtifactPreviewRouter';

interface InlineChatMessageProps {
  message: Message;
  chatType: ChatType;
  conversationId: string;
  chatTitle?: string;
  precedingUserQuestion?: string;
  onArtifactsChange?: (messageId: string, artifacts: NonNullable<Message['artifacts']>) => void;
}

export function InlineChatMessage({
  message,
  chatType,
  conversationId,
  chatTitle,
  precedingUserQuestion,
  onArtifactsChange,
}: InlineChatMessageProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const isUser = message.role === 'user';

  const userBg = isDark ? 'var(--mantine-color-cyan-8)' : 'var(--mantine-color-teal-6)';
  const assistantBg = isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-1)';
  const userColor = 'white';
  const assistantColor = 'var(--mantine-color-text)';

  return (
    <Box
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 6,
        paddingLeft: isUser ? 32 : 0,
        paddingRight: isUser ? 0 : 32,
      }}
    >
      <Box
        px="xs"
        py={6}
        style={{
          backgroundColor: isUser ? userBg : assistantBg,
          color: isUser ? userColor : assistantColor,
          borderRadius: isUser ? '10px 10px 3px 10px' : '10px 10px 10px 3px',
          maxWidth: '100%',
          minWidth: 0,
          width: isUser ? undefined : '100%',
        }}
      >
        {isUser ? (
          <Box component="span" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.4, fontSize: '12px' }}>
            {message.content}
          </Box>
        ) : (
          <Box style={{ fontSize: '12px' }}>
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
        )}
      </Box>
    </Box>
  );
}
