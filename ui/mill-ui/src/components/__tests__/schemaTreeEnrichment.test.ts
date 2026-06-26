import { describe, it, expect } from 'vitest';
import type { SchemaNode } from '../../types/schema';
import { enrichNodeChildren } from '../data-model/schemaTreeEnrichment';

describe('enrichNodeChildren', () => {
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

  it('should attach columns using case-insensitive table id match', () => {
    const columns: SchemaNode[] = [
      { id: 'skymill.passenger.id', type: 'COLUMN', name: 'id' },
    ];
    const next = enrichNodeChildren(tree, 'skymill.passenger', columns);
    const table = next[0]!.children![0]!.children![0]!;
    expect(table.children).toEqual(columns);
  });
});
