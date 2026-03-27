import type {
  ColumnDetail,
  DescriptiveFacet,
  EntityFacets,
  RelationFacet,
  SchemaContext,
  SchemaDetail,
  SchemaListItem,
  SchemaNode,
  SchemaService,
  StructuralFacet,
  TableDetail,
} from '../types/schema';

const FACET_DESCRIPTIVE = 'urn:mill/metadata/facet-type:descriptive';
const FACET_STRUCTURAL  = 'urn:mill/metadata/facet-type:structural';
const FACET_RELATION    = 'urn:mill/metadata/facet-type:relation';
const CONTEXT_GLOBAL    = 'global';
const DEFAULT_FACET_MODE = 'direct';

function mapCardinality(raw: unknown): RelationFacet['cardinality'] {
  const value = String(raw ?? '').toUpperCase();
  switch (value) {
    case 'ONE_TO_ONE':
    case '1:1':
      return '1:1';
    case 'ONE_TO_MANY':
    case '1:N':
      return '1:N';
    case 'MANY_TO_ONE':
    case 'N:1':
      return 'N:1';
    case 'MANY_TO_MANY':
    case 'N:N':
      return 'N:N';
    default:
      return 'N:N';
  }
}

function mapRelationPayload(payload: unknown): RelationFacet[] {
  const rows = Array.isArray(payload)
    ? payload
    : payload && typeof payload === 'object' && Array.isArray((payload as { relations?: unknown[] }).relations)
      ? (payload as { relations: unknown[] }).relations
      : [];

  return rows
    .map((row, idx): RelationFacet | null => {
      if (!row || typeof row !== 'object') return null;
      const data = row as Record<string, unknown>;
      const sourceTable = data.sourceTable as { fqn?: string } | undefined;
      const targetTable = data.targetTable as { fqn?: string } | undefined;
      const sourceEntity = sourceTable?.fqn ?? String(data.sourceEntity ?? '');
      const targetEntity = targetTable?.fqn ?? String(data.targetEntity ?? '');
      return {
        id: String(data.id ?? data.name ?? `relation-${idx}`),
        name: String(data.name ?? `Relation ${idx + 1}`),
        sourceEntity,
        targetEntity,
        cardinality: mapCardinality(data.cardinality),
        relationType: String(data.type ?? data.relationType ?? 'LOGICAL').toUpperCase() as RelationFacet['relationType'],
        description: data.description ? String(data.description) : undefined,
      };
    })
    .filter((item): item is RelationFacet => item !== null);
}

/**
 * Normalizes `GET /api/v1/metadata/entities/{id}/facets` JSON to a list of `{ facetType, payload, uid? }`.
 * Supports the current **array** shape and legacy **map** shape for transitional callers.
 */
function normalizeFacetEntries(raw: unknown): { facetType: string; payload: unknown; uid?: string }[] {
  if (raw == null) return [];
  if (Array.isArray(raw)) {
    return raw
      .filter((e): e is { facetType: string; payload?: unknown; uid?: unknown } =>
        e != null && typeof e === 'object' && 'facetType' in e
      )
      .map((e) => ({
        facetType: String(e.facetType),
        payload: e.payload,
        uid: typeof e.uid === 'string' && e.uid.length > 0 ? e.uid : undefined,
      }));
  }
  if (typeof raw === 'object') {
    return Object.entries(raw as Record<string, { facetType?: string; payload?: unknown; uid?: unknown }>).map(
      ([k, v]) => ({
        facetType: v?.facetType ?? k,
        payload: v?.payload,
        uid: typeof v?.uid === 'string' && v.uid.length > 0 ? v.uid : undefined,
      })
    );
  }
  return [];
}

function extractFacets(raw?: unknown): EntityFacets {
  const entries = normalizeFacetEntries(raw);
  if (entries.length === 0) return {};
  const result: EntityFacets = {};
  result.byType = {};
  const instanceUidsByType: Record<string, (string | undefined)[]> = {};
  for (const { facetType, payload, uid } of entries) {
    if (!facetType) continue;
    if (result.byType[facetType] === undefined) {
      result.byType[facetType] = payload;
      instanceUidsByType[facetType] = [uid];
      continue;
    }
    const existing = result.byType[facetType];
    if (!Array.isArray(existing)) {
      result.byType[facetType] = [existing, payload];
      instanceUidsByType[facetType] = [instanceUidsByType[facetType]![0], uid];
    } else {
      (existing as unknown[]).push(payload);
      instanceUidsByType[facetType]!.push(uid);
    }
  }
  if (Object.keys(instanceUidsByType).length > 0) {
    result.instanceUidsByType = instanceUidsByType;
  }
  const byType = result.byType;
  if (byType[FACET_DESCRIPTIVE]) result.descriptive = byType[FACET_DESCRIPTIVE] as DescriptiveFacet;
  if (byType[FACET_STRUCTURAL]) result.structural = byType[FACET_STRUCTURAL] as StructuralFacet;
  if (byType[FACET_RELATION]) {
    result.relations = mapRelationPayload(byType[FACET_RELATION]);
  }
  return result;
}

