import type {
  ColumnDetail,
  DescriptiveFacet,
  EntityFacets,
  FacetResolvedRow,
  RelationFacet,
  SchemaContext,
  SchemaDetail,
  SchemaEntity,
  SchemaListItem,
  SchemaNode,
  SchemaService,
  StructuralFacet,
  TableDetail,
} from '../types/schema';
import { facetTypePathSegment, metadataEntityPathSegment } from '../utils/urnSlug';

const FACET_DESCRIPTIVE = 'urn:mill/metadata/facet-type:descriptive';
const FACET_STRUCTURAL  = 'urn:mill/metadata/facet-type:structural';
const FACET_RELATION    = 'urn:mill/metadata/facet-type:relation';
const CONTEXT_GLOBAL    = 'global';
const DEFAULT_FACET_MODE = 'direct';
/** Path segment id for the catalog model root; matches [io.qpointz.mill.data.schema.SchemaModelRoot.ENTITY_LOCAL_ID]. */
const MODEL_ROOT_LOCAL_ID = 'model-entity';

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
 * Builds the canonical metadata entity URN from relational coordinates (SPEC §13).
 *
 * @param schema catalog schema name
 * @param table optional table name
 * @param column optional column name
 * @returns `urn:mill/metadata/entity:…` with lowercase dot-separated local part
 */
export function buildEntityUrn(schema: string, table?: string, column?: string): string {
  const parts = [schema, table, column].filter(Boolean).map((s) => s!.toLowerCase());
  return `urn:mill/metadata/entity:${parts.join('.')}`;
}

/**
 * URN to pass to metadata facet REST calls: uses `metadataEntityId` from the schema API when set, otherwise derives from entity coordinates.
 *
 * @param entity loaded schema explorer entity
 * @returns full `urn:mill/…` id, or `null` if coordinates are insufficient
 */
export function metadataEntityUrnForFacetApi(entity: SchemaEntity): string | null {
  const fromApi = entity.metadataEntityId?.trim();
  if (fromApi) return fromApi;
  if (entity.entityType === 'SCHEMA' && entity.schemaName) {
    return buildEntityUrn(entity.schemaName);
  }
  if (entity.entityType === 'TABLE' && entity.schemaName && entity.tableName) {
    return buildEntityUrn(entity.schemaName, entity.tableName);
  }
  if (entity.entityType === 'COLUMN' && entity.schemaName && entity.tableName && entity.columnName) {
    return buildEntityUrn(entity.schemaName, entity.tableName, entity.columnName);
  }
  if (entity.entityType === 'MODEL' && entity.metadataEntityId) {
    return entity.metadataEntityId;
  }
  return null;
}

function facetTypeKeyFromEntry(e: Record<string, unknown>): string | undefined {
  const urn = e.facetTypeUrn;
  const legacy = e.facetType;
  if (typeof urn === 'string' && urn.length > 0) return urn;
  if (typeof legacy === 'string' && legacy.length > 0) return legacy;
  return undefined;
}

/**
 * Normalizes `GET /api/v1/metadata/entities/{id}/facets` JSON to a list of `{ facetType, payload, uid? }`.
 * Supports `facetTypeUrn` (current) and legacy `facetType`, plus the **map** shape for transitional callers.
 */
function normalizeFacetEntries(raw: unknown): { facetType: string; payload: unknown; uid?: string }[] {
  if (raw == null) return [];
  if (Array.isArray(raw)) {
    const out: { facetType: string; payload: unknown; uid?: string }[] = [];
    for (const e of raw) {
      if (e == null || typeof e !== 'object') continue;
      const rec = e as Record<string, unknown>;
      const ft = facetTypeKeyFromEntry(rec);
      if (ft == null) continue;
      out.push({
        facetType: ft,
        payload: rec.payload,
        uid: typeof rec.uid === 'string' && rec.uid.length > 0 ? rec.uid : undefined,
      });
    }
    return out;
  }
  if (typeof raw === 'object') {
    return Object.entries(raw as Record<string, Record<string, unknown>>).map(([k, v]) => {
      const ft = v && typeof v === 'object' ? facetTypeKeyFromEntry(v) : undefined;
      return {
        facetType: ft ?? k,
        payload: v?.payload,
        uid: typeof v?.uid === 'string' && v.uid.length > 0 ? v.uid : undefined,
      };
    });
  }
  return [];
}

/**
 * Parses `facetsResolved` arrays from schema explorer JSON (`FacetResolvedRowDto`).
 *
 * @param raw JSON value of `facetsResolved`
 * @returns parsed rows, or `undefined` when the payload is not an array
 */
