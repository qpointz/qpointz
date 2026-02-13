import { Box, Text, Badge, Group, Tabs, useMantineColorScheme } from '@mantine/core';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineDocumentText,
  HiOutlineCube,
  HiOutlineArrowsRightLeft,
} from 'react-icons/hi2';
import type { SchemaEntity, EntityFacets } from '../../types/schema';
import { DescriptiveFacet } from './facets/DescriptiveFacet';
import { StructuralFacet } from './facets/StructuralFacet';
import { RelationFacet } from './facets/RelationFacet';
import { InlineChatButton } from '../common/InlineChatButton';
import { RelatedContentButton } from '../common/RelatedContentButton';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface EntityDetailsProps {
  entity: SchemaEntity;
  facets: EntityFacets;
}

const entityIcons = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  ATTRIBUTE: HiOutlineViewColumns,
};

const entityLabels = {
  SCHEMA: 'Schema',
  TABLE: 'Table',
  ATTRIBUTE: 'Column',
};

export function EntityDetails({ entity, facets }: EntityDetailsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const Icon = entityIcons[entity.type];

  const hasDescriptive = flags.modelDescriptiveFacet && facets.descriptive && Object.keys(facets.descriptive).length > 0;
  const hasStructural = flags.modelStructuralFacet && facets.structural && Object.keys(facets.structural).length > 0;
  const hasRelations = flags.modelRelationsFacet && facets.relations && facets.relations.length > 0;

  // Determine which tabs to show
  const showTabs = hasDescriptive || hasStructural || hasRelations;

  return (
    <Box h="100%">
      {/* Header */}
      <Box
        p="md"
        style={{
          borderBottom: '1px solid var(--mantine-color-default-border)',
          background: isDark
            ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-7) 100%)'
            : 'linear-gradient(135deg, var(--mantine-color-teal-0) 0%, white 100%)',
        }}
      >
        <Group gap="md" mb="xs" justify="space-between" wrap="nowrap">
          <Group gap="md" wrap="nowrap" style={{ minWidth: 0 }}>
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
              <Icon size={20} color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'} />
            </Box>
            <Box style={{ minWidth: 0 }}>
              <Group gap="xs">
                <Text size="lg" fw={600} c={isDark ? 'gray.1' : 'gray.8'} truncate>
                  {facets.descriptive?.displayName || entity.name}
                </Text>
                <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                  {entityLabels[entity.type]}
                </Badge>
              </Group>
              <Text size="sm" c="dimmed" ff="monospace" truncate>
                {entity.id}
              </Text>
            </Box>
          </Group>
          <Group gap={4} wrap="nowrap">
            <RelatedContentButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entity.name}
              contextEntityType={entity.type}
            />
            <InlineChatButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entity.name}
              contextEntityType={entity.type}
            />
          </Group>
        </Group>

        {/* Quick badges for structural info */}
        {flags.modelQuickBadges && facets.structural && (
          <Group gap="xs" mt="sm">
            {facets.structural.isPrimaryKey && (
              <Badge color={isDark ? 'cyan' : 'teal'} variant="filled" size="sm">
                PK
              </Badge>
            )}
            {facets.structural.isForeignKey && (
              <Badge color="orange" variant="filled" size="sm">
                FK
              </Badge>
            )}
            {facets.structural.isUnique && (
              <Badge color={isDark ? 'cyan' : 'teal'} variant="light" size="sm">
                Unique
              </Badge>
            )}
            {facets.structural.nullable === false && (
              <Badge color="red" variant="light" size="sm">
                Not Null
              </Badge>
            )}
            {flags.modelPhysicalType && facets.structural.physicalType && (
              <Badge variant="outline" color="gray" size="sm">
                {facets.structural.physicalType}
              </Badge>
            )}
          </Group>
        )}
      </Box>

      {/* Content */}
      <Box p="md" style={{ overflow: 'auto', height: 'calc(100% - 120px)' }}>
        {!showTabs ? (
          <Box py="xl" ta="center">
            <Text c="dimmed">No detailed information available for this entity.</Text>
          </Box>
        ) : (
          <Tabs defaultValue={hasDescriptive ? 'descriptive' : hasStructural ? 'structural' : 'relations'}>
            <Tabs.List mb="md">
              {hasDescriptive && (
                <Tabs.Tab value="descriptive" leftSection={<HiOutlineDocumentText size={16} />}>
                  Descriptive
                </Tabs.Tab>
              )}
              {hasStructural && (
                <Tabs.Tab value="structural" leftSection={<HiOutlineCube size={16} />}>
                  Structural
                </Tabs.Tab>
              )}
              {hasRelations && (
                <Tabs.Tab value="relations" leftSection={<HiOutlineArrowsRightLeft size={16} />}>
                  Relations ({facets.relations!.length})
                </Tabs.Tab>
              )}
            </Tabs.List>

            {hasDescriptive && (
              <Tabs.Panel value="descriptive">
                <DescriptiveFacet facet={facets.descriptive!} />
              </Tabs.Panel>
            )}
            {hasStructural && (
              <Tabs.Panel value="structural">
                <StructuralFacet facet={facets.structural!} />
              </Tabs.Panel>
            )}
            {hasRelations && (
              <Tabs.Panel value="relations">
                <RelationFacet relations={facets.relations!} />
              </Tabs.Panel>
            )}
          </Tabs>
        )}
      </Box>
    </Box>
  );
}
