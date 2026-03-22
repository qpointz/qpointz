import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { SchemaEntity, EntityFacets } from '../../types/schema';

// ---------------------------------------------------------------------------
// Fixture data
// ---------------------------------------------------------------------------

const emailAttr: SchemaEntity       = { id: 'sales.customers.email',       type: 'ATTRIBUTE', name: 'email' };
const customerIdAttr: SchemaEntity  = { id: 'sales.customers.customer_id', type: 'ATTRIBUTE', name: 'customer_id' };
const customersTable: SchemaEntity  = { id: 'sales.customers', type: 'TABLE',  name: 'customers',  children: [emailAttr, customerIdAttr] };
const salesSchema: SchemaEntity     = { id: 'sales',           type: 'SCHEMA', name: 'sales',      children: [customersTable] };

const mockTree: SchemaEntity[] = [salesSchema];

const mockFacetsCustomers: Record<string, { facetType: string; payload: unknown }> = {
  'urn:mill/metadata/facet-type:descriptive': {
    facetType: 'urn:mill/metadata/facet-type:descriptive',
    payload: { displayName: 'Customers', description: 'Core customer records' },
  },
};

const mockFacetsCustomerId: Record<string, { facetType: string; payload: unknown }> = {
  'urn:mill/metadata/facet-type:structural': {
    facetType: 'urn:mill/metadata/facet-type:structural',
    payload: { physicalName: 'customer_id', physicalType: 'INTEGER', isPrimaryKey: true, nullable: false },
  },
};

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
  describe('getTree', () => {
    it('should return a non-empty array of schema entities', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTree));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree();
      expect(Array.isArray(tree)).toBe(true);
      expect(tree.length).toBeGreaterThan(0);
    });

    it('every root entity should be of type SCHEMA', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTree));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree();
      for (const entity of tree) {
        expect(entity.type).toBe('SCHEMA');
      }
    });

    it('each schema entity should have id, type, and name', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTree));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree();
      for (const entity of tree) {
        expect(entity.id).toBeDefined();
        expect(entity.name).toBeDefined();
        expect(entity.type).toBeDefined();
      }
    });

    it('schemas should contain children (tables)', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockTree));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree();
      const withChildren = tree.filter((e) => e.children && e.children.length > 0);
      expect(withChildren.length).toBeGreaterThan(0);
    });

    it('should return empty array when backend is not ok', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(503));
      const { schemaService } = await import('../schemaService');
      const tree = await schemaService.getTree();
      expect(tree).toEqual([]);
    });
  });

  describe('getEntityById', () => {
    it('should return the entity for a valid id', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(salesSchema));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales');
      expect(result).not.toBeNull();
      expect(result?.id).toBe('sales');
      expect(result?.type).toBe('SCHEMA');
    });

    it('should find nested entities (tables)', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(customersTable));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales.customers');
      expect(result).not.toBeNull();
      expect(result?.type).toBe('TABLE');
    });

    it('should find deeply nested entities (attributes)', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(emailAttr));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('sales.customers.email');
      expect(result).not.toBeNull();
      expect(result?.type).toBe('ATTRIBUTE');
    });

    it('should return null for a non-existent id', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(404));
      const { schemaService } = await import('../schemaService');
      const result = await schemaService.getEntityById('nonexistent.table');
      expect(result).toBeNull();
    });
  });

  describe('getEntityFacets', () => {
    it('should return facets for a known entity', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomers));
      const { schemaService } = await import('../schemaService');
      const facets: EntityFacets = await schemaService.getEntityFacets('sales.customers');
      expect(facets).toBeDefined();
      expect(facets.descriptive).toBeDefined();
    });

    it('should return descriptive facet with displayName', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomers));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets('sales.customers');
      expect(facets.descriptive?.displayName).toBe('Customers');
    });

    it('should return empty object for unknown entity', async () => {
      fetchMock.mockResolvedValueOnce(makeErrorResponse(404));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets('nonexistent');
      expect(facets).toBeDefined();
      expect(Object.keys(facets).length).toBe(0);
    });

    it('should include structural facets for attributes with metadata', async () => {
      fetchMock.mockResolvedValueOnce(makeOkResponse(mockFacetsCustomerId));
      const { schemaService } = await import('../schemaService');
      const facets = await schemaService.getEntityFacets('sales.customers.customer_id');
      expect(facets.structural).toBeDefined();
      expect(facets.structural?.isPrimaryKey).toBe(true);
    });
  });
});
