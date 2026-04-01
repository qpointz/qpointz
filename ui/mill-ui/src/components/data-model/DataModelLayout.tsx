import { Box, Loader, ScrollArea, Select, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineCircleStack } from 'react-icons/hi2';
import { SchemaTree } from './SchemaTree';
import { EntityDetails } from './EntityDetails';
import { ExplorerSplitLayout } from '../layout/ExplorerSplitLayout';
import { buildEntityFacetsFromResolvedList, metadataEntityUrnForFacetApi, schemaService } from '../../services/api';
import type { SchemaNode, SchemaEntity, EntityFacets, ScopeOption, TableDetail } from '../../types/schema';

/**
 * Prefers schema explorer `facetsResolved` on the entity when present (WI-134 full constellation);
 * otherwise loads legacy `GET /metadata/entities/{id}/facets`.
 *
 * **TEMPORARY:** Metadata fallback exists for pre–WI-134 servers. Remove this branch once WI-134 is
 * deployed to all target environments so absent `facetsResolved` surfaces as a contract gap instead
 * of silently masking API drift.
 */
async function loadFacetsForEntity(
  entity: SchemaEntity,
  selectedContext: string,
  signal?: AbortSignal
): Promise<EntityFacets> {
  const fr = entity.facetsResolved;
  if (fr != null) {
    return buildEntityFacetsFromResolvedList(fr);
  }
  const metaUrn = metadataEntityUrnForFacetApi(entity);
  if (!metaUrn) return {};
  return schemaService.getEntityFacets(metaUrn, selectedContext, signal);
}

function enrichNodeChildren(
  nodes: SchemaNode[],
  targetId: string,
  children: SchemaNode[]
): SchemaNode[] {
  return nodes.map((node) => {
    if (node.id === targetId) {
      return { ...node, children };
    }
    if (!node.children || node.children.length === 0) {
      return node;
    }
    return { ...node, children: enrichNodeChildren(node.children, targetId, children) };
  });
}

function tableColumnNodes(entity: TableDetail): SchemaNode[] {
  return entity.columns.map((column) => ({
    id: column.id,
    type: 'COLUMN' as const,
    name: column.columnName,
  }));
}

/**
 * Data Model explorer route: scope selector, schema tree (including model root per SPEC §3f), and
 * {@link EntityDetails} for the selected entity.
 */
