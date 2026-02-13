import { describe, it, expect } from 'vitest';
import { searchService } from '../searchService';
import type { SearchResult } from '../../types/search';

describe('searchService', () => {
  describe('short query guard', () => {
    it('should return empty array for empty string', async () => {
      expect(await searchService.search('')).toEqual([]);
    });

    it('should return empty array for single character', async () => {
      expect(await searchService.search('a')).toEqual([]);
    });

    it('should return empty array for whitespace-only input', async () => {
      expect(await searchService.search('   ')).toEqual([]);
    });

    it('should return empty array for single char with whitespace', async () => {
      expect(await searchService.search(' x ')).toEqual([]);
    });
  });

  describe('view search', () => {
    it('should match views by name', async () => {
      const results = await searchService.search('chat');
      const viewResults = results.filter((r) => r.type === 'view');
      expect(viewResults.some((r) => r.name === 'Chat')).toBe(true);
    });

    it('should match views by description', async () => {
      const results = await searchService.search('dashboard');
      const viewResults = results.filter((r) => r.type === 'view');
      expect(viewResults.some((r) => r.name === 'Home')).toBe(true);
    });

    it('should return correct route for views', async () => {
      const results = await searchService.search('model');
      const modelView = results.find((r) => r.type === 'view' && r.name === 'Model');
      expect(modelView?.route).toBe('/model');
    });
  });

  describe('schema entity search', () => {
    it('should find schemas by name', async () => {
      const results = await searchService.search('sales');
      expect(results.some((r) => r.type === 'schema' && r.name === 'Sales')).toBe(true);
    });

    it('should find tables by display name', async () => {
      const results = await searchService.search('customers');
      expect(results.some((r) => r.type === 'table' && r.name === 'Customers')).toBe(true);
    });

    it('should find attributes by display name', async () => {
      const results = await searchService.search('email');
      expect(results.some((r) => r.type === 'attribute')).toBe(true);
    });

    it('should find entities by tag', async () => {
      const results = await searchService.search('pii');
      expect(results.length).toBeGreaterThan(0);
    });

    it('should find attributes by synonym', async () => {
      // "cust_id" is a synonym for customer_id
      const results = await searchService.search('cust_id');
      expect(results.some((r) => r.type === 'attribute' && r.name === 'Customer ID')).toBe(true);
    });

    it('should build correct route from dotted entity id', async () => {
      const results = await searchService.search('customers');
      const table = results.find((r) => r.id === 'sales.customers');
      expect(table?.route).toBe('/model/sales/customers');
    });

    it('should build correct breadcrumb', async () => {
      const results = await searchService.search('email');
      const attr = results.find((r) => r.id === 'sales.customers.email');
      expect(attr?.breadcrumb).toBe('sales > customers');
    });
  });

  describe('concept search', () => {
    it('should find concepts by name', async () => {
      const results = await searchService.search('lifetime value');
      expect(results.some((r) => r.type === 'concept' && r.name.includes('Lifetime Value'))).toBe(true);
    });

    it('should find concepts by category', async () => {
      const results = await searchService.search('analytics');
      expect(results.some((r) => r.type === 'concept')).toBe(true);
    });

    it('should find concepts by tag', async () => {
      const results = await searchService.search('retention');
      expect(results.some((r) => r.type === 'concept')).toBe(true);
    });

    it('should truncate long descriptions to 80 chars', async () => {
      const results = await searchService.search('lifetime value');
      const clv = results.find((r) => r.id === 'customer-lifetime-value');
      expect(clv?.description).toBeDefined();
      expect(clv!.description!.length).toBeLessThanOrEqual(81); // 80 + '…'
    });

    it('should include category as breadcrumb', async () => {
      const results = await searchService.search('lifetime value');
      const clv = results.find((r) => r.id === 'customer-lifetime-value');
      expect(clv?.breadcrumb).toBe('Analytics');
    });

    it('should build correct route for concepts', async () => {
      const results = await searchService.search('lifetime value');
      const clv = results.find((r) => r.id === 'customer-lifetime-value');
      expect(clv?.route).toBe('/knowledge/customer-lifetime-value');
    });
  });

  describe('saved query search', () => {
    it('should find queries by name', async () => {
      const results = await searchService.search('top customers');
      expect(results.some((r) => r.type === 'query' && r.name.includes('Top Customers'))).toBe(true);
    });

    it('should find queries by tag', async () => {
      const results = await searchService.search('revenue');
      expect(results.some((r) => r.type === 'query')).toBe(true);
    });

    it('should build correct route for queries', async () => {
      const results = await searchService.search('top customers');
      const q = results.find((r) => r.id === 'top-customers');
      expect(q?.route).toBe('/analysis/top-customers');
    });
  });

  describe('case insensitivity', () => {
    it('should match regardless of case', async () => {
      const lower = await searchService.search('sales');
      const upper = await searchService.search('SALES');
      const mixed = await searchService.search('SaLeS');
      expect(lower.length).toBe(upper.length);
      expect(lower.length).toBe(mixed.length);
    });
  });

  describe('result structure', () => {
    it('every result should have required fields', async () => {
      const results = await searchService.search('customer');
      expect(results.length).toBeGreaterThan(0);
      for (const r of results) {
        expect(r.id).toBeDefined();
        expect(r.name).toBeDefined();
        expect(r.type).toBeDefined();
        expect(r.route).toBeDefined();
        expect(typeof r.id).toBe('string');
        expect(typeof r.name).toBe('string');
        expect(typeof r.route).toBe('string');
      }
    });

    it('route should start with /', async () => {
      const results = await searchService.search('customer');
      for (const r of results) {
        expect(r.route.startsWith('/')).toBe(true);
      }
    });

    it('type should be one of the valid SearchResultType values', async () => {
      const validTypes = ['view', 'schema', 'table', 'attribute', 'concept', 'query'];
      const results = await searchService.search('or');
      for (const r of results) {
        expect(validTypes).toContain(r.type);
      }
    });
  });

  describe('max results cap', () => {
    it('should return at most 20 results', async () => {
      // "a" is too short but "or" should match many things
      const results = await searchService.search('or');
      expect(results.length).toBeLessThanOrEqual(20);
    });
  });

  describe('cross-type search', () => {
    it('should return results from multiple types for a broad query', async () => {
      const results = await searchService.search('customer');
      const types = new Set(results.map((r: SearchResult) => r.type));
      // Should find at least schema entities and concepts related to "customer"
      expect(types.size).toBeGreaterThanOrEqual(2);
    });
  });
});
