import { Box, ScrollArea, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineCircleStack } from 'react-icons/hi2';
import { SchemaTree } from './SchemaTree';
import { EntityDetails } from './EntityDetails';
import { mockSchemaTree, getEntityFacets, findEntityById } from '../../data/mockSchema';
import type { SchemaEntity } from '../../types/schema';

export function DataModelLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ schema?: string; table?: string; attribute?: string }>();

  const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);

  // Sync URL params to selected entity
  useEffect(() => {
    let entityId = '';
    if (params.schema) {
      entityId = params.schema;
      if (params.table) {
        entityId += `.${params.table}`;
        if (params.attribute) {
          entityId += `.${params.attribute}`;
        }
      }
    }

    if (entityId) {
      const entity = findEntityById(entityId);
      setSelectedEntity(entity);
    } else {
      setSelectedEntity(null);
    }
  }, [params]);

  const handleSelect = (entity: SchemaEntity) => {
    setSelectedEntity(entity);
    // Update URL based on entity type
    const parts = entity.id.split('.');
    if (parts.length === 1) {
      navigate(`/data-model/${parts[0]}`);
    } else if (parts.length === 2) {
      navigate(`/data-model/${parts[0]}/${parts[1]}`);
    } else if (parts.length >= 3) {
      navigate(`/data-model/${parts[0]}/${parts[1]}/${parts.slice(2).join('.')}`);
    }
  };

  const facets = selectedEntity ? getEntityFacets(selectedEntity.id) : {};

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar - Schema Tree */}
      <Box
        style={{
          width: 280,
          borderRight: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
          backgroundColor: isDark ? 'var(--mantine-color-slate-9)' : 'var(--mantine-color-slate-0)',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Box
          p="md"
          style={{
            borderBottom: `1px solid ${isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-gray-3)'}`,
          }}
        >
          <Text size="sm" fw={600} c={isDark ? 'slate.2' : 'slate.7'}>
            Schema Browser
          </Text>
        </Box>
        <ScrollArea style={{ flex: 1 }} p="xs">
          <SchemaTree
            tree={mockSchemaTree}
            selectedId={selectedEntity?.id || null}
            onSelect={handleSelect}
          />
        </ScrollArea>
      </Box>

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: isDark ? 'var(--mantine-color-slate-8)' : 'white',
          overflow: 'hidden',
        }}
      >
        {selectedEntity ? (
          <EntityDetails entity={selectedEntity} facets={facets} />
        ) : (
          <Box
            style={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Box
              style={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: 24,
              }}
            >
              <HiOutlineCircleStack
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'slate.1' : 'slate.8'} mb="xs">
              Data Model Explorer
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Select a schema, table, or column from the tree on the left to view its details and metadata.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
