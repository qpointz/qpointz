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
import { useChatReferences } from '../../context/ChatReferencesContext';

interface EntityDetailsProps {
  entity: SchemaEntity;
  facets: EntityFacets;
}

const entityIcons = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  COLUMN: HiOutlineViewColumns,
};

const entityLabels = {
  SCHEMA: 'Schema',
  TABLE: 'Table',
  COLUMN: 'Column',
};

export function EntityDetails({ entity, facets }: EntityDetailsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const Icon = entityIcons[entity.entityType];
  const entityName = entity.entityType === 'SCHEMA'
    ? entity.schemaName
    : entity.entityType === 'TABLE'
      ? entity.tableName
      : entity.columnName;
  const { refs: chatRefs } = useChatReferences('model', entity.id);
  const relationCount = facets.relations?.length ?? 0;

  const hasDescriptive = flags.modelDescriptiveFacet && facets.descriptive && Object.keys(facets.descriptive).length > 0;
  const hasStructural = flags.modelStructuralFacet && facets.structural && Object.keys(facets.structural).length > 0;
  const hasRelations = flags.modelRelationsFacet && facets.relations && facets.relations.length > 0;

  // Determine which tabs to show
  const showTabs = hasDescriptive || hasStructural || hasRelations;
  const baseTypeLabel = entity.entityType === 'COLUMN' ? entity.type.type : undefined;

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
                  {facets.descriptive?.displayName || entityName}
                </Text>
                <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                  {entityLabels[entity.entityType]}
                </Badge>
              </Group>
              <Text size="sm" c="dimmed" ff="monospace" truncate>
                {entity.id}
              </Text>
            </Box>
          </Group>
          <Group gap={4} wrap="nowrap">
            {relationCount > 0 && (
              <Badge variant="light" color="indigo" size="xs">
                Related {relationCount}
              </Badge>
            )}
            {chatRefs.length > 0 && (
              <Badge variant="light" color="violet" size="xs">
                Chats {chatRefs.length}
              </Badge>
            )}
            <RelatedContentButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entityName}
              contextEntityType={entity.entityType}
            />
            <InlineChatButton
              contextType="model"
              contextId={entity.id}
              contextLabel={entityName}
              contextEntityType={entity.entityType}
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
            {flags.modelPhysicalType && (facets.structural.physicalType || facets.structural.type) && (
              <Badge variant="outline" color="gray" size="sm">
                {facets.structural.type || facets.structural.physicalType}
              </Badge>
            )}
          </Group>
        )}
      </Box>

      {/* Content */}
      <Box p="md" style={{ overflow: 'auto', height: 'calc(100% - 120px)' }}>
        <Box
          mb="md"
          p="sm"
          style={{
            border: '1px solid var(--mantine-color-default-border)',
            borderRadius: 8,
            backgroundColor: isDark ? 'var(--mantine-color-dark-7)' : 'var(--mantine-color-gray-0)',
          }}
        >
          <Group gap="xs" mb="xs">
            <Badge variant="outline" color="gray" size="sm">
              ID {entity.id}
            </Badge>
            {baseTypeLabel && (
              <Badge variant="light" color={isDark ? 'cyan' : 'teal'} size="sm">
                {baseTypeLabel}
              </Badge>
            )}
          </Group>
          {entity.entityType === 'SCHEMA' && (
            <Text size="sm" c="dimmed">
              Tables: {entity.tables.length}
            </Text>
          )}
          {entity.entityType === 'TABLE' && (
            <Text size="sm" c="dimmed">
              Table type: {entity.tableType} · Columns: {entity.columns.length}
            </Text>
          )}
          {entity.entityType === 'COLUMN' && (
            <Text size="sm" c="dimmed">
              Column: {entity.columnName} · Position: {entity.fieldIndex}
            </Text>
          )}
        </Box>

        {!showTabs ? (
          <Box py="xl" ta="center">
            <Text c="dimmed">No metadata facets available for this entity yet.</Text>
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