export function parseFacetResolvedRows(raw: unknown): FacetResolvedRow[] | undefined {
  if (raw == null || !Array.isArray(raw)) return undefined;
  const out: FacetResolvedRow[] = [];
  for (const e of raw) {
    if (e == null || typeof e !== 'object') continue;
    const r = e as Record<string, unknown>;
    const uid = typeof r.uid === 'string' && r.uid.length > 0 ? r.uid : String(r.uid ?? '');
    const facetTypeUrn =
      (typeof r.facetTypeUrn === 'string' && r.facetTypeUrn.length > 0
        ? r.facetTypeUrn
        : typeof r.facetType === 'string'
          ? r.facetType
          : '') || '';
    if (!facetTypeUrn) continue;
    const scopeUrn = typeof r.scopeUrn === 'string' ? r.scopeUrn : String(r.scopeUrn ?? '');
    const origin: FacetResolvedRow['origin'] = r.origin === 'INFERRED' ? 'INFERRED' : 'CAPTURED';
    const originId = typeof r.originId === 'string' ? r.originId : String(r.originId ?? '');
    const payloadRaw = r.payload;
    const payload: Record<string, unknown> =
      payloadRaw != null && typeof payloadRaw === 'object' && !Array.isArray(payloadRaw)
        ? (payloadRaw as Record<string, unknown>)
        : {};
    const assignmentUid =
      r.assignmentUid === null || r.assignmentUid === undefined
        ? null
        : typeof r.assignmentUid === 'string'
          ? r.assignmentUid
          : String(r.assignmentUid);
    const createdAt = typeof r.createdAt === 'string' ? r.createdAt : undefined;
    const lastModifiedAt = typeof r.lastModifiedAt === 'string' ? r.lastModifiedAt : undefined;
    out.push({
      uid,
      facetTypeUrn,
      scopeUrn,
      origin,
      originId,
      assignmentUid,
      payload,
      createdAt,
      lastModifiedAt,
    });
  }
  return out;
}

/**
 * Attaches parsed {@link FacetResolvedRow} lists to schema explorer DTOs (recurses into `tables` / `columns`).
 */
function attachFacetsResolvedToExplorerDto<T extends Record<string, unknown>>(dto: T): T {
  let next: Record<string, unknown> = { ...dto };
  if ('facetsResolved' in next && next.facetsResolved != null) {
    const parsed = parseFacetResolvedRows(next.facetsResolved);
    if (parsed) next = { ...next, facetsResolved: parsed };
  }
  if (Array.isArray(next.tables)) {
    next = {
      ...next,
      tables: (next.tables as unknown[]).map((t) =>
        t && typeof t === 'object' ? attachFacetsResolvedToExplorerDto(t as Record<string, unknown>) : t
      ),
    };
  }
  if (Array.isArray(next.columns)) {
    next = {
      ...next,
      columns: (next.columns as unknown[]).map((c) =>
        c && typeof c === 'object' ? attachFacetsResolvedToExplorerDto(c as Record<string, unknown>) : c
      ),
    };
  }
  return next as T;
}

/**
 * Builds {@link EntityFacets} from a resolved constellation list (captured + inferred).
 * Edit/delete metadata calls use only {@link EntityFacets.byType} / `instanceUidsByType` from **captured** rows;
 * header shortcuts merge inferred + captured (prefer captured when both exist for a well-known facet).
 *
 * @param rows merged `facetsResolved` rows for one entity
 */
