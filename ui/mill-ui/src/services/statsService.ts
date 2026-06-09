import type { StatsService } from '../types/stats';
import { mockSchemaTree } from '../data/mockSchema';
import { mockConcepts } from '../data/mockConcepts';
import { queryService } from './queryService';

const mockStatsService: StatsService = {
  async getStats() {
    let tableCount = 0;
    for (const schema of mockSchemaTree) {
      if (schema.children) {
        tableCount += schema.children.filter((c) => c.type === 'TABLE').length;
      }
    }
    let queryCount = 0;
    try {
      queryCount = (await queryService.getSavedQueries()).length;
    } catch {
      queryCount = 0;
    }
    return {
      schemaCount: mockSchemaTree.length,
      tableCount,
      conceptCount: mockConcepts.length,
      queryCount,
    };
  },
};

// When real backend is ready, create realStatsService and change the export below
export const statsService: StatsService = mockStatsService;
