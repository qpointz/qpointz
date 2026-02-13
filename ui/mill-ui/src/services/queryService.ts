import type { QueryService } from '../types/query';
import { getResultForQuery, mockSavedQueries, getSavedQueryById } from '../data/mockQueries';

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const mockQueryService: QueryService = {
  async executeQuery(sql: string) {
    // Simulate network + execution delay (800-1500ms)
    await sleep(800 + Math.random() * 700);

    // Simulate syntax errors for empty or very short queries
    const trimmed = sql.trim();
    if (!trimmed) {
      throw new Error('Empty query');
    }
    if (trimmed.length < 10 && !trimmed.toLowerCase().startsWith('select')) {
      throw new Error(`Syntax error near "${trimmed}": expected SELECT, INSERT, UPDATE, or DELETE`);
    }

    // Return mock result based on SQL content
    const result = getResultForQuery(sql);

    // Randomize execution time slightly
    return {
      ...result,
      executionTimeMs: Math.round(result.executionTimeMs * (0.8 + Math.random() * 0.4)),
    };
  },

  async getSavedQueries() {
    return mockSavedQueries;
  },

  async getSavedQueryById(id: string) {
    return getSavedQueryById(id) ?? null;
  },
};

// When real backend is ready, create realQueryService and change the export below
export const queryService: QueryService = mockQueryService;
