import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { queryService } from '../queryService';
import type { SavedQuery } from '../../types/query';

const sampleQuery: SavedQuery = {
  id: 'top-customers',
  name: 'Top Customers by Revenue',
  description: 'Customers ranked by total order value',
  sql: 'SELECT 1',
  createdAt: 1_738_065_600_000,
  updatedAt: 1_739_083_200_000,
  tags: ['revenue', 'customer'],
};

const samplePageEnvelope = {
  epoch: 1,
  pageIndex: 0,
  pageSize: 50,
  rowCount: 1,
  totalResult: 120,
  hasNext: true,
  hasPrevious: false,
  schema: [{ name: 'id', type: 'INT' }, { name: 'name', type: 'STRING' }],
  data: {
    fields: ['id', 'name'],
    rows: [[1, 'Acme']],
  },
};

describe('queryService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe('executeQuery', () => {
    it('should return mapped first page and keep session open', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            executionId: 'exec-1',
            firstPage: samplePageEnvelope,
          }),
          { status: 200 },
        ),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const result = await queryService.executeQuery('SELECT 1');
      expect(result.columns).toEqual([{ name: 'id', type: 'int' }, { name: 'name', type: 'string' }]);
      expect(result.rows).toEqual([{ id: 1, name: 'Acme' }]);
      expect(result.page).toMatchObject({
        executionId: 'exec-1',
        pageIndex: 0,
        pageSize: 50,
        totalResult: 120,
        hasNext: true,
        hasPrevious: false,
      });
      expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

    it('should throw for empty query', async () => {
      vi.stubGlobal('fetch', vi.fn());
      await expect(queryService.executeQuery('   ')).rejects.toThrow('Empty query');
    });
  });

  describe('fetchQueryPage', () => {
    it('should fetch another page from an existing session', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            ...samplePageEnvelope,
            pageIndex: 1,
            hasNext: false,
            hasPrevious: true,
          }),
          { status: 200 },
        ),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const result = await queryService.fetchQueryPage({
        executionId: 'exec-1',
        pageIndex: 1,
        pageSize: 50,
        epoch: 1,
      });
      expect(result.page.pageIndex).toBe(1);
      expect(result.page.hasPrevious).toBe(true);
      expect(fetchSpy).toHaveBeenCalledWith(
        '/api/v1/query/exec-1?pageIndex=1&pageSize=50&format=rows-compact-batch&epoch=1',
        { credentials: 'include' },
      );
    });
  });

  describe('closeQuerySession', () => {
    it('should DELETE the execution session', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(new Response(null, { status: 204 }));
      vi.stubGlobal('fetch', fetchSpy);

      await queryService.closeQuerySession('exec-1');
      expect(fetchSpy).toHaveBeenCalledWith(
        '/api/v1/query/exec-1',
        expect.objectContaining({ method: 'DELETE' }),
      );
    });
  });

  describe('getSavedQueries', () => {
    it('should map catalog list from GET /api/v1/analysis/queries', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(JSON.stringify({ queries: [sampleQuery] }), { status: 200 }),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const queries = await queryService.getSavedQueries();
      expect(queries).toHaveLength(1);
      expect(queries[0]?.id).toBe('top-customers');
      expect(fetchSpy).toHaveBeenCalledWith('/api/v1/analysis/queries', { credentials: 'include' });
    });
  });

  describe('getSavedQueryById', () => {
    it('should return query for existing id', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(JSON.stringify(sampleQuery), { status: 200 }),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const query = await queryService.getSavedQueryById('top-customers');
      expect(query?.name).toBe('Top Customers by Revenue');
    });

    it('should return null for 404', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(new Response('not found', { status: 404 }));
      vi.stubGlobal('fetch', fetchSpy);

      const query = await queryService.getSavedQueryById('missing');
      expect(query).toBeNull();
    });
  });

  describe('createSavedQuery', () => {
    it('should POST new query', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(JSON.stringify(sampleQuery), { status: 201 }),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const created = await queryService.createSavedQuery({ name: 'New', sql: 'SELECT 1' });
      expect(created.id).toBe('top-customers');
      expect(fetchSpy).toHaveBeenCalledWith('/api/v1/analysis/queries', expect.objectContaining({ method: 'POST' }));
    });
  });

  describe('updateSavedQuery', () => {
    it('should PUT query changes', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(JSON.stringify({ ...sampleQuery, sql: 'SELECT 2' }), { status: 200 }),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const updated = await queryService.updateSavedQuery('top-customers', {
        name: sampleQuery.name,
        sql: 'SELECT 2',
      });
      expect(updated.sql).toBe('SELECT 2');
      expect(fetchSpy).toHaveBeenCalledWith(
        '/api/v1/analysis/queries/top-customers',
        expect.objectContaining({ method: 'PUT' }),
      );
    });
  });

  describe('deleteSavedQuery', () => {
    it('should DELETE query', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(new Response(null, { status: 204 }));
      vi.stubGlobal('fetch', fetchSpy);

      await queryService.deleteSavedQuery('top-customers');
      expect(fetchSpy).toHaveBeenCalledWith(
        '/api/v1/analysis/queries/top-customers',
        expect.objectContaining({ method: 'DELETE' }),
      );
    });
  });
});
