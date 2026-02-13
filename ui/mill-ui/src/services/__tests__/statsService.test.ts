import { describe, it, expect } from 'vitest';
import { statsService } from '../statsService';

describe('statsService', () => {
  describe('getStats', () => {
    it('should return dashboard stats object', async () => {
      const stats = await statsService.getStats();
      expect(stats).toBeDefined();
    });

    it('should include schemaCount', async () => {
      const stats = await statsService.getStats();
      expect(typeof stats.schemaCount).toBe('number');
      expect(stats.schemaCount).toBeGreaterThan(0);
    });

    it('should include tableCount', async () => {
      const stats = await statsService.getStats();
      expect(typeof stats.tableCount).toBe('number');
      expect(stats.tableCount).toBeGreaterThan(0);
    });

    it('should include conceptCount', async () => {
      const stats = await statsService.getStats();
      expect(typeof stats.conceptCount).toBe('number');
      expect(stats.conceptCount).toBeGreaterThan(0);
    });

    it('should include queryCount', async () => {
      const stats = await statsService.getStats();
      expect(typeof stats.queryCount).toBe('number');
      expect(stats.queryCount).toBeGreaterThan(0);
    });
  });
});
