import { Stack, Text } from '@mantine/core';
import type { Message } from '../../../types/chat';
import { MessageContent } from '../../common/MessageContent';
import { deriveAssistantReplyView, structuredReplySectionTitle } from '../../../utils/assistantReplyView';
import { MessageArtifactComposer, type MessageArtifactComposerProps } from './MessageArtifactComposer';

type ArtifactPreviewRouterProps = MessageArtifactComposerProps & {
  message: Message;
};

/** Routes assistant reply layout and artefact treatment by chat type. */
export function ArtifactPreviewRouter({
  message,
  ...composerProps
}: ArtifactPreviewRouterProps) {
  const view = message.assistantReplyView ?? deriveAssistantReplyView(message.artifacts);
  const composer = <MessageArtifactComposer message={message} {...composerProps} />;
  const sectionTitle = structuredReplySectionTitle(view);

  if (sectionTitle) {
    return (
      <Stack gap="sm" style={{ width: '100%' }}>
        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
          {sectionTitle}
        </Text>
        {composer}
        {message.content.trim() ? <MessageContent content={message.content} /> : null}
      </Stack>
    );
  }

  return (
    <Stack gap="sm" style={{ width: '100%' }}>
      {message.content.trim() ? <MessageContent content={message.content} /> : null}
      {composer}
    </Stack>
  );
}
