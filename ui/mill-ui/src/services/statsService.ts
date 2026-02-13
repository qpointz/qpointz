import type { StatsService } from '../types/stats';
import { mockSchemaTree } from '../data/mockSchema';
import { mockConcepts } from '../data/mockConcepts';
import { mockSavedQueries } from '../data/mockQueries';

const mockStatsService: StatsService = {
  async getStats() {
    let tableCount = 0;
    for (const schema of mockSchemaTree) {
      if (schema.children) {
        tableCount += schema.children.filter((c) => c.type === 'TABLE').length;
      }
    }
    return {
      schemaCount: mockSchemaTree.length,
      tableCount,
      conceptCount: mockConcepts.length,
      queryCount: mockSavedQueries.length,
    };
  },
};

// When real backend is ready, create realStatsService and change the export below
export const statsService: StatsService = mockStatsService;
