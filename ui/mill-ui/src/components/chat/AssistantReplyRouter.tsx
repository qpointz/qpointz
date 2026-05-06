import { Stack, Text } from '@mantine/core';
import type { Message } from '../../types/chat';
import { MessageContent } from '../common/MessageContent';
import { SqlArtifactCard } from './artifacts/SqlArtifactCard';
import { FacetProposalArtifactCard } from './artifacts/FacetProposalArtifactCard';
import { deriveAssistantReplyView } from '../../utils/assistantReplyView';

function ArtifactStack({ message }: { message: Message }) {
  const arts = message.artifacts;
  if (!arts?.length) return null;
  return (
    <Stack gap="sm" style={{ marginTop: 4 }}>
      {arts.map((a, idx) =>
        a.kind === 'sql' ? (
          <SqlArtifactCard key={`${a.kind}-${idx}`} artifact={a} />
        ) : (
          <FacetProposalArtifactCard key={`${a.kind}-${idx}-${a.metadataEntityId}`} artifact={a} />
        ),
      )}
    </Stack>
  );
}

/** Picks assistant chrome (grinder-style) from [Message.assistantReplyView] or derived artefacts. */
export function AssistantReplyRouter({ message }: { message: Message }) {
  const view = message.assistantReplyView ?? deriveAssistantReplyView(message.artifacts);

  if (view === 'sql-primary') {
    return (
      <Stack gap="sm" style={{ width: '100%' }}>
        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
          SQL
        </Text>
        <ArtifactStack message={message} />
        {message.content.trim() ? <MessageContent content={message.content} /> : null}
      </Stack>
    );
  }

  if (view === 'facet-primary') {
    return (
      <Stack gap="sm" style={{ width: '100%' }}>
        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
          Facet proposal
        </Text>
        <ArtifactStack message={message} />
        {message.content.trim() ? <MessageContent content={message.content} /> : null}
      </Stack>
    );
  }

  return (
    <Stack gap="sm" style={{ width: '100%' }}>
      <MessageContent content={message.content} />
      <ArtifactStack message={message} />
    </Stack>
  );
}
