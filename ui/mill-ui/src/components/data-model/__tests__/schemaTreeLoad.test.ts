import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { SchemaNode } from '../../types/schema';

const testTree: SchemaNode[] = [
  {
    id: 'model-entity',
    type: 'MODEL',
    name: 'Model',
    children: [
      {
        id: 'skymill',
        type: 'SCHEMA',
        name: 'skymill',
        children: [{ id: 'skymill.passenger', type: 'TABLE', name: 'passenger', children: [] }],
      },
    ],
  },
];

const tableDetail = {
  id: 'skymill.passenger',
  entityType: 'TABLE' as const,
  schemaName: 'skymill',
  tableName: 'passenger',
  tableType: 'TABLE',
  columns: [
    {
      id: 'skymill.passenger.id',
      entityType: 'COLUMN' as const,
      schemaName: 'skymill',
      tableName: 'passenger',
      columnName: 'id',
      fieldIndex: 0,
      type: { type: 'BIG_INT', nullable: true },
    },
  ],
};

vi.mock('../../../services/api', () => ({
  schemaService: {
    getTree: vi.fn(() => Promise.resolve(testTree)),
    getTable: vi.fn(() => Promise.resolve(tableDetail)),
  },
}));

describe('loadExplorerTreeWithColumns', () => {
  beforeEach(async () => {
    const { schemaService } = await import('../../../services/api');
    vi.mocked(schemaService.getTree).mockResolvedValue(testTree);
    vi.mocked(schemaService.getTable).mockResolvedValue(tableDetail);
  });

  it('should attach columns when deep-linking to a column', async () => {
    const { loadExplorerTreeWithColumns } = await import('../schemaTreeLoad');
    const tree = await loadExplorerTreeWithColumns('global', 'skymill.passenger.id', null);
    const table = tree[0]!.children![0]!.children![0]!;
    expect(table.children?.map((column) => column.name)).toEqual(['id']);
  });
});
