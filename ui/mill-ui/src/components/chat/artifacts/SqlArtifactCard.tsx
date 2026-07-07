import { CodeHighlight } from '@mantine/code-highlight';
import { Paper, Stack, Text } from '@mantine/core';
import type { ChatMessageArtifact } from '../../../types/chat';

export function SqlArtifactCard({ artifact }: { artifact: Extract<ChatMessageArtifact, { kind: 'sql' }> }) {
  const title = artifact.info?.title?.trim() || 'SQL query';
  const description = artifact.info?.description?.trim();

  return (
    <Paper withBorder p="sm" radius="md" style={{ maxWidth: '100%' }}>
      <Stack gap="xs">
        <Stack gap={2}>
          <Text size="sm" fw={600}>
            {title}
            {artifact.dialectId ? (
              <Text component="span" size="xs" c="dimmed" ml="xs" fw={400}>
                ({artifact.dialectId})
              </Text>
            ) : null}
          </Text>
          {description ? (
            <Text size="xs" c="dimmed">
              {description}
            </Text>
          ) : null}
        </Stack>
        <CodeHighlight code={artifact.sql} language="sql" withCopyButton />
      </Stack>
    </Paper>
  );
}
