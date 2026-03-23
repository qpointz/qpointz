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

function extractFacets(raw?: Record<string, { facetType: string; payload: unknown }>): EntityFacets {
  if (!raw) return {};
  const result: EntityFacets = {};
  if (raw[FACET_DESCRIPTIVE]) result.descriptive = raw[FACET_DESCRIPTIVE].payload as DescriptiveFacet;
  if (raw[FACET_STRUCTURAL]) result.structural = raw[FACET_STRUCTURAL].payload as StructuralFacet;
  if (raw[FACET_RELATION]) {
    result.relations = mapRelationPayload(raw[FACET_RELATION].payload);
  }
  return result;
}

async function fetchJsonOrNull<T>(url: string): Promise<T | null> {
  const res = await fetch(url, { credentials: 'include' });
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
  async getSchema(schemaName: string, context: string, facetMode = DEFAULT_FACET_MODE) {
    return fetchJsonOrNull<SchemaDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`
    );
  },
  async getTable(schemaName: string, tableName: string, context: string, facetMode = DEFAULT_FACET_MODE) {
    return fetchJsonOrNull<TableDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`
    );
  },
  async getColumn(schemaName: string, tableName: string, columnName: string, context: string, facetMode = DEFAULT_FACET_MODE) {
    return fetchJsonOrNull<ColumnDetail>(
      `/api/v1/schema/${encodeURIComponent(schemaName)}/tables/${encodeURIComponent(tableName)}/columns/${encodeURIComponent(columnName)}?context=${encodeURIComponent(context)}&facetMode=${encodeURIComponent(facetMode)}`
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
  async getEntityById(id: string, context: string) {
    const parts = id.split('.');
    if (parts.length === 1) {
      return realSchemaService.getSchema(parts[0]!, context, 'none');
    }
    if (parts.length === 2) {
      return realSchemaService.getTable(parts[0]!, parts[1]!, context, 'none');
    }
    return realSchemaService.getColumn(parts[0]!, parts[1]!, parts.slice(2).join('.'), context, 'none');
  },
  async getEntityFacets(id: string, context: string) {
    const parts = id.split('.');
    if (parts.length === 1) {
      const schema = await realSchemaService.getSchema(parts[0]!, context, 'direct');
      return extractFacets((schema as SchemaDetail & { facets?: Record<string, { facetType: string; payload: unknown }> })?.facets);
    }
    if (parts.length === 2) {
      const table = await realSchemaService.getTable(parts[0]!, parts[1]!, context, 'direct');
      return extractFacets((table as TableDetail & { facets?: Record<string, { facetType: string; payload: unknown }> })?.facets);
    }
    const column = await realSchemaService.getColumn(parts[0]!, parts[1]!, parts.slice(2).join('.'), context, 'direct');
    return extractFacets((column as ColumnDetail & { facets?: Record<string, { facetType: string; payload: unknown }> })?.facets);
  },
};

export const schemaService: SchemaService = realSchemaService;
