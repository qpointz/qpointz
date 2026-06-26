import { describe, it, expect } from 'vitest';
import { groupMessageArtifacts } from '../artifactGroups';

describe('groupMessageArtifacts', () => {
  it('should emit one facet-proposal group per artefact', () => {
    const groups = groupMessageArtifacts([
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'sales.customers',
        payload: { summary: 'VIP' },
      },
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'sales.orders',
        payload: { summary: 'Orders' },
      },
    ]);

    expect(groups).toHaveLength(2);
    expect(groups.every((g) => g.kind === 'facet-proposal')).toBe(true);
  });

  it('should pair each SQL with its data by sourceArtifactId', () => {
    const groups = groupMessageArtifacts([
      { kind: 'sql', sql: 'select 1', artifactId: 'sql-1' },
      { kind: 'sql', sql: 'select 2', artifactId: 'sql-2' },
      {
        kind: 'data',
        executionId: 'e2',
        sql: 'select 2',
        columns: [],
        sourceArtifactId: 'sql-2',
      },
    ]);

    expect(groups).toHaveLength(2);
    const first = groups[0];
    const second = groups[1];
    expect(first?.kind).toBe('sql-data-composite');
    expect(second?.kind).toBe('sql-data-composite');
    if (first?.kind !== 'sql-data-composite' || second?.kind !== 'sql-data-composite') {
      throw new Error('expected sql-data-composite groups');
    }
    expect(first.sql?.artifactId).toBe('sql-1');
    expect(first.data).toBeUndefined();
    expect(second.sql?.artifactId).toBe('sql-2');
    expect(second.data?.executionId).toBe('e2');
  });

  it('should pair data to matching SQL by sql text when ids are missing', () => {
    const groups = groupMessageArtifacts([
      { kind: 'sql', sql: 'SELECT * FROM aircraft' },
      { kind: 'sql', sql: 'SELECT * FROM aircraft_types' },
      {
        kind: 'data',
        executionId: 'e-types',
        sql: 'SELECT * FROM aircraft_types',
        columns: [],
      },
    ]);

    expect(groups).toHaveLength(2);
    const first = groups[0];
    const second = groups[1];
    expect(first?.kind).toBe('sql-data-composite');
    expect(second?.kind).toBe('sql-data-composite');
    if (first?.kind !== 'sql-data-composite' || second?.kind !== 'sql-data-composite') {
      throw new Error('expected sql-data-composite groups');
    }
    expect(first.sql?.sql).toContain('aircraft');
    expect(first.data).toBeUndefined();
    expect(second.data?.executionId).toBe('e-types');
  });

  it('should attach late data to the correct earlier SQL in stream order', () => {
    const groups = groupMessageArtifacts([
      { kind: 'sql', sql: 'SELECT * FROM aircraft' },
      { kind: 'sql', sql: 'SELECT * FROM aircraft_types' },
      {
        kind: 'data',
        executionId: 'e-aircraft',
        sql: 'SELECT * FROM aircraft',
        columns: [],
      },
    ]);

    expect(groups).toHaveLength(2);
    const first = groups[0];
    const second = groups[1];
    expect(first?.kind).toBe('sql-data-composite');
    expect(second?.kind).toBe('sql-data-composite');
    if (first?.kind !== 'sql-data-composite' || second?.kind !== 'sql-data-composite') {
      throw new Error('expected sql-data-composite groups');
    }
    expect(first.data?.executionId).toBe('e-aircraft');
    expect(second.data).toBeUndefined();
  });
});
