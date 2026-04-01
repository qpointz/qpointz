import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { EntityFacets } from '../../types/schema';

// ---------------------------------------------------------------------------
// Fixture data
// ---------------------------------------------------------------------------

const modelMetadataId = 'urn:mill/metadata/entity:model-entity';

const mockSchemaList = [
  { id: 'model-entity', entityType: 'MODEL', schemaName: '', metadataEntityId: modelMetadataId },
  { id: 'sales', entityType: 'SCHEMA', schemaName: 'sales' },
];

const mockModelDetail = {
  id: 'model-entity',
  entityType: 'MODEL',
  metadataEntityId: modelMetadataId,
};

const mockSchemaDetail = {
  id: 'sales',
  entityType: 'SCHEMA',
  schemaName: 'sales',
  tables: [{ id: 'sales.customers', entityType: 'TABLE', schemaName: 'sales', tableName: 'customers' }],
};

const mockTreePayload = {
  modelRoot: {
    id: 'model-entity',
    entityType: 'MODEL',
    metadataEntityId: modelMetadataId,
  },
  schemas: [mockSchemaDetail],
};

const mockTableDetail = {
  id: 'sales.customers',
  entityType: 'TABLE',
  schemaName: 'sales',
  tableName: 'customers',
  tableType: 'TABLE',
  columns: [
    {
      id: 'sales.customers.customer_id',
      entityType: 'COLUMN',
      schemaName: 'sales',
      tableName: 'customers',
      columnName: 'customer_id',
      fieldIndex: 0,
      type: { type: 'BIG_INT', nullable: false },
    },
  ],
};

const salesCustomersUrn = 'urn:mill/metadata/entity:sales.customers';
const salesCustomerColUrn = 'urn:mill/metadata/entity:sales.customers.customer_id';

/** Matches `GET .../metadata/entities/{id}/facets` — JSON array of facet instances (`facetType`, `payload`, …). */
const mockFacetsCustomers = [
  {
    facetType: 'urn:mill/metadata/facet-type:descriptive',
    payload: { displayName: 'Customers', description: 'Core customer records' },
  },
];

const mockFacetsCustomerId = [
  {
    facetType: 'urn:mill/metadata/facet-type:structural',
    payload: { physicalName: 'customer_id', physicalType: 'INTEGER', isPrimaryKey: true, nullable: false },
  },
];

// ---------------------------------------------------------------------------
// fetch mock
// ---------------------------------------------------------------------------

function makeOkResponse(body: unknown): Response {
  return {
    ok: true,
    status: 200,
    json: () => Promise.resolve(body),
  } as unknown as Response;
}

function makeErrorResponse(status: number): Response {
  return { ok: false, status } as unknown as Response;
}

const fetchMock = vi.fn<typeof fetch>();

beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock);
  fetchMock.mockReset();
});

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('schemaService', () => {
  describe('buildEntityUrn', () => {
    it('should build lowercase dot-separated entity URNs', async () => {
      const { buildEntityUrn } = await import('../schemaService');
      expect(buildEntityUrn('Sales', 'Customers')).toBe('urn:mill/metadata/entity:sales.customers');
      expect(buildEntityUrn('P', 'T', 'C')).toBe('urn:mill/metadata/entity:p.t.c');
    });
  });

  describe('getContext', () => {
    it('falls back to global when context endpoint fails', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(503));
      const { schemaService } = await import('../schemaService');
      const context = await schemaService.getContext();
      expect(context.selectedContext).toBe('global');
      expect(context.availableScopes[0]?.slug).toBe('global');
    });
  });

  describe('listSchemas', () => {
    it('requests list with scope query parameter', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockSchemaList));
      const { schemaService } = await import('../schemaService');
      const schemas = await schemaService.listSchemas('global');
      expect(schemas.length).toBe(2);
      expect(schemas[0]?.entityType).toBe('MODEL');
      expect(fetchMock).toHaveBeenCalledWith('/api/v1/schema?scope=global&facetMode=direct', { credentials: 'include' });
    });
  });

  describe('getTree', () => {
    it('loads tree from schema tree endpoint with model root', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTreePayload));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree('global');
      expect(tree.length).toBe(1);
      expect(tree[0]?.type).toBe('MODEL');
      expect(tree[0]?.children?.length).toBe(1);
      expect(tree[0]?.children?.[0]?.type).toBe('SCHEMA');
      expect(fetchMock).toHaveBeenCalledWith('/api/v1/schema/tree?scope=global&facetMode=none', { credentials: 'include' });
    });
  });

  describe('getEntityById', () => {
    it('returns model root for model-entity id', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockModelDetail));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('model-entity', 'global');
      expect(result?.entityType).toBe('MODEL');
      expect(result?.metadataEntityId).toBe(modelMetadataId);
      expect(fetchMock).toHaveBeenCalledWith('/api/v1/schema/model?scope=global&facetMode=none', { credentials: 'include' });
    });

    it('returns schema for one-part id', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockSchemaDetail));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales', 'global');
      expect(result).not.toBeNull();
      expect(result?.id).toBe('sales');
      expect(result?.entityType).toBe('SCHEMA');
    });

    it('returns table for two-part id', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTableDetail));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales.customers', 'global');
      expect(result).not.toBeNull();
      expect(result?.entityType).toBe('TABLE');
    });

    it('returns column for three-part id', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTableDetail.columns[0]));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales.customers.customer_id', 'global');
      expect(result).not.toBeNull();
      expect(result?.entityType).toBe('COLUMN');
    });

    it('should return null for a non-existent id', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(404));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('nonexistent.table', 'global');
      expect(result).toBeNull();
    });
  });

  describe('getEntityFacets', () => {
    it('should return facets for a known entity', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomers));
      const { schemaService } = await import('../schemaService');
      const facets: EntityFacets = await schemaService.getEntityFacets(salesCustomersUrn, 'global');
      expect(facets).toBeDefined();
      expect(facets.descriptive).toBeDefined();
    });

    it('should return descriptive facet with displayName', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomers));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets(salesCustomersUrn, 'global');
      expect(facets.descriptive?.displayName).toBe('Customers');
    });

    it('should return empty object for unknown entity', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(404));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets('urn:mill/metadata/entity:nonexistent', 'global');
      expect(facets).toBeDefined();
      expect(Object.keys(facets).length).toBe(0);
    });

    it('should include structural facets for attributes with metadata', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomerId));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets(salesCustomerColUrn, 'global');
      expect(facets.structural).toBeDefined();
      expect(facets.structural?.isPrimaryKey).toBe(true);
    });

    it('should accept legacy map-shaped facet response', async () => {
      const legacyMap = {
        'urn:mill/metadata/facet-type:descriptive': {
          facetType: 'urn:mill/metadata/facet-type:descriptive',
          payload: { displayName: 'Legacy', description: '' },
        },
      };
      fetchMock.mockResolvedValueOnce(makeOkResponse(legacyMap));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets(salesCustomersUrn, 'global');
      expect(facets.descriptive?.displayName).toBe('Legacy');
    });
  });

  describe('facetsResolved (WI-134 / WI-136)', () => {
    const iso = '2026-01-01T00:00:00Z';

    it('parses facetsResolved on schema entity GET', async () => {
      const withResolved = {
        ...mockSchemaDetail,
        facetsResolved: [
          {
            uid: 'u1',
            facetTypeUrn: 'urn:mill/metadata/facet-type:descriptive',
            scopeUrn: 'urn:mill:scope:global',
            origin: 'CAPTURED',
            originId: 'repository',
            assignmentUid: 'as1',
            payload: { displayName: 'Captured', description: '' },
            createdAt: iso,
            lastModifiedAt: iso,
          },
          {
            uid: 'u2',
            facetTypeUrn: 'urn:mill/metadata/facet-type:descriptive',
            scopeUrn: 'urn:mill:scope:global',
            origin: 'INFERRED',
            originId: 'logical-layout',
            assignmentUid: null,
            payload: { displayName: 'Inferred title', description: 'from layout' },
            createdAt: iso,
            lastModifiedAt: iso,
          },
        ],
      };
      fetchMock.mockResolvedValueOnce(makeOkResponse(withResolved));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales', 'global');
      expect(result?.facetsResolved?.length).toBe(2);
      expect(result?.facetsResolved?.[1]?.origin).toBe('INFERRED');
      expect(result?.facetsResolved?.[1]?.originId).toBe('logical-layout');
    });

    it('buildEntityFacetsFromResolvedList merges header from captured-first with inferred fallback', async () => {
      const { buildEntityFacetsFromResolvedList } = await import('../schemaService');
      const rows = [
        {
          uid: 'u2',
          facetTypeUrn: 'urn:mill/metadata/facet-type:descriptive',
          scopeUrn: 'urn:mill:scope:global',
          origin: 'INFERRED' as const,
          originId: 'logical-layout',
          assignmentUid: null,
          payload: { displayName: 'Only inferred', description: '' },
        },
        {
          uid: 'u1',
          facetTypeUrn: 'urn:mill/metadata/facet-type:descriptive',
          scopeUrn: 'urn:mill:scope:global',
          origin: 'CAPTURED' as const,
          originId: 'repository',
          assignmentUid: 'as1',
          payload: { displayName: 'Captured wins', description: '' },
        },
      ];
      const ef = buildEntityFacetsFromResolvedList(rows);
      expect(ef.resolvedRows?.length).toBe(2);
      expect(ef.descriptive?.displayName).toBe('Captured wins');
      expect(ef.byType?.['urn:mill/metadata/facet-type:descriptive']).toEqual({ displayName: 'Captured wins', description: '' });
    });

    it('buildEntityFacetsFromResolvedList dedupes identical captured relation rows', async () => {
      const { buildEntityFacetsFromResolvedList } = await import('../schemaService');
      const edge = {
        sourceColumns: ['id'],
        target: { schema: 'skymill', table: 'bookings', columns: ['flight_instance_id'] },
      };
      const rows = [
        {
          uid: 'a',
          facetTypeUrn: 'urn:mill/metadata/facet-type:relation',
          scopeUrn: 'urn:mill:scope:global',
          origin: 'CAPTURED' as const,
          originId: 'repository',
          assignmentUid: 'x1',
          payload: [edge, edge, edge],
        },
        {
          uid: 'b',
          facetTypeUrn: 'urn:mill/metadata/facet-type:relation',
          scopeUrn: 'urn:mill:scope:global',
          origin: 'CAPTURED' as const,
          originId: 'repository',
          assignmentUid: 'x2',
          payload: [edge],
        },
      ];
      const ef = buildEntityFacetsFromResolvedList(rows);
      expect(ef.resolvedRows?.length).toBe(1);
      expect(Array.isArray(ef.resolvedRows?.[0]?.payload)).toBe(true);
      expect((ef.resolvedRows?.[0]?.payload as unknown[]).length).toBe(1);
    });
  });
});
