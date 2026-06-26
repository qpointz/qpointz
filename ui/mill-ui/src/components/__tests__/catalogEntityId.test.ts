import { describe, it, expect } from 'vitest';
import {
  catalogIdsEqual,
  catalogIdStartsWith,
  findTreeNodeId,
  resolveColumnSyncTarget,
  treeTableHasColumnChildren,
} from '../data-model/catalogEntityId';
import type { SchemaNode } from '../../types/schema';

describe('catalogIdsEqual', () => {
  it('should compare dotted ids case-insensitively', () => {
    expect(catalogIdsEqual('Skymill.Passenger.id', 'skymill.passenger.id')).toBe(true);
  });
});

describe('catalogIdStartsWith', () => {
  it('should match descendant catalog paths case-insensitively', () => {
    expect(catalogIdStartsWith('skymill.passenger.id', 'Skymill.Passenger')).toBe(true);
  });
});

describe('findTreeNodeId', () => {
  const tree: SchemaNode[] = [
    {
      id: 'model-entity',
      type: 'MODEL',
      name: 'Model',
      children: [
        {
          id: 'Skymill',
          type: 'SCHEMA',
          name: 'skymill',
          children: [{ id: 'Skymill.Passenger', type: 'TABLE', name: 'passenger', children: [] }],
        },
      ],
    },
  ];

  it('should return the canonical tree id for a case-insensitive match', () => {
    expect(findTreeNodeId(tree, 'skymill.passenger')).toBe('Skymill.Passenger');
  });
});

describe('resolveColumnSyncTarget', () => {
  it('should derive table coordinates from a column deep-link route before entity load', () => {
    expect(resolveColumnSyncTarget(null, 'skymill.passenger.id')).toEqual({
      schemaName: 'skymill',
      tableName: 'passenger',
      tableCatalogId: 'skymill.passenger',
    });
  });

  it('should derive table coordinates from a table deep-link route before entity load', () => {
    expect(resolveColumnSyncTarget(null, 'skymill.passenger')).toEqual({
      schemaName: 'skymill',
      tableName: 'passenger',
      tableCatalogId: 'skymill.passenger',
    });
  });
});

describe('treeTableHasColumnChildren', () => {
  const tree: SchemaNode[] = [
    {
      id: 'sales',
      type: 'SCHEMA',
      name: 'sales',
      children: [
        { id: 'Sales.Customers', type: 'TABLE', name: 'customers', children: [] },
        {
          id: 'sales.orders',
          type: 'TABLE',
          name: 'orders',
          children: [{ id: 'sales.orders.order_id', type: 'COLUMN', name: 'order_id' }],
        },
      ],
    },
  ];

  it('should return false when the table has no column children', () => {
    expect(treeTableHasColumnChildren(tree, 'sales.customers')).toBe(false);
  });

  it('should return true when the table already has column children', () => {
    expect(treeTableHasColumnChildren(tree, 'sales.orders')).toBe(true);
  });
});
