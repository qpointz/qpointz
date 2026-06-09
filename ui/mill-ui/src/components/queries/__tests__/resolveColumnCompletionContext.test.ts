import { describe, expect, it } from 'vitest';
import type { SchemaCompletionEntry } from '../schemaCompletionIndex';
import { resolveColumnCompletionContext } from '../resolveColumnCompletionContext';

const tables: SchemaCompletionEntry[] = [
  { label: 'sales.orders', kind: 'table', schema: 'sales', table: 'orders' },
  { label: 'sales.customers', kind: 'table', schema: 'sales', table: 'customers' },
  { label: 'hr.orders', kind: 'table', schema: 'hr', table: 'orders' },
];

describe('resolveColumnCompletionContext', () => {
  it('should resolve unquoted schema.table.column partial', () => {
    const ctx = resolveColumnCompletionContext('SELECT sales.orders.c', tables);
    expect(ctx).toEqual({
      schema: 'sales',
      table: 'orders',
      partial: 'c',
      from: 7,
    });
  });

  it('should resolve quoted schema.table.column partial', () => {
    const ctx = resolveColumnCompletionContext('SELECT `sales`.`orders`.cu', tables);
    expect(ctx).toEqual({
      schema: 'sales',
      table: 'orders',
      partial: 'cu',
      from: 7,
    });
  });

  it('should resolve table.column when table name is unique', () => {
    const ctx = resolveColumnCompletionContext('SELECT customers.c', tables);
    expect(ctx).toEqual({
      schema: 'sales',
      table: 'customers',
      partial: 'c',
      from: 7,
    });
  });

  it('should not resolve table.column when table name is ambiguous', () => {
    expect(resolveColumnCompletionContext('SELECT orders.c', tables)).toBeNull();
  });

  it('should not treat schema.table without trailing dot as column context', () => {
    expect(resolveColumnCompletionContext('SELECT sales.orders', tables)).toBeNull();
  });
});
