import { Stack, Text } from '@mantine/core';
import type { Message } from '../../types/chat';
import { MessageContent } from '../common/MessageContent';
import { ArtifactCard } from './artifacts/ArtifactCard';
import { deriveAssistantReplyView, structuredReplySectionTitle } from '../../utils/assistantReplyView';

function ArtifactStack({ message }: { message: Message }) {
  const arts = message.artifacts;
  if (!arts?.length) return null;
  return (
    <Stack gap="sm" style={{ marginTop: 4 }}>
      {arts.map((artifact, idx) => (
        <ArtifactCard key={`${artifact.kind}-${idx}`} artifact={artifact} />
      ))}
    </Stack>
  );
}

function StructuredReplyLayout({
  message,
  sectionTitle,
}: {
  message: Message;
  sectionTitle: string;
}) {
  return (
    <Stack gap="sm" style={{ width: '100%' }}>
      <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
        {sectionTitle}
      </Text>
      <ArtifactStack message={message} />
      {message.content.trim() ? <MessageContent content={message.content} /> : null}
    </Stack>
  );
}

/** Picks assistant chrome (grinder-style) from [Message.assistantReplyView] or derived artefacts. */
export function AssistantReplyRouter({ message }: { message: Message }) {
  const view = message.assistantReplyView ?? deriveAssistantReplyView(message.artifacts);
  const sectionTitle = structuredReplySectionTitle(view);

  if (sectionTitle) {
    return <StructuredReplyLayout message={message} sectionTitle={sectionTitle} />;
  }

  return (
    <Stack gap="sm" style={{ width: '100%' }}>
      <MessageContent content={message.content} />
      <ArtifactStack message={message} />
    </Stack>
  );
}