async function fetchJsonOrNull<T>(url: string, signal?: AbortSignal): Promise<T | null> {
  const res = await fetch(url, { credentials: 'include', signal });
  if (res.status === 404) return null;
  if (!res.ok) return null;
  return res.json() as Promise<T>;
}

const realSchemaService: SchemaService = {
  async getContext() {
    const res = await fetch('/api/v1/schema/context', { credentials: 'include' });
    if (!res.ok) {
      return {
        selectedContext: CONTEXT_GLOBAL,
        availableScopes: [{ id: CONTEXT_GLOBAL, slug: CONTEXT_GLOBAL, displayName: 'Global' }],
      };
    }
    return res.json() as Promise<SchemaContext>;
  },
  async listSchemas(context: string, facetMode = DEFAULT_FACET_MODE) {
    const res = await fetch(
      `/api/v1/schema?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      {
      credentials: 'include',
    });
    if (!res.ok) return [];
    return res.json() as Promise<SchemaListItem[]>;
  },
  async getSchema(schemaName: string, context: string, facetMode = DEFAULT_FACET_MODE, signal?: AbortSignal) {
    return fetchJsonOrNull<SchemaDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
  },
  async getTable(schemaName: string, tableName: string, context: string, facetMode = DEFAULT_FACET_MODE, signal?: AbortSignal) {
    return fetchJsonOrNull<TableDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
  },
  async getColumn(
    schemaName: string,
    tableName: string,
    columnName: string,
    context: string,
    facetMode = DEFAULT_FACET_MODE,
    signal?: AbortSignal
  ) {
    return fetchJsonOrNull<ColumnDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}/columns/${encodeURIComponent(columnName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
  },
  async getTree(context: string) {
    const schemas = await realSchemaService.listSchemas(context, 'none');
    const schemaDetails = await Promise.all(
      schemas.map((schema) => realSchemaService.getSchema(schema.schemaName, context, 'none'))
    );
    return schemaDetails
      .filter((schema): schema is SchemaDetail => schema !== null)
      .map((schema) => ({
        id: schema.id,
        type: 'SCHEMA',
        name: schema.schemaName,
        children: schema.tables.map((table) => ({
          id: table.id,
          type: 'TABLE',
          name: table.tableName,
          children: [],
        })),
      })) as SchemaNode[];
  },
  async getEntityById(id: string, context: string, signal?: AbortSignal) {
    const parts = id.split('.');
    if (parts.length === 1) {
      return realSchemaService.getSchema(parts[0]!, context, 'none', signal);
    }
    if (parts.length === 2) {
      return realSchemaService.getTable(parts[0]!, parts[1]!, context, 'none', signal);
    }
    return realSchemaService.getColumn(parts[0]!, parts[1]!, parts.slice(2).join('.'), context, 'none', signal);
  },
  async getEntityFacets(id: string, context: string, signal?: AbortSignal) {
    try {
      const res = await fetch(
        `/api/v1/metadata/entities/${encodeURIComponent(id)}/facets?context=${encodeURIComponent(context)}`,
        { credentials: 'include', signal }
      );
      if (res.status === 404 || !res.ok) return {};
      const raw = await res.json();
      return extractFacets(raw);
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') return {};
      throw e;
    }
  },
  async setEntityFacet(id: string, facetType: string, context: string, payload: unknown) {
    const facetPrefix = 'urn:mill/metadata/facet-type:';
    const pathType = facetType.startsWith(facetPrefix) ? facetType.slice(facetPrefix.length) : facetType;
    const res = await fetch(
      `/api/v1/metadata/entities/${encodeURIComponent(id)}/facets/${encodeURIComponent(pathType)}?context=${encodeURIComponent(context)}`,
      {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      }
    );
    if (!res.ok) {
      const body = await res.text();
      throw new Error(body || `${res.status} ${res.statusText}`);
    }
  },
  async deleteEntityFacet(id: string, facetType: string, context: string, instanceUid?: string) {
    const facetPrefix = 'urn:mill/metadata/facet-type:';
    const pathType = facetType.startsWith(facetPrefix) ? facetType.slice(facetPrefix.length) : facetType;
    const uidPart =
      instanceUid != null && instanceUid !== ''
        ? `&uid=${encodeURIComponent(instanceUid)}`
        : '';
    const res = await fetch(
      `/api/v1/metadata/entities/${encodeURIComponent(id)}/facets/${encodeURIComponent(pathType)}?context=${encodeURIComponent(context)}${uidPart}`,
      { method: 'DELETE', credentials: 'include' }
    );
    if (!res.ok) {
      const body = await res.text();
      throw new Error(body || `${res.status} ${res.statusText}`);
    }
  },
};

export const schemaService: SchemaService = realSchemaService;
