import { Box, Group, Loader, ScrollArea, Text, useMantineColorScheme } from '@mantine/core';
import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router';
import { HiOutlineCircleStack } from 'react-icons/hi2';
import { SchemaTree } from './SchemaTree';
import { EntityDetails } from './EntityDetails';
import { ExplorerSplitLayout } from '../layout/ExplorerSplitLayout';
import { MetadataScopeCheckboxPicker } from './MetadataScopeCheckboxPicker';
import { entityIdFromModelRouteParams } from './modelRouteEntityId';
import { resolveTreeTableId } from './catalogEntityId';
import { enrichNodeChildren } from './schemaTreeEnrichment';
import { enrichExplorerTreeColumns, loadExplorerTreeWithColumns } from './schemaTreeLoad';
import { buildEntityFacetsFromResolvedList, metadataEntityUrnForFacetApi, schemaService } from '../../services/api';
import type { SchemaNode, SchemaEntity, EntityFacets } from '../../types/schema';
import {
  modelEntityLoadKey,
  modelViewSearchFromParams,
  resolveModelScopeFromSearchParams,
  scopeSearchParamsAfterReadScopeChange,
} from '../../utils/modelScopeQuery';

/**
 * Prefers schema explorer `facetsResolved` on the entity when present (WI-134 full constellation);
 * otherwise loads legacy `GET /metadata/entities/{id}/facets`.
 */
async function loadFacetsForEntity(
  entity: SchemaEntity,
  scopeQuery: string,
  signal?: AbortSignal
): Promise<EntityFacets> {
  const fr = entity.facetsResolved;
  if (fr != null) {
    return buildEntityFacetsFromResolvedList(fr);
  }
  const metaUrn = metadataEntityUrnForFacetApi(entity);
  if (!metaUrn) return {};
  return schemaService.getEntityFacets(metaUrn, scopeQuery, signal);
}

/**
 * Data Model explorer route: URL-driven scope selection, schema tree, and {@link EntityDetails}.
 */
