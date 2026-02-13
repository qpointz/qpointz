import { describe, it, expect } from 'vitest';
import { queryService } from '../queryService';

describe('queryService', () => {
  describe('executeQuery', () => {
    it('should return a result with columns and rows for a valid query', async () => {
      const result = await queryService.executeQuery('SELECT * FROM sales.customers LIMIT 10');
      expect(result.columns).toBeDefined();
      expect(Array.isArray(result.columns)).toBe(true);
      expect(result.rows).toBeDefined();
      expect(Array.isArray(result.rows)).toBe(true);
    });

    it('should include executionTimeMs in the result', async () => {
      const result = await queryService.executeQuery('SELECT * FROM sales.orders');
      expect(result.executionTimeMs).toBeDefined();
      expect(typeof result.executionTimeMs).toBe('number');
      expect(result.executionTimeMs).toBeGreaterThan(0);
    });

    it('should throw for empty query', async () => {
      await expect(queryService.executeQuery('')).rejects.toThrow('Empty query');
    });

    it('should throw for short non-SELECT queries', async () => {
      await expect(queryService.executeQuery('DROP')).rejects.toThrow(/Syntax error/);
    });
  });

  describe('getSavedQueries', () => {
    it('should return a non-empty array', async () => {
      const queries = await queryService.getSavedQueries();
      expect(Array.isArray(queries)).toBe(true);
      expect(queries.length).toBeGreaterThan(0);
    });

    it('each query should have id, name, sql, and createdAt', async () => {
      const queries = await queryService.getSavedQueries();
      for (const q of queries) {
        expect(q.id).toBeDefined();
        expect(q.name).toBeDefined();
        expect(q.sql).toBeDefined();
        expect(q.createdAt).toBeDefined();
      }
    });
  });

  describe('getSavedQueryById', () => {
    it('should return a query for a valid id', async () => {
      const query = await queryService.getSavedQueryById('top-customers');
      expect(query).not.toBeNull();
      expect(query?.id).toBe('top-customers');
    });

    it('should return null for non-existent id', async () => {
      const query = await queryService.getSavedQueryById('nonexistent');
      expect(query).toBeNull();
    });
  });
});
