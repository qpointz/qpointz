import { describe, expect, it } from 'vitest';
import { dialectQuotedApplyText, stripIdentifierQuotes } from '../dialectQuotedSchemaCompletion';

describe('stripIdentifierQuotes', () => {
  it('should strip backticks', () => {
    expect(stripIdentifierQuotes('`sales`')).toBe('sales');
  });

  it('should strip double quotes', () => {
    expect(stripIdentifierQuotes('"orders"')).toBe('orders');
  });
});

describe('dialectQuotedApplyText', () => {
  it('should quote a single segment with dialect characters', () => {
    expect(dialectQuotedApplyText('orders', '`', '`')).toBe('`orders`');
  });

  it('should quote each qualified segment', () => {
    expect(dialectQuotedApplyText('sales.orders', '`', '`')).toBe('`sales`.`orders`');
  });

  it('should normalize already quoted labels before requoting', () => {
    expect(dialectQuotedApplyText('`sales`.`orders`', '"', '"')).toBe('"sales"."orders"');
  });
});
