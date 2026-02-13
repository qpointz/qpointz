import { Box, Text, Badge, Group, Stack, Card, useMantineColorScheme } from '@mantine/core';
import { CodeHighlight } from '@mantine/code-highlight';
import { HiOutlineLightBulb, HiOutlineLink } from 'react-icons/hi2';
import type { Concept } from '../../types/context';
import { InlineChatButton } from '../common/InlineChatButton';
import { RelatedContentButton } from '../common/RelatedContentButton';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
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
  const flags = useFeatureFlags();

  return (
    <Box h="100%">
      {/* Header */}
      <Box
        p="md"
        style={{
          borderBottom: `1px solid var(--mantine-color-default-border)`,
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
        }}
      >
        <Group gap="md" mb="xs" justify="space-between" wrap="nowrap">
          <Group gap="md" wrap="nowrap" style={{ minWidth: 0, flex: 1 }}>
            <Box
              style={{
                width: 40,
                height: 40,
                borderRadius: 8,
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <HiOutlineLightBulb
                size={20}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Box style={{ minWidth: 0, flex: 1 }}>
              <Text size="lg" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                {concept.name}
              </Text>
              <Group gap="xs" mt={4}>
                <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                  {concept.category}
                </Badge>
                {flags.knowledgeSourceBadge && concept.source && (
                  <Badge variant="outline" color={sourceColors[concept.source] || 'gray'} size="sm">
                    {concept.source}
                  </Badge>
                )}
              </Group>
            </Box>
          </Group>
          <Group gap={4} wrap="nowrap">
            <RelatedContentButton
              contextType="knowledge"
              contextId={concept.id}
              contextLabel={concept.name}
            />
            <InlineChatButton
              contextType="knowledge"
              contextId={concept.id}
              contextLabel={concept.name}
            />
          </Group>
        </Group>
      </Box>

      {/* Content */}
      <Box p="md" style={{ overflow: 'auto', height: 'calc(100% - 100px)' }}>
        <Stack gap="lg">
          {/* Description */}
          {flags.knowledgeDescription && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
                Description
              </Text>
              <Text size="sm" c={isDark ? 'gray.2' : 'gray.7'} style={{ lineHeight: 1.6 }}>
                {concept.description}
              </Text>
            </Box>
          )}

          {/* Tags */}
          {flags.knowledgeTags && concept.tags.length > 0 && (
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
          {flags.knowledgeSqlDefinition && concept.sql && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
                SQL Definition
              </Text>
              <CodeHighlight code={concept.sql} language="sql" withCopyButton />
            </Box>
          )}

          {/* Related Entities */}
          {flags.knowledgeRelatedEntities && concept.relatedEntities && concept.relatedEntities.length > 0 && (
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
                      borderColor: 'var(--mantine-color-default-border)',
                      backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-0)',
                    }}
                  >
                    <Group gap="xs">
                      <HiOutlineLink size={14} color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'} />
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
          {flags.knowledgeMetadata && concept.createdAt && (
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
