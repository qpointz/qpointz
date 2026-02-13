import { Box, Text, Badge, Group, Stack, Card, useMantineColorScheme } from '@mantine/core';
import { HiArrowLongRight } from 'react-icons/hi2';
import type { RelationFacet as RelationFacetType } from '../../../types/schema';

interface RelationFacetProps {
  relations: RelationFacetType[];
}

const cardinalityColors: Record<string, string> = {
  '1:1': 'blue',
  '1:N': 'green',
  'N:1': 'orange',
  'N:N': 'red',
};

const relationTypeColors: Record<string, string> = {
  FOREIGN_KEY: 'teal',
  LOGICAL: 'violet',
  HIERARCHICAL: 'indigo',
};

export function RelationFacet({ relations }: RelationFacetProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  if (relations.length === 0) {
    return (
      <Box py="md" ta="center">
        <Text size="sm" c="dimmed">
          No relations defined
        </Text>
      </Box>
    );
  }

  return (
    <Stack gap="sm">
      {relations.map((relation) => (
        <Card
          key={relation.id}
          withBorder
          padding="sm"
          radius="md"
          style={{
            borderColor: 'var(--mantine-color-default-border)',
            backgroundColor: isDark ? 'var(--mantine-color-dark-6)' : 'var(--mantine-color-gray-0)',
          }}
        >
          <Stack gap="xs">
            {/* Relation name and badges */}
            <Group justify="space-between">
              <Text size="sm" fw={500}>
                {relation.name}
              </Text>
              <Group gap="xs">
                <Badge
                  size="sm"
                  variant="light"
                  color={cardinalityColors[relation.cardinality] || 'gray'}
                >
                  {relation.cardinality}
                </Badge>
                <Badge
                  size="sm"
                  variant="outline"
                  color={relationTypeColors[relation.relationType] || 'gray'}
                >
                  {relation.relationType.replace('_', ' ')}
                </Badge>
              </Group>
            </Group>

            {/* Source -> Target */}
            <Group gap="xs" wrap="nowrap">
              <Text size="xs" ff="monospace" c={isDark ? 'gray.3' : 'gray.6'}>
                {relation.sourceEntity}
              </Text>
              <HiArrowLongRight
                size={16}
                color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-5)'}
              />
              <Text size="xs" ff="monospace" c={isDark ? 'gray.3' : 'gray.6'}>
                {relation.targetEntity}
              </Text>
            </Group>

            {/* Description */}
            {relation.description && (
              <Text size="xs" c="dimmed">
                {relation.description}
              </Text>
            )}
          </Stack>
        </Card>
      ))}
    </Stack>
  );
}
