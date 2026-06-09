import { describe, expect, it } from 'vitest';
import type { SchemaNode } from '../../../types/schema';
import {
  buildColumnCompletionEntries,
  buildCompletionIndexFromTree,
  filterColumnEntries,
  filterCompletionEntries,
  MAX_COMPLETION_ENTRIES,
} from '../schemaCompletionIndex';

const sampleTree: SchemaNode[] = [
  {
    id: 'model',
    type: 'MODEL',
    name: 'Model',
    children: [
      {
        id: 's1',
        type: 'SCHEMA',
        name: 'sales',
        children: [
          { id: 't1', type: 'TABLE', name: 'orders' },
          { id: 't2', type: 'TABLE', name: 'customers' },
        ],
      },
      {
        id: 's2',
        type: 'SCHEMA',
        name: 'hr',
        children: [{ id: 't3', type: 'TABLE', name: 'employees' }],
      },
    ],
  },
];

describe('buildCompletionIndexFromTree', () => {
  it('should include schemas and qualified tables', () => {
    const index = buildCompletionIndexFromTree(sampleTree);
    expect(index.map((e) => e.label)).toEqual(
      expect.arrayContaining(['sales', 'hr', 'sales.orders', 'sales.customers', 'hr.employees']),
    );
    expect(index.find((e) => e.label === 'sales')?.kind).toBe('schema');
    expect(index.find((e) => e.label === 'sales.orders')?.kind).toBe('table');
  });

  it('should cap entries at MAX_COMPLETION_ENTRIES', () => {
    const hugeTree: SchemaNode[] = [
      {
        id: 'model',
        type: 'MODEL',
        name: 'Model',
        children: [
          {
            id: 'big',
            type: 'SCHEMA',
            name: 'bigschema',
            children: Array.from({ length: MAX_COMPLETION_ENTRIES + 50 }, (_, i) => ({
              id: `t-${i}`,
              type: 'TABLE' as const,
              name: `table_${i}`,
            })),
          },
        ],
      },
    ];
    const index = buildCompletionIndexFromTree(hugeTree);
    expect(index.length).toBeLessThanOrEqual(MAX_COMPLETION_ENTRIES);
  });
});

describe('filterCompletionEntries', () => {
  it('should filter by case-insensitive prefix', () => {
    const index = buildCompletionIndexFromTree(sampleTree);
    const filtered = filterCompletionEntries(index, 'Sal');
    expect(filtered.map((e) => e.label)).toEqual(['sales', 'sales.orders', 'sales.customers']);
  });
});

describe('column completion helpers', () => {
  it('should build qualified column labels', () => {
    const entries = buildColumnCompletionEntries('sales', 'orders', ['id', 'amount']);
    expect(entries.map((e) => e.label)).toEqual(['sales.orders.id', 'sales.orders.amount']);
  });

  it('should filter columns by partial name', () => {
    const entries = buildColumnCompletionEntries('sales', 'orders', ['order_id', 'amount', 'customer_id']);
    const filtered = filterColumnEntries(entries, 'ord');
    expect(filtered.map((e) => e.label)).toEqual(['sales.orders.order_id']);
  });
});
