import { CodeHighlight } from '@mantine/code-highlight';
import { Paper, Stack, Text } from '@mantine/core';
import type { ChatMessageArtifact } from '../../../types/chat';

export function SqlArtifactCard({ artifact }: { artifact: Extract<ChatMessageArtifact, { kind: 'sql' }> }) {
  return (
    <Paper withBorder p="sm" radius="md" style={{ maxWidth: '100%' }}>
      <Stack gap="xs">
        <Text size="sm" fw={600}>
          Generated SQL
          {artifact.dialectId ? (
            <Text component="span" size="xs" c="dimmed" ml="xs" fw={400}>
              ({artifact.dialectId})
            </Text>
          ) : null}
        </Text>
        <CodeHighlight code={artifact.sql} language="sql" withCopyButton />
      </Stack>
    </Paper>
  );
}