export function DataModelLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ schema?: string; table?: string; attribute?: string }>();

  const [tree, setTree] = useState<SchemaNode[] | null>(null);
  const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);
  const [facets, setFacets] = useState<EntityFacets>({});
  const [selectedContext, setSelectedContext] = useState<string>('global');
  const [availableScopes, setAvailableScopes] = useState<ScopeOption[]>([]);
  const [treeLoading, setTreeLoading] = useState<boolean>(true);
  const [entityLoading, setEntityLoading] = useState<boolean>(false);
  const treeRequestIdRef = useRef(0);
  const entityRequestIdRef = useRef(0);

  // Load schema context and tree on mount
  useEffect(() => {
    let cancelled = false;
    const currentRequestId = ++treeRequestIdRef.current;
    setTreeLoading(true);
    schemaService.getContext().then((contextInfo) => {
      if (cancelled || currentRequestId !== treeRequestIdRef.current) return;
      const ctx = contextInfo.selectedContext || 'global';
      setSelectedContext(ctx);
      setAvailableScopes(contextInfo.availableScopes ?? []);
      return schemaService.getTree(ctx).then((loadedTree) => {
        if (cancelled || currentRequestId !== treeRequestIdRef.current) return;
        setTree(loadedTree);
        setTreeLoading(false);
      });
    }).catch(() => {
      if (!cancelled && currentRequestId === treeRequestIdRef.current) {
        setSelectedContext('global');
        setTree([]);
        setTreeLoading(false);
      }
    });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  }, []);

  // Sync URL params to selected entity (AbortController cancels stale fetches — dev StrictMode + fast param changes)
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

    if (!entityId) {
      setEntityLoading(false);
      return;
    }

    const ac = new AbortController();
    const { signal } = ac;
    const currentRequestId = ++entityRequestIdRef.current;
    setEntityLoading(true);
    schemaService.getEntityById(entityId, selectedContext, signal).then((entity) => {
      if (signal.aborted || currentRequestId !== entityRequestIdRef.current) return;
      setSelectedEntity(entity);
      if (entity) {
        void loadFacetsForEntity(entity, selectedContext, signal)
          .then((nextFacets) => {
            if (signal.aborted || currentRequestId !== entityRequestIdRef.current) return;
            setFacets(nextFacets);
          })
          .catch((e) => {
            if (e instanceof DOMException && e.name === 'AbortError') return;
            if (signal.aborted) return;
            setFacets({});
          });
        if (entity.entityType === 'TABLE') {
          const columnNodes = tableColumnNodes(entity);
          setTree((prev) => (prev ? enrichNodeChildren(prev, entity.id, columnNodes) : prev));
        } else if (entity.entityType === 'COLUMN') {
          const tableId = `${entity.schemaName}.${entity.tableName}`;
          void schemaService
            .getTable(entity.schemaName, entity.tableName, selectedContext, 'none', signal)
            .then((table) => {
              if (signal.aborted || currentRequestId !== entityRequestIdRef.current || !table) return;
              const columnNodes = tableColumnNodes(table);
              setTree((prev) => (prev ? enrichNodeChildren(prev, tableId, columnNodes) : prev));
            });
        }
      } else {
        setFacets({});
      }
      setEntityLoading(false);
    }).catch((e) => {
      if (e instanceof DOMException && e.name === 'AbortError') return;
      if (currentRequestId !== entityRequestIdRef.current) return;
      setSelectedEntity(null);
      setFacets({});
      setEntityLoading(false);
    });

    return () => {
      ac.abort();
    };
  }, [params, selectedContext]);

  const handleScopeChange = useCallback((scope: string) => {
    const currentRequestId = ++treeRequestIdRef.current;
    setSelectedContext(scope);
    setTree(null);
    setSelectedEntity(null);
    setFacets({});
    setTreeLoading(true);
    schemaService.getTree(scope).then((loadedTree) => {
      if (currentRequestId !== treeRequestIdRef.current) return;
      setTree(loadedTree);
      setTreeLoading(false);
    }).catch(() => {
      if (currentRequestId !== treeRequestIdRef.current) return;
      setTree([]);
      setTreeLoading(false);
    });
  }, []);

  const handleSelect = (node: SchemaNode) => {
    const currentRequestId = ++entityRequestIdRef.current;
    setEntityLoading(true);
    schemaService.getEntityById(node.id, selectedContext).then((entity) => {
      if (currentRequestId !== entityRequestIdRef.current) return;
      setSelectedEntity(entity);
      if (!entity) {
        setFacets({});
        setEntityLoading(false);
        return;
      }
      void loadFacetsForEntity(entity, selectedContext).then(setFacets).catch(() => setFacets({}));
      // Lazy-load table columns into the tree only when table is selected.
      if (entity.entityType === 'TABLE') {
        const columnNodes: SchemaNode[] = entity.columns.map((column) => ({
          id: column.id,
          type: 'COLUMN',
          name: column.columnName,
        }));
        setTree((prev) => (prev ? enrichNodeChildren(prev, node.id, columnNodes) : prev));
      }
      setEntityLoading(false);
    }).catch(() => {
      setSelectedEntity(null);
      setFacets({});
      setEntityLoading(false);
    });
    // Update URL based on entity type
    const parts = node.id.split('.');
    if (parts.length === 1) {
      navigate(`/model/${parts[0]}`);
    } else if (parts.length === 2) {
      navigate(`/model/${parts[0]}/${parts[1]}`);
    } else if (parts.length >= 3) {
      navigate(`/model/${parts[0]}/${parts[1]}/${parts.slice(2).join('.')}`);
    }
  };

  const scopeSelect = availableScopes.length > 1 ? (
    <Select
      size="xs"
      value={selectedContext}
      data={availableScopes.map((s) => ({ value: s.id, label: s.displayName }))}
      onChange={(v) => { if (v) handleScopeChange(v); }}
      style={{ minWidth: 140 }}
    />
  ) : null;

  return (
    <ExplorerSplitLayout
      icon={HiOutlineCircleStack}
      title="Schema Browser"
      viewPaneHeader={scopeSelect}
      sidebarBody={
        treeLoading ? (
          <Box p="md" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
            <Loader size="sm" />
            <Text size="xs" c="dimmed">Loading model...</Text>
          </Box>
        ) : tree !== null ? (
          <ScrollArea style={{ flex: 1 }} p="xs">
            <SchemaTree
              tree={tree}
              selectedId={selectedEntity?.id || null}
              onSelect={handleSelect}
            />
          </ScrollArea>
        ) : null
      }
      main={
        selectedEntity ? (
          entityLoading ? (
            <Box p="md" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
              <Loader size="sm" />
              <Text size="xs" c="dimmed">Loading details...</Text>
            </Box>
          ) : (
            <EntityDetails
              entity={selectedEntity}
              facets={facets}
              selectedContext={selectedContext}
              onFacetsChanged={async () => {
                const reloaded = await schemaService.getEntityById(selectedEntity.id, selectedContext);
                if (!reloaded) {
                  setFacets({});
                  return;
                }
                setFacets(await loadFacetsForEntity(reloaded, selectedContext));
              }}
            />
          )
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
        )
      }
    />
  );
}
