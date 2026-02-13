import { Box, Text, Badge, Group, Stack, useMantineColorScheme } from '@mantine/core';
import type { DescriptiveFacet as DescriptiveFacetType } from '../../../types/schema';

interface DescriptiveFacetProps {
  facet: DescriptiveFacetType;
}

export function DescriptiveFacet({ facet }: DescriptiveFacetProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Stack gap="md">
      {facet.displayName && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Display Name
          </Text>
          <Text size="sm" fw={500}>
            {facet.displayName}
          </Text>
        </Box>
      )}

      {facet.description && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Description
          </Text>
          <Text size="sm" c={isDark ? 'gray.2' : 'gray.7'}>
            {facet.description}
          </Text>
        </Box>
      )}

      {facet.businessMeaning && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Business Meaning
          </Text>
          <Text size="sm" c={isDark ? 'gray.2' : 'gray.7'}>
            {facet.businessMeaning}
          </Text>
        </Box>
      )}

      {(facet.businessDomain || facet.businessOwner) && (
        <Group gap="xl">
          {facet.businessDomain && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
                Domain
              </Text>
              <Badge variant="light" color={isDark ? 'cyan' : 'teal'}>
                {facet.businessDomain}
              </Badge>
            </Box>
          )}
          {facet.businessOwner && (
            <Box>
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
                Owner
              </Text>
              <Text size="sm">{facet.businessOwner}</Text>
            </Box>
          )}
        </Group>
      )}

      {facet.synonyms && facet.synonyms.length > 0 && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Synonyms
          </Text>
          <Group gap="xs">
            {facet.synonyms.map((synonym) => (
              <Badge key={synonym} variant="outline" color="gray" size="sm">
                {synonym}
              </Badge>
            ))}
          </Group>
        </Box>
      )}

      {facet.tags && facet.tags.length > 0 && (
        <Box>
          <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>
            Tags
          </Text>
          <Group gap="xs">
            {facet.tags.map((tag) => (
              <Badge key={tag} variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                #{tag}
              </Badge>
            ))}
          </Group>
        </Box>
      )}
    </Stack>
  );
}
