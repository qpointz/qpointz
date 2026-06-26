import { Box, Loader, ScrollArea, Select, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router';
import { HiOutlineCircleStack } from 'react-icons/hi2';
import { SchemaTree } from './SchemaTree';
import { EntityDetails } from './EntityDetails';
import { ExplorerSplitLayout } from '../layout/ExplorerSplitLayout';
import { entityIdFromModelRouteParams } from './modelRouteEntityId';
import { resolveTreeTableId } from './catalogEntityId';
import { enrichNodeChildren } from './schemaTreeEnrichment';
import { enrichExplorerTreeColumns, loadExplorerTreeWithColumns } from './schemaTreeLoad';
import { buildEntityFacetsFromResolvedList, metadataEntityUrnForFacetApi, schemaService } from '../../services/api';
import type { SchemaNode, SchemaEntity, EntityFacets, ScopeOption } from '../../types/schema';

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

/**
 * Data Model explorer route: scope selector, schema tree (including model root per SPEC §3f), and
 * {@link EntityDetails} for the selected entity.
 */
export function DataModelLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
  const routeEntityId = useMemo(() => entityIdFromModelRouteParams(params), [params]);

  const [tree, setTree] = useState<SchemaNode[] | null>(null);
  const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);
  const [facets, setFacets] = useState<EntityFacets>({});
  const [selectedContext, setSelectedContext] = useState<string>('global');
  const [availableScopes, setAvailableScopes] = useState<ScopeOption[]>([]);
  const [treeLoading, setTreeLoading] = useState<boolean>(true);
  const [entityLoading, setEntityLoading] = useState<boolean>(false);
  const [contextReady, setContextReady] = useState<boolean>(false);
  const treeRequestIdRef = useRef(0);
  const entityRequestIdRef = useRef(0);
  const treeRef = useRef<SchemaNode[] | null>(null);
  treeRef.current = tree;

  const loadTreeForContext = useCallback(async (context: string, requestId: number, routeIdForColumns: string) => {
    const loadedTree = await loadExplorerTreeWithColumns(context, routeIdForColumns, null);
    if (requestId !== treeRequestIdRef.current) return;
    setTree(loadedTree);
    setTreeLoading(false);
  }, []);

  // Resolve scope, then load the explorer tree once (deep-link columns included via route at init).
  useEffect(() => {
    let cancelled = false;
    const currentRequestId = ++treeRequestIdRef.current;
    const routeAtInit = routeEntityId;
    setTreeLoading(true);
    setContextReady(false);
    schemaService.getContext().then(async (contextInfo) => {
      if (cancelled || currentRequestId !== treeRequestIdRef.current) return;
      const ctx = contextInfo.selectedContext || 'global';
      setSelectedContext(ctx);
      setAvailableScopes(contextInfo.availableScopes ?? []);
      setContextReady(true);
      await loadTreeForContext(ctx, currentRequestId, routeAtInit);
    }).catch(() => {
      if (!cancelled && currentRequestId === treeRequestIdRef.current) {
        setSelectedContext('global');
        setContextReady(true);
        setTree([]);
        setTreeLoading(false);
      }
    });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- tree loads once per mount; route changes patch columns only
  }, [loadTreeForContext]);

  // Sync URL params to selected entity (AbortController cancels stale fetches — dev StrictMode + fast param changes)
  useEffect(() => {
    const entityId = routeEntityId;

    if (!contextReady) return;

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
  }, [routeEntityId, selectedContext, contextReady]);

  // Lazy-load column children when the route deep-links to a table/column without refetching the tree.
  useEffect(() => {
    if (!contextReady || treeRef.current === null || !routeEntityId) return;

    let cancelled = false;
    void enrichExplorerTreeColumns(
      treeRef.current,
      selectedContext,
      routeEntityId,
      selectedEntity,
    ).then((nextTree) => {
      if (cancelled || nextTree === treeRef.current) return;
      setTree(nextTree);
    });

    return () => {
      cancelled = true;
    };
  }, [routeEntityId, selectedContext, contextReady, selectedEntity]);

  const handleScopeChange = useCallback((scope: string) => {
    const currentRequestId = ++treeRequestIdRef.current;
    setSelectedContext(scope);
    setTree(null);
    setSelectedEntity(null);
    setFacets({});
    setTreeLoading(true);
    void loadExplorerTreeWithColumns(scope, routeEntityId, null).then((loadedTree) => {
      if (currentRequestId !== treeRequestIdRef.current) return;
      setTree(loadedTree);
      setTreeLoading(false);
    }).catch(() => {
      if (currentRequestId !== treeRequestIdRef.current) return;
      setTree([]);
      setTreeLoading(false);
    });
  }, [routeEntityId]);

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
        const tableId = resolveTreeTableId(tree ?? [], entity.schemaName, entity.tableName) ?? node.id;
        const columnNodes: SchemaNode[] = entity.columns.map((column) => ({
          id: column.id,
          type: 'COLUMN',
          name: column.columnName,
        }));
        setTree((prev) => (prev ? enrichNodeChildren(prev, tableId, columnNodes) : prev));
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
              selectedId={(selectedEntity?.id ?? routeEntityId) || null}
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
