import { describe, expect, it } from 'vitest';
import { mapQueryPageRows } from '../queryRowFormat';

describe('mapQueryPageRows', () => {
  it('should map rows-objects arrays', () => {
    expect(mapQueryPageRows([{ id: 1, name: 'Acme' }])).toEqual([{ id: 1, name: 'Acme' }]);
  });

  it('should map rows-compact-batch payloads', () => {
    expect(mapQueryPageRows({
      fields: ['id', 'name'],
      rows: [[1, 'Acme'], [2, null]],
    })).toEqual([
      { id: 1, name: 'Acme' },
      { id: 2, name: null },
    ]);
  });
});
