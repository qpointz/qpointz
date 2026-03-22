import type { DescriptiveFacet, EntityFacets, RelationFacet, SchemaEntity, SchemaService, StructuralFacet } from '../types/schema';

const FACET_DESCRIPTIVE = 'urn:mill/metadata/facet-type:descriptive';
const FACET_STRUCTURAL  = 'urn:mill/metadata/facet-type:structural';
const FACET_RELATION    = 'urn:mill/metadata/facet-type:relation';
const CONTEXT_GLOBAL    = 'global';

async function realGetEntityFacets(id: string): Promise<EntityFacets> {
  const url = `/api/v1/metadata/entities/${encodeURIComponent(id)}/facets?context=${CONTEXT_GLOBAL}`;
  const res = await fetch(url, { credentials: 'include' });
  if (!res.ok) return {};
  const map = await res.json() as Record<string, { facetType: string; payload: unknown }>;

  const result: EntityFacets = {};
  if (map[FACET_DESCRIPTIVE]) result.descriptive = map[FACET_DESCRIPTIVE].payload as DescriptiveFacet;
  if (map[FACET_STRUCTURAL])  result.structural  = map[FACET_STRUCTURAL].payload  as StructuralFacet;
  if (map[FACET_RELATION])    result.relations   = map[FACET_RELATION].payload    as RelationFacet[];
  return result;
}

const realSchemaService: SchemaService = {
  async getTree() {
    const res = await fetch('/api/v1/schema/tree', { credentials: 'include' });
    if (!res.ok) return [];
    return res.json() as Promise<SchemaEntity[]>;
  },
  async getEntityById(id: string) {
    const res = await fetch(`/api/v1/schema/entities/${encodeURIComponent(id)}`, {
      credentials: 'include',
    });
    if (res.status === 404) return null;
    if (!res.ok) return null;
    return res.json() as Promise<SchemaEntity>;
  },
  async getEntityFacets(id: string) {
    return realGetEntityFacets(id);
  },
};

export const schemaService: SchemaService = realSchemaService;
