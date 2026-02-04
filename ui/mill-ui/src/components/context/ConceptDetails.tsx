import { Box, Text, Badge, Group, Stack, Card, useMantineColorScheme } from '@mantine/core';
import { CodeHighlight } from '@mantine/code-highlight';
import { HiOutlineLightBulb, HiOutlineLink } from 'react-icons/hi2';
import type { Concept } from '../../types/context';
import '@mantine/code-highlight/styles.css';

interface ConceptDetailsProps {
  concept: Concept;
}

const sourceColors: Record<string, string> = {
  MANUAL: 'teal',
  INFERRED: 'violet',
  IMPORTED: 'blue',
};

export function ConceptDetails({ concept }: ConceptDetailsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box h="100%">
      {/* Header */}
      <Box
        p="md"
        style={{
          borderBottom: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-slate-9) 0%, var(--mantine-color-slate-8) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
        }}
      >
        <Group gap="md" mb="xs">
          <Box
            style={{
              width: 40,
              height: 40,
              borderRadius: 8,
              backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <HiOutlineLightBulb
              size={20}
              color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
            />
          </Box>
          <Box style={{ flex: 1 }}>
            <Text size="lg" fw={600} c={isDark ? 'slate.1' : 'slate.8'}>
              {concept.name}
            </Text>
            <Group gap="xs" mt={4}>
              <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                {concept.category}
              </Badge>
              {concept.source && (
                <Badge variant="outline" color={sourceColors[concept.source] || 'gray'} size="sm">
                  {concept.source}
                </Badge>
              )}
            </Group>
          </Box>
        </Group>
      </Box>

      {/* Content */}
      <Box p="md" style={{ overflow: 'auto', height: 'calc(100% - 100px)' }}>
        <Stack gap="lg">
          {/* Description */}
          <Box>
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
              Description
            </Text>
            <Text size="sm" c={isDark ? 'slate.2' : 'slate.7'} style={{ lineHeight: 1.6 }}>
              {concept.description}
            </Text>
          </Box>

          {/* Tags */}
          {concept.tags.length > 0 && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
                Tags
              </Text>
              <Group gap="xs">
                {concept.tags.map((tag) => (
                  <Badge key={tag} variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                    #{tag}
                  </Badge>
                ))}
              </Group>
            </Box>
          )}

          {/* SQL Definition */}
          {concept.sql && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
                SQL Definition
              </Text>
              <CodeHighlight code={concept.sql} language="sql" withCopyButton />
            </Box>
          )}

          {/* Related Entities */}
          {concept.relatedEntities && concept.relatedEntities.length > 0 && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
                Related Entities
              </Text>
              <Stack gap="xs">
                {concept.relatedEntities.map((entity) => (
                  <Card
                    key={entity}
                    withBorder
                    padding="xs"
                    radius="sm"
                    style={{
                      borderColor: isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)',
                      backgroundColor: isDark ? 'var(--mantine-color-slate-8)' : 'var(--mantine-color-gray-0)',
                    }}
                  >
                    <Group gap="xs">
                      <HiOutlineLink size={14} color={isDark ? 'var(--mantine-color-slate-4)' : 'var(--mantine-color-gray-5)'} />
                      <Text size="sm" ff="monospace">
                        {entity}
                      </Text>
                    </Group>
                  </Card>
                ))}
              </Stack>
            </Box>
          )}

          {/* Metadata */}
          {concept.createdAt && (
            <Box>
              <Text size="xs" c="dimmed">
                Created: {new Date(concept.createdAt).toLocaleDateString()}
              </Text>
            </Box>
          )}
        </Stack>
      </Box>
    </Box>
  );
}
