import { describe, it, expect } from 'vitest';
import { mockSavedQueries, getResultForQuery, getSavedQueryById } from '../mockQueries';

describe('mockSavedQueries', () => {
  it('should contain 6 sample queries', () => {
    expect(mockSavedQueries).toHaveLength(6);
  });

  it('should have unique IDs', () => {
    const ids = mockSavedQueries.map((q) => q.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  it('should have non-empty name and sql for every query', () => {
    for (const query of mockSavedQueries) {
      expect(query.name).toBeTruthy();
      expect(query.sql).toBeTruthy();
    }
  });

  it('should have valid timestamps', () => {
    for (const query of mockSavedQueries) {
      expect(query.createdAt).toBeGreaterThan(0);
      expect(query.updatedAt).toBeGreaterThanOrEqual(query.createdAt);
    }
  });
});

describe('getSavedQueryById', () => {
  it('should return a query by its ID', () => {
    const query = getSavedQueryById('top-customers');
    expect(query).toBeDefined();
    expect(query!.name).toBe('Top Customers by Revenue');
  });

  it('should return undefined for unknown ID', () => {
    expect(getSavedQueryById('nonexistent')).toBeUndefined();
  });
});

describe('getResultForQuery', () => {
  it('should return top-customers result for customer revenue SQL', () => {
    const result = getResultForQuery('SELECT customer_id, SUM(total_amount) AS revenue FROM customers');
    expect(result.columns).toBeDefined();
    expect(result.rowCount).toBeGreaterThan(0);
  });

  it('should return daily-revenue result for date-based SQL', () => {
    const result = getResultForQuery('SELECT DATE(order_date), COUNT(*) FROM orders GROUP BY DATE(order_date)');
    expect(result.columns.some((c) => c.name === 'date')).toBe(true);
  });

  it('should return order-status result for status grouping SQL', () => {
    const result = getResultForQuery('SELECT status, COUNT(*) FROM orders GROUP BY status');
    expect(result.columns.some((c) => c.name === 'status')).toBe(true);
  });

  it('should return segment result for segment grouping SQL', () => {
    const result = getResultForQuery('SELECT segment, COUNT(*) FROM customers GROUP BY segment');
    expect(result.columns.some((c) => c.name === 'segment')).toBe(true);
  });

  it('should return low-stock result for stock SQL', () => {
    const result = getResultForQuery('SELECT * FROM products WHERE stock_quantity < 50');
    expect(result.columns.some((c) => c.name === 'stock_quantity')).toBe(true);
  });

  it('should return lifetime-value result for CLV SQL', () => {
    const result = getResultForQuery('SELECT customer_id, lifetime_value FROM customer_metrics');
    expect(result.columns.some((c) => c.name === 'lifetime_value')).toBe(true);
  });

  it('should return generic result for unrecognized SQL', () => {
    const result = getResultForQuery('SELECT 1');
    expect(result.rowCount).toBe(5);
    expect(result.columns.some((c) => c.name === 'id')).toBe(true);
  });

  it('should always return valid structure', () => {
    const sqls = ['', 'SELECT * FROM foo', 'DROP TABLE users'];
    for (const sql of sqls) {
      const result = getResultForQuery(sql);
      expect(result.columns).toBeDefined();
      expect(Array.isArray(result.rows)).toBe(true);
      expect(typeof result.rowCount).toBe('number');
      expect(typeof result.executionTimeMs).toBe('number');
    }
  });
});
