import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { analysisService } from '../analysisService';

describe('analysisService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe('getDialect', () => {
    it('should fetch configured dialect from GET /api/v1/analysis/dialect', async () => {
      const fetchSpy = vi.fn().mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            id: 'CALCITE',
            name: 'Apache Calcite',
            readOnly: true,
            editorDialect: 'standard',
            identifiers: { quoteStart: '`', quoteEnd: '`' },
            functions: { aggregates: ['COUNT'] },
          }),
          { status: 200 },
        ),
      );
      vi.stubGlobal('fetch', fetchSpy);

      const dialect = await analysisService.getDialect();
      expect(dialect.id).toBe('CALCITE');
      expect(dialect.editorDialect).toBe('standard');
      expect(fetchSpy).toHaveBeenCalledWith('/api/v1/analysis/dialect', { credentials: 'include' });
    });
  });
});
