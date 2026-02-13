import { describe, it, expect } from 'vitest';
import { schemaService } from '../schemaService';

describe('schemaService', () => {
  describe('getTree', () => {
    it('should return a non-empty array of schema entities', async () => {
      const tree = await schemaService.getTree();
      expect(Array.isArray(tree)).toBe(true);
      expect(tree.length).toBeGreaterThan(0);
    });

    it('every root entity should be of type SCHEMA', async () => {
      const tree = await schemaService.getTree();
      for (const entity of tree) {
        expect(entity.type).toBe('SCHEMA');
      }
    });

    it('each schema entity should have id, type, and name', async () => {
      const tree = await schemaService.getTree();
      for (const entity of tree) {
        expect(entity.id).toBeDefined();
        expect(entity.name).toBeDefined();
        expect(entity.type).toBeDefined();
      }
    });

    it('schemas should contain children (tables)', async () => {
      const tree = await schemaService.getTree();
      const withChildren = tree.filter((e) => e.children && e.children.length > 0);
      expect(withChildren.length).toBeGreaterThan(0);
    });
  });

  describe('getEntityById', () => {
    it('should return the entity for a valid id', async () => {
      const entity = await schemaService.getEntityById('sales');
      expect(entity).not.toBeNull();
      expect(entity?.id).toBe('sales');
      expect(entity?.type).toBe('SCHEMA');
    });

    it('should find nested entities (tables)', async () => {
      const entity = await schemaService.getEntityById('sales.customers');
      expect(entity).not.toBeNull();
      expect(entity?.type).toBe('TABLE');
    });

    it('should find deeply nested entities (attributes)', async () => {
      const entity = await schemaService.getEntityById('sales.customers.email');
      expect(entity).not.toBeNull();
      expect(entity?.type).toBe('ATTRIBUTE');
    });

    it('should return null for a non-existent id', async () => {
      const entity = await schemaService.getEntityById('nonexistent.table');
      expect(entity).toBeNull();
    });
  });

  describe('getEntityFacets', () => {
    it('should return facets for a known entity', async () => {
      const facets = await schemaService.getEntityFacets('sales.customers');
      expect(facets).toBeDefined();
      expect(facets.descriptive).toBeDefined();
    });

    it('should return descriptive facet with displayName', async () => {
      const facets = await schemaService.getEntityFacets('sales.customers');
      expect(facets.descriptive?.displayName).toBe('Customers');
    });

    it('should return empty object for unknown entity', async () => {
      const facets = await schemaService.getEntityFacets('nonexistent');
      expect(facets).toBeDefined();
      expect(Object.keys(facets).length).toBe(0);
    });

    it('should include structural facets for attributes with metadata', async () => {
      const facets = await schemaService.getEntityFacets('sales.customers.customer_id');
      expect(facets.structural).toBeDefined();
      expect(facets.structural?.isPrimaryKey).toBe(true);
    });
  });
});
