import { describe, expect, it } from 'vitest';
import type { SchemaNode } from '../../../types/schema';
import { collectTreeExpansionIds } from '../schemaTreeExpansion';

const tree: SchemaNode[] = [
  {
    id: 'model',
    type: 'MODEL',
    name: 'Model',
    children: [
      {
        id: 'skymill',
        type: 'SCHEMA',
        name: 'skymill',
        children: [
          {
            id: 'skymill.passenger',
            type: 'TABLE',
            name: 'passenger',
            children: [],
          },
        ],
      },
    ],
  },
];

const treeWithColumns: SchemaNode[] = [
  {
    id: 'model',
    type: 'MODEL',
    name: 'Model',
    children: [
      {
        id: 'skymill',
        type: 'SCHEMA',
        name: 'skymill',
        children: [
          {
            id: 'skymill.passenger',
            type: 'TABLE',
            name: 'passenger',
            children: [
              { id: 'skymill.passenger.id', type: 'COLUMN', name: 'id' },
            ],
          },
        ],
      },
    ],
  },
];

describe('collectTreeExpansionIds', () => {
  it('shouldExpandAncestors_whenColumnSelectedAndChildrenLoaded', () => {
    const expanded = collectTreeExpansionIds(treeWithColumns, 'skymill.passenger.id');
    expect(expanded).toEqual(new Set(['model', 'skymill', 'skymill.passenger']));
  });

  it('shouldExpandTablePath_whenColumnsNotYetLoaded', () => {
    const expanded = collectTreeExpansionIds(tree, 'skymill.passenger.id');
    expect(expanded).toEqual(new Set(['model', 'skymill', 'skymill.passenger']));
  });

  it('shouldExpandSchema_whenTableSelected', () => {
    const expanded = collectTreeExpansionIds(tree, 'skymill.passenger');
    expect(expanded).toEqual(new Set(['model', 'skymill']));
  });
});
