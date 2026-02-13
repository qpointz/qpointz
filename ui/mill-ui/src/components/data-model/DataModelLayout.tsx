import { Box, ScrollArea, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineCircleStack } from 'react-icons/hi2';
import { SchemaTree } from './SchemaTree';
import { EntityDetails } from './EntityDetails';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { schemaService } from '../../services/api';
import { useChatReferencesContext } from '../../context/ChatReferencesContext';
import type { SchemaEntity, EntityFacets } from '../../types/schema';

/** Recursively collect all entity IDs from the schema tree */
function collectEntityIds(entities: SchemaEntity[]): string[] {
  const ids: string[] = [];
  for (const entity of entities) {
    ids.push(entity.id);
    if (entity.children) {
      ids.push(...collectEntityIds(entity.children));
    }
  }
  return ids;
}

export function DataModelLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
  const { prefetchRefs } = useChatReferencesContext();

  const [tree, setTree] = useState<SchemaEntity[] | null>(null);
  const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);
  const [facets, setFacets] = useState<EntityFacets>({});

  // Load schema tree on mount
  useEffect(() => {
    let cancelled = false;
    schemaService.getTree().then((loadedTree) => {
      if (cancelled) return;
      setTree(loadedTree);
      // Prefetch chat references for all schema entities
      const allIds = collectEntityIds(loadedTree);
      prefetchRefs('model', allIds);
    }).catch(() => {
      if (!cancelled) setTree([]);
    });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  }, []);

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
      schemaService.getEntityById(entityId).then((entity) => {
        setSelectedEntity(entity);
        if (entity) {
          schemaService.getEntityFacets(entity.id).then(setFacets).catch(() => setFacets({}));
        } else {
          setFacets({});
        }
      }).catch(() => {
        setSelectedEntity(null);
        setFacets({});
      });
    }
  }, [params]);

  const handleSelect = (entity: SchemaEntity) => {
    setSelectedEntity(entity);
    schemaService.getEntityFacets(entity.id).then(setFacets).catch(() => setFacets({}));
    // Update URL based on entity type
    const parts = entity.id.split('.');
    if (parts.length === 1) {
      navigate(`/model/${parts[0]}`);
    } else if (parts.length === 2) {
      navigate(`/model/${parts[0]}/${parts[1]}`);
    } else if (parts.length >= 3) {
      navigate(`/model/${parts[0]}/${parts[1]}/${parts.slice(2).join('.')}`);
    }
  };

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar - Schema Tree */}
      <CollapsibleSidebar icon={HiOutlineCircleStack} title="Schema Browser">
        {tree !== null && (
          <ScrollArea style={{ flex: 1 }} p="xs">
            <SchemaTree
              tree={tree}
              selectedId={selectedEntity?.id || null}
              onSelect={handleSelect}
            />
          </ScrollArea>
        )}
      </CollapsibleSidebar>

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: 'var(--mantine-color-body)',
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
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
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
