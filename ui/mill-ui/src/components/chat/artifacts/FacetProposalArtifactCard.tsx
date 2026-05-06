import { CodeHighlight } from '@mantine/code-highlight';
import { Button, Collapse, Paper, Stack, Text } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import type { ChatMessageArtifact } from '../../../types/chat';

export function FacetProposalArtifactCard({
  artifact,
}: {
  artifact: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>;
}) {
  const [open, { toggle }] = useDisclosure(false);
  const json = JSON.stringify(artifact.payload ?? null, null, 2);

  return (
    <Paper withBorder p="sm" radius="md" style={{ maxWidth: '100%' }}>
      <Stack gap="xs">
        <Text size="sm" fw={600}>
          Facet proposal
        </Text>
        <Text size="xs" ff="monospace">
          {artifact.metadataEntityId}
        </Text>
        <Text size="xs" c="dimmed">
          {artifact.facetTypeKey}
        </Text>
        <Button variant="subtle" size="xs" onClick={toggle}>
          {open ? 'Hide payload' : 'Show payload'}
        </Button>
        <Collapse in={open}>
          <CodeHighlight code={json} language="json" withCopyButton />
        </Collapse>
      </Stack>
    </Paper>
  );
}
