import { describe, it, expect } from 'vitest';
import {
  mockSchemaTree,
  mockFacets,
  getEntityFacets,
  findEntityById,
} from '../mockSchema';

describe('mockSchemaTree', () => {
  it('should contain top-level schemas', () => {
    expect(mockSchemaTree.length).toBeGreaterThan(0);
    for (const schema of mockSchemaTree) {
      expect(schema.type).toBe('SCHEMA');
    }
  });

  it('should have a sales schema with children', () => {
    const sales = mockSchemaTree.find((s) => s.id === 'sales');
    expect(sales).toBeDefined();
    expect(sales!.children).toBeDefined();
    expect(sales!.children!.length).toBeGreaterThan(0);
  });

  it('should have proper entity hierarchy (SCHEMA > TABLE > ATTRIBUTE)', () => {
    const sales = mockSchemaTree.find((s) => s.id === 'sales')!;
    expect(sales.type).toBe('SCHEMA');

    const customers = sales.children!.find((t) => t.id === 'sales.customers');
    expect(customers).toBeDefined();
    expect(customers!.type).toBe('TABLE');

    if (customers!.children && customers!.children.length > 0) {
      for (const col of customers!.children) {
        expect(col.type).toBe('ATTRIBUTE');
      }
    }
  });
});

describe('findEntityById', () => {
  it('should find a schema by ID', () => {
    const entity = findEntityById('sales');
    expect(entity).toBeDefined();
    expect(entity!.type).toBe('SCHEMA');
    expect(entity!.name).toBe('sales');
  });

  it('should find a table by ID', () => {
    const entity = findEntityById('sales.customers');
    expect(entity).toBeDefined();
    expect(entity!.type).toBe('TABLE');
    expect(entity!.name).toBe('customers');
  });

  it('should find an attribute by ID', () => {
    const entity = findEntityById('sales.customers.customer_id');
    expect(entity).toBeDefined();
    expect(entity!.type).toBe('ATTRIBUTE');
    expect(entity!.name).toBe('customer_id');
  });

  it('should return null for unknown entity', () => {
    expect(findEntityById('nonexistent.table.column')).toBeNull();
  });

  it('should return null for empty string', () => {
    expect(findEntityById('')).toBeNull();
  });
});

describe('getEntityFacets', () => {
  it('should return facets for a known entity', () => {
    const facets = getEntityFacets('sales.customers.customer_id');
    expect(facets).toBeDefined();
  });

  it('should return empty object for unknown entity', () => {
    const facets = getEntityFacets('nonexistent');
    expect(facets).toBeDefined();
    // Should return an empty object or default
    expect(typeof facets).toBe('object');
  });
});

describe('mockFacets', () => {
  it('should have facet entries keyed by entity ID', () => {
    const keys = Object.keys(mockFacets);
    expect(keys.length).toBeGreaterThan(0);
  });

  it('facets should have at least one facet type per entry', () => {
    for (const [, facets] of Object.entries(mockFacets)) {
      const hasSomeFacet =
        facets.descriptive !== undefined ||
        facets.structural !== undefined ||
        facets.relations !== undefined;
      expect(hasSomeFacet).toBe(true);
    }
  });
});
