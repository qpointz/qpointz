import { describe, expect, it } from 'vitest';
import {
  quoteIdentifierPart,
  quoteQualifiedName,
  toQuotedColumnCompletions,
  toQuotedSchemaCompletions,
} from '../quoteSqlIdentifier';

describe('quoteSqlIdentifier', () => {
  it('should wrap a single segment with backticks', () => {
    expect(quoteIdentifierPart('sales', '`', '`')).toBe('`sales`');
  });

  it('should escape embedded quote characters', () => {
    expect(quoteIdentifierPart('a`b', '`', '`')).toBe('`a``b`');
    expect(quoteIdentifierPart('a"b', '"', '"')).toBe('"a""b"');
  });

  it('should quote each segment of a qualified name', () => {
    expect(quoteQualifiedName('sales.orders', '`', '`')).toBe('`sales`.`orders`');
    expect(quoteQualifiedName('sales.orders.id', '"', '"')).toBe('"sales"."orders"."id"');
  });

  it('should set apply text on schema completion options', () => {
    const options = toQuotedSchemaCompletions(
      [{ label: 'sales.customers', kind: 'table', schema: 'sales', table: 'customers' }],
      '`',
      '`',
    );
    expect(options[0]?.label).toBe('sales.customers');
    expect(options[0]?.apply).toBe('`sales`.`customers`');
  });

  it('should use short column labels with qualified apply text', () => {
    const options = toQuotedColumnCompletions(
      [{ label: 'sales.orders.customer_id', kind: 'column', schema: 'sales', table: 'orders' }],
      '`',
      '`',
    );
    expect(options[0]?.label).toBe('customer_id');
    expect(options[0]?.apply).toBe('`sales`.`orders`.`customer_id`');
    expect(options[0]?.detail).toBe('sales.orders.customer_id');
  });
});
