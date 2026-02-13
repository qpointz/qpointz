import { Box, Text, Badge, Group, Stack, useMantineColorScheme } from '@mantine/core';
import type { StructuralFacet as StructuralFacetType } from '../../../types/schema';

interface StructuralFacetProps {
  facet: StructuralFacetType;
}

export function StructuralFacet({ facet }: StructuralFacetProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const formatType = () => {
    let type = facet.physicalType || 'Unknown';
    if (facet.precision !== undefined) {
      if (facet.scale !== undefined) {
        type += `(${facet.precision}, ${facet.scale})`;
      } else {
        type += `(${facet.precision})`;
      }
    }
    return type;
  };

  return (
    <Stack gap="md">
      {/* Constraints badges */}
      <Box>
        <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={8}>
          Constraints
        </Text>
        <Group gap="xs">
          {facet.isPrimaryKey && (
            <Badge color={isDark ? 'cyan' : 'teal'} variant="filled">
              Primary Key
            </Badge>
          )}
          {facet.isForeignKey && (
            <Badge color="orange" variant="filled">
              Foreign Key
            </Badge>
          )}
          {facet.isUnique && (
            <Badge color={isDark ? 'cyan' : 'teal'} variant="light">
              Unique
            </Badge>
          )}
          {facet.nullable === false && (
            <Badge color="red" variant="light">
              Not Null
            </Badge>
          )}
          {facet.nullable === true && (
            <Badge color="gray" variant="light">
              Nullable
            </Badge>
          )}
        </Group>
      </Box>

      {/* Physical properties */}
      <Group gap="xl" align="flex-start">
        {facet.physicalName && (
          <Box>
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
              Physical Name
            </Text>
            <Text size="sm" ff="monospace">
              {facet.physicalName}
            </Text>
          </Box>
        )}

        {facet.physicalType && (
          <Box>
            <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
              Data Type
            </Text>
            <Badge variant="outline" color={isDark ? 'gray.4' : 'gray.6'} size="lg">
              {formatType()}
            </Badge>
          </Box>
        )}
      </Group>

      {facet.defaultValue !== undefined && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Default Value
          </Text>
          <Text size="sm" ff="monospace" c={isDark ? 'gray.3' : 'gray.6'}>
            {facet.defaultValue || 'NULL'}
          </Text>
        </Box>
      )}
    </Stack>
  );
}
