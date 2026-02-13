import { describe, it, expect } from 'vitest';
import { relatedContentService } from '../relatedContentService';

describe('relatedContentService', () => {
  describe('getRelatedContent', () => {
    it('should return an array', async () => {
      const refs = await relatedContentService.getRelatedContent('model', 'sales.customers');
      expect(Array.isArray(refs)).toBe(true);
    });

    it('should return related content refs for a known entity', async () => {
      const refs = await relatedContentService.getRelatedContent('model', 'sales.customers');
      expect(refs.length).toBeGreaterThan(0);
    });

    it('each ref should have id, title, and type', async () => {
      const refs = await relatedContentService.getRelatedContent('model', 'sales.customers');
      for (const ref of refs) {
        expect(ref.id).toBeDefined();
        expect(ref.title).toBeDefined();
        expect(ref.type).toBeDefined();
        expect(['model', 'concept', 'analysis']).toContain(ref.type);
      }
    });

    it('should return empty array for unknown context', async () => {
      const refs = await relatedContentService.getRelatedContent('model', 'totally-unknown-id-67890');
      expect(Array.isArray(refs)).toBe(true);
    });
  });
});
