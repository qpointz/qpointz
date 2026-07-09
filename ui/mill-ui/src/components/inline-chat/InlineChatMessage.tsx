import { Box, Stack, useMantineColorScheme } from '@mantine/core';
import type { Message } from '../../types/chat';
import type { ChatType } from '../chat/artifactPreview/types';
import { ArtifactPreviewRouter } from '../chat/artifactPreview/ArtifactPreviewRouter';
import classes from '../chat/artifactPreview/InlineArtifactStrip.module.css';

interface InlineChatMessageProps {
  message: Message;
  chatType: ChatType;
  conversationId: string;
  chatTitle?: string;
  precedingUserQuestion?: string;
  onArtifactsChange?: (messageId: string, artifacts: NonNullable<Message['artifacts']>) => void;
}

function isArtifactOnlyAssistantMessage(message: Message): boolean {
  if (message.role !== 'assistant') return false;
  if (message.content.trim()) return false;
  return (message.artifacts?.length ?? 0) > 0;
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
  const artifactOnly = isArtifactOnlyAssistantMessage(message);

  const userBg = isDark ? 'var(--mantine-color-cyan-8)' : 'var(--mantine-color-teal-6)';
  const assistantBg = artifactOnly
    ? 'transparent'
    : isDark
      ? 'var(--mantine-color-dark-6)'
      : 'var(--mantine-color-gray-1)';
  const userColor = 'white';
  const assistantColor = 'var(--mantine-color-text)';

  return (
    <Box
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 6,
        paddingLeft: isUser ? 24 : 0,
        paddingRight: isUser ? 0 : 8,
      }}
    >
      <Box
        px={artifactOnly ? 0 : 'xs'}
        py={artifactOnly ? 0 : 6}
        style={{
          backgroundColor: isUser ? userBg : assistantBg,
          color: isUser ? userColor : assistantColor,
          borderRadius: isUser ? '10px 10px 3px 10px' : artifactOnly ? 0 : '10px 10px 10px 3px',
          maxWidth: '100%',
          minWidth: 0,
          width: isUser ? undefined : artifactOnly ? 'fit-content' : '100%',
        }}
      >
        {isUser ? (
          <Box component="span" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.4, fontSize: '12px' }}>
            {message.content}
          </Box>
        ) : artifactOnly ? (
          <Stack gap={4} className={classes.inlineArtifactReply} style={{ fontSize: '12px' }}>
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
          </Stack>
        ) : (
          <Box style={{ fontSize: '12px', width: '100%' }}>
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