export function DataModelLayout() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
  const routeEntityId = useMemo(() => entityIdFromModelRouteParams(params), [params]);

  const modelScope = useMemo(
    () => resolveModelScopeFromSearchParams(searchParams),
    [searchParams],
  );
  const { declaredScopes, readScopes, scopeQuery } = modelScope;
  const hasActiveReadScopes = readScopes.length > 0;
  const modelViewSearch = useMemo(
    () => modelViewSearchFromParams(searchParams),
    [searchParams],
  );
  /** Distinguishes implicit all-scopes URLs from explicit readScope subsets with the same scopeQuery. */
  const entityScopeLoadKey = useMemo(
    () => (routeEntityId ? modelEntityLoadKey(routeEntityId, scopeQuery, searchParams) : null),
    [routeEntityId, scopeQuery, searchParams],
  );

  const [tree, setTree] = useState<SchemaNode[] | null>(null);
  const [selectedEntity, setSelectedEntity] = useState<SchemaEntity | null>(null);
  const [facets, setFacets] = useState<EntityFacets>({});
  const [treeLoading, setTreeLoading] = useState<boolean>(true);
  const [entityLoading, setEntityLoading] = useState<boolean>(false);
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

  // Reload explorer tree when read scopes change; route/deep-link column patches use enrichExplorerTreeColumns.
  useEffect(() => {
    if (!hasActiveReadScopes) {
      setTreeLoading(false);
      setTree([]);
      setSelectedEntity(null);
      setFacets({});
      return;
    }
    let cancelled = false;
    const currentRequestId = ++treeRequestIdRef.current;
    const routeAtInit = routeEntityId;
    setTreeLoading(true);
    setTree(null);
    void loadTreeForContext(scopeQuery, currentRequestId, routeAtInit).catch(() => {
      if (!cancelled && currentRequestId === treeRequestIdRef.current) {
        setTree([]);
        setTreeLoading(false);
      }
    });
    return () => { cancelled = true; };
    // routeEntityId omitted — navigation patches columns only (see enrichExplorerTreeColumns effect).
  }, [hasActiveReadScopes, loadTreeForContext, scopeQuery]);

  useEffect(() => {
    const entityId = routeEntityId;

    if (!hasActiveReadScopes || !entityId) {
      // Bump request id so in-flight loads cannot repopulate state after scopes are cleared.
      entityRequestIdRef.current += 1;
      setEntityLoading(false);
      setSelectedEntity(null);
      setFacets({});
      return;
    }

    const ac = new AbortController();
    const { signal } = ac;
    const currentRequestId = ++entityRequestIdRef.current;
    setEntityLoading(true);

    void (async () => {
      try {
        const entity = await schemaService.getEntityById(entityId, scopeQuery, signal);
        if (signal.aborted || currentRequestId !== entityRequestIdRef.current) {
          return;
        }
        setSelectedEntity(entity);
        if (entity) {
          const nextFacets = await loadFacetsForEntity(entity, scopeQuery, signal);
          if (signal.aborted || currentRequestId !== entityRequestIdRef.current) {
            return;
          }
          setFacets(nextFacets);
        } else {
          setFacets({});
        }
      } catch (e) {
        if (e instanceof DOMException && e.name === 'AbortError') {
          return;
        }
        if (currentRequestId !== entityRequestIdRef.current) {
          return;
        }
        setSelectedEntity(null);
        setFacets({});
      } finally {
        if (!signal.aborted && currentRequestId === entityRequestIdRef.current) {
          setEntityLoading(false);
        }
      }
    })();

    return () => {
      ac.abort();
    };
  }, [entityScopeLoadKey, hasActiveReadScopes, routeEntityId, scopeQuery]);

  useEffect(() => {
    if (!hasActiveReadScopes || treeRef.current === null || !routeEntityId) return;

    let cancelled = false;
    void enrichExplorerTreeColumns(
      treeRef.current,
      scopeQuery,
      routeEntityId,
      selectedEntity,
    ).then((nextTree) => {
      if (cancelled || nextTree === treeRef.current) return;
      setTree(nextTree);
    });

    return () => {
      cancelled = true;
    };
  }, [hasActiveReadScopes, routeEntityId, scopeQuery, selectedEntity]);

  const handleScopeSelectionChange = useCallback((nextReadSlugs: string[]) => {
    setSearchParams((current) => {
      const { declaredScopes: pool } = resolveModelScopeFromSearchParams(current);
      return scopeSearchParamsAfterReadScopeChange(current, pool, nextReadSlugs);
    }, { replace: true });
  }, [setSearchParams]);

  const handleSelect = (node: SchemaNode) => {
    const currentRequestId = ++entityRequestIdRef.current;
    setEntityLoading(true);
    schemaService.getEntityById(node.id, scopeQuery).then((entity) => {
      if (currentRequestId !== entityRequestIdRef.current) return;
      setSelectedEntity(entity);
      if (!entity) {
        setFacets({});
        setEntityLoading(false);
        return;
      }
      void loadFacetsForEntity(entity, scopeQuery).then(setFacets).catch(() => setFacets({}));
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
    const parts = node.id.split('.');
    if (parts.length === 1) {
      navigate({ pathname: `/model/${parts[0]}`, search: modelViewSearch });
    } else if (parts.length === 2) {
      navigate({ pathname: `/model/${parts[0]}/${parts[1]}`, search: modelViewSearch });
    } else if (parts.length >= 3) {
      navigate({
        pathname: `/model/${parts[0]}/${parts[1]}/${parts.slice(2).join('.')}`,
        search: modelViewSearch,
      });
    }
  };

  const scopePicker = (
    <MetadataScopeCheckboxPicker
      urlScopes={declaredScopes}
      activeScopes={readScopes}
      onChange={handleScopeSelectionChange}
    />
  );

  const emptyMainState = (title: string, message: string, showScopePicker: boolean) => (
    <Box
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {showScopePicker ? (
        <Box
          px="md"
          py="xs"
          style={{
            borderBottom: '1px solid var(--mantine-color-default-border)',
            flexShrink: 0,
          }}
        >
          <Group justify="flex-end" wrap="nowrap">
            {scopePicker}
          </Group>
        </Box>
      ) : null}
      <Box
        style={{
          flex: 1,
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
          {title}
        </Text>
        <Text size="sm" c="dimmed" ta="center" maw={400}>
          {message}
        </Text>
      </Box>
    </Box>
  );

  return (
    <ExplorerSplitLayout
      icon={HiOutlineCircleStack}
      title="Schema Browser"
      sidebarBody={
        !hasActiveReadScopes ? (
          <Box p="md">
            <Text size="sm" c="dimmed" ta="center">
              Select one or more metadata scopes to browse the model.
            </Text>
          </Box>
        ) : treeLoading ? (
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
        !hasActiveReadScopes ? (
          emptyMainState(
            'No scopes selected',
            'Enable at least one metadata scope in the picker to view model metadata.',
            true,
          )
        ) : routeEntityId && (entityLoading || !selectedEntity) ? (
          <Box p="md" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
            <Loader size="sm" />
            <Text size="xs" c="dimmed">Loading details...</Text>
          </Box>
        ) : selectedEntity ? (
            <EntityDetails
              entity={selectedEntity}
              facets={facets}
              activeScopes={readScopes}
              declaredScopes={declaredScopes}
              onScopeSelectionChange={handleScopeSelectionChange}
              scopeQuery={scopeQuery}
              onFacetsChanged={async () => {
                const reloaded = await schemaService.getEntityById(selectedEntity.id, scopeQuery);
                if (!reloaded) {
                  setFacets({});
                  return;
                }
                setSelectedEntity(reloaded);
                setFacets(await loadFacetsForEntity(reloaded, scopeQuery));
              }}
            />
        ) : (
          emptyMainState(
            'Data Model Explorer',
            'Select a schema, table, or column from the tree on the left to view its details and metadata.',
            true,
          )
        )
      }
    />
  );
}
