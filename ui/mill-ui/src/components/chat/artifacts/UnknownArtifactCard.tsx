import { CodeHighlight } from '@mantine/code-highlight';
import { Badge, Button, Collapse, Paper, Stack, Text } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import type { ChatMessageArtifact } from '../../../types/chat';

export function UnknownArtifactCard({
  artifact,
}: {
  artifact: Extract<ChatMessageArtifact, { kind: 'unknown' }>;
}) {
  const [open, { toggle }] = useDisclosure(true);
  const json = JSON.stringify(artifact.payload ?? null, null, 2);

  return (
    <Paper withBorder p="sm" radius="md" style={{ maxWidth: '100%' }}>
      <Stack gap="xs">
        <Stack gap={4}>
          <Text size="sm" fw={600}>
            {artifact.title}
          </Text>
          <Badge size="xs" variant="light" color="gray">
            {artifact.partType}
          </Badge>
        </Stack>
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