export function buildEntityFacetsFromResolvedList(rows: FacetResolvedRow[]): EntityFacets {
  const capturedEntries = rows
    .filter((r) => r.origin === 'CAPTURED')
    .map((r) => ({
      facetType: r.facetTypeUrn,
      payload: r.payload,
      uid: r.assignmentUid && r.assignmentUid.length > 0 ? r.assignmentUid : r.uid,
    }));
  const base = extractFacets(capturedEntries);

  const descriptiveRow =
    rows.find((r) => r.facetTypeUrn === FACET_DESCRIPTIVE && r.origin === 'CAPTURED') ??
    rows.find((r) => r.facetTypeUrn === FACET_DESCRIPTIVE);
  const structuralRow =
    rows.find((r) => r.facetTypeUrn === FACET_STRUCTURAL && r.origin === 'CAPTURED') ??
    rows.find((r) => r.facetTypeUrn === FACET_STRUCTURAL);

  const relationRows = rows.filter((r) => r.facetTypeUrn === FACET_RELATION);
  let relations: RelationFacet[] = [];
  for (const r of relationRows) {
    relations = relations.concat(mapRelationPayload(r.payload));
  }

  return {
    ...base,
    descriptive: descriptiveRow?.payload as DescriptiveFacet | undefined,
    structural: structuralRow?.payload as StructuralFacet | undefined,
    relations: relations.length > 0 ? relations : undefined,
    resolvedRows: rows,
  };
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

/** Fetches a schema explorer JSON document and normalizes `facetsResolved` when present. */
async function fetchExplorerEntityJsonOrNull(
  url: string,
  signal?: AbortSignal
): Promise<Record<string, unknown> | null> {
  const raw = await fetchJsonOrNull<Record<string, unknown>>(url, signal);
  if (!raw) return null;
  return attachFacetsResolvedToExplorerDto(raw);
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
      `/api/v1/schema?scope=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      {
      credentials: 'include',
    });
    if (!res.ok) return [];
    return res.json() as Promise<SchemaListItem[]>;
  },
  async getSchema(schemaName: string, context: string, facetMode = DEFAULT_FACET_MODE, signal?: AbortSignal) {
    const raw = await fetchExplorerEntityJsonOrNull(
      `/api/v1/schema/${encodeURIComponent(schemaName)}?scope=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
    return raw as SchemaDetail | null;
  },
  async getTable(schemaName: string, tableName: string, context: string, facetMode = DEFAULT_FACET_MODE, signal?: AbortSignal) {
    const raw = await fetchExplorerEntityJsonOrNull(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}?scope=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
    return raw as TableDetail | null;
  },
  async getColumn(
    schemaName: string,
    tableName: string,
    columnName: string,
    context: string,
    facetMode = DEFAULT_FACET_MODE,
    signal?: AbortSignal
  ) {
    const raw = await fetchExplorerEntityJsonOrNull(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}/columns/${encodeURIComponent(columnName)}?scope=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`,
      signal
    );
    return raw as ColumnDetail | null;
  },
  async getTree(context: string) {
    const res = await fetch(
      `/api/v1/schema/tree?scope=${encodeURIComponent(context)}&facetMode=none`,
      { credentials: 'include' }
    );
    if (!res.ok) return [];
    const data = (await res.json()) as {
      modelRoot: { id: string; metadataEntityId: string; entityType: string };
      schemas: Array<{
        id: string;
        schemaName: string;
        tables: Array<{ id: string; tableName: string }>;
      }>;
    };
    const schemaNodes: SchemaNode[] = data.schemas.map((schema) => ({
      id: schema.id,
      type: 'SCHEMA',
      name: schema.schemaName,
      children: schema.tables.map((table) => ({
        id: table.id,
        type: 'TABLE',
        name: table.tableName,
        children: [],
      })),
    }));
    return [
      {
        id: data.modelRoot.id,
        type: 'MODEL',
        name: 'Model',
        children: schemaNodes,
      },
    ];
  },
  async getEntityById(id: string, context: string, signal?: AbortSignal) {
    if (id === MODEL_ROOT_LOCAL_ID) {
      const raw = await fetchExplorerEntityJsonOrNull(
        `/api/v1/schema/model?scope=${encodeURIComponent(context)}&facetMode=none`,
        signal
      );
      if (!raw) return null;
      const base = {
        id: String(raw.id ?? MODEL_ROOT_LOCAL_ID),
        entityType: 'MODEL' as const,
        schemaName: '' as const,
        metadataEntityId: String(raw.metadataEntityId ?? ''),
      };
      const fr = raw.facetsResolved;
      if (fr != null && Array.isArray(fr)) {
        return { ...base, facetsResolved: fr as FacetResolvedRow[] };
      }
      return base;
    }
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
        `/api/v1/metadata/entities/${metadataEntityPathSegment(id)}/facets?scope=${encodeURIComponent(context)}`,
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
    const res = await fetch(
      `/api/v1/metadata/entities/${metadataEntityPathSegment(id)}/facets/${facetTypePathSegment(facetType)}?scope=${encodeURIComponent(context)}`,
      {
        method: 'POST',
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
  async patchEntityFacetPayload(id: string, facetType: string, facetUid: string, payload: unknown) {
    const res = await fetch(
      `/api/v1/metadata/entities/${metadataEntityPathSegment(id)}/facets/${facetTypePathSegment(facetType)}/${encodeURIComponent(facetUid)}`,
      {
        method: 'PATCH',
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
    const base = `/api/v1/metadata/entities/${metadataEntityPathSegment(id)}/facets/${facetTypePathSegment(facetType)}`;
    const url =
      instanceUid != null && instanceUid !== ''
        ? `${base}/${encodeURIComponent(instanceUid)}`
        : `${base}?scope=${encodeURIComponent(context)}`;
    const res = await fetch(url, { method: 'DELETE', credentials: 'include' });
    if (!res.ok) {
      const body = await res.text();
      throw new Error(body || `${res.status} ${res.statusText}`);
    }
  },
};

export const schemaService: SchemaService = realSchemaService;
