import { describe, expect, it } from 'vitest';
import { buildAnalysisContextSnapshot } from '../buildAnalysisContextSnapshot';
import type { QueryResult } from '../../types/query';

describe('buildAnalysisContextSnapshot', () => {
  it('should include sql, query metadata, and execution summary without raw rows', () => {
    const result: QueryResult = {
      columns: [
        { name: 'id', type: 'INTEGER' },
        { name: 'name', type: 'VARCHAR' },
      ],
      rows: [{ id: 1, name: 'alpha' }],
      rowCount: 1,
      executionTimeMs: 12,
      page: {
        executionId: 'exec-1',
        epoch: 0,
        pageIndex: 0,
        pageSize: 100,
        pageRowCount: 1,
        totalResult: 1,
        hasNext: false,
        hasPrevious: false,
      },
    };

    const values = buildAnalysisContextSnapshot({
      sql: 'SELECT id, name FROM users',
      dialectId: 'postgresql',
      activeQueryId: 'q-1',
      activeQueryName: 'Users',
      activeQueryDescription: 'All users',
      isDirty: true,
      isExecuting: false,
      error: null,
      result,
    });

    expect(values['sql.current']).toBe('SELECT id, name FROM users');
    expect(values['sql.dialect']).toBe('postgresql');
    expect(values['artifact.query.id']).toBe('q-1');
    expect(values['artifact.query.name']).toBe('Users');
    expect(values['artifact.query.description']).toBe('All users');
    expect(values['artifact.query.dirty']).toBe(true);
    expect(values['execution.last.id']).toBe('exec-1');
    expect(values['execution.last.status']).toBe('completed');
    expect(values['execution.last.rowCount']).toBe(1);
    expect(values['execution.last.columns']).toEqual([
      { name: 'id', type: 'INTEGER' },
      { name: 'name', type: 'VARCHAR' },
    ]);
    expect(values).not.toHaveProperty('execution.last.rows');
    expect(JSON.stringify(values)).not.toContain('alpha');
  });

  it('should report running status and error summary when execution fails', () => {
    const values = buildAnalysisContextSnapshot({
      sql: 'SELECT bad',
      isDirty: false,
      isExecuting: false,
      error: 'Syntax error near bad',
      activeExecutionId: 'exec-err',
    });

    expect(values['execution.last.id']).toBe('exec-err');
    expect(values['execution.last.status']).toBe('failed');
    expect(values['execution.last.error']).toBe('Syntax error near bad');
  });

  it('should omit empty optional keys for unsaved analysis context', () => {
    const values = buildAnalysisContextSnapshot({
      sql: 'SELECT 1',
      isDirty: true,
      isExecuting: true,
    });

    expect(values['sql.current']).toBe('SELECT 1');
    expect(values['artifact.query.dirty']).toBe(true);
    expect(values['execution.last.status']).toBe('running');
    expect(values).not.toHaveProperty('artifact.query.id');
    expect(values).not.toHaveProperty('artifact.query.name');
  });
});
