import { describe, it, expect } from 'vitest';
import { conceptService } from '../conceptService';

describe('conceptService', () => {
  describe('getConcepts', () => {
    it('should return all concepts when no filter is provided', async () => {
      const concepts = await conceptService.getConcepts();
      expect(Array.isArray(concepts)).toBe(true);
      expect(concepts.length).toBeGreaterThan(0);
    });

    it('each concept should have id, name, category, tags, and description', async () => {
      const concepts = await conceptService.getConcepts();
      for (const c of concepts) {
        expect(c.id).toBeDefined();
        expect(c.name).toBeDefined();
        expect(c.category).toBeDefined();
        expect(Array.isArray(c.tags)).toBe(true);
        expect(c.description).toBeDefined();
      }
    });

    it('should filter by category', async () => {
      const concepts = await conceptService.getConcepts({ type: 'category', value: 'Analytics' });
      expect(concepts.length).toBeGreaterThan(0);
      for (const c of concepts) {
        expect(c.category).toBe('Analytics');
      }
    });

    it('should filter by tag', async () => {
      const concepts = await conceptService.getConcepts({ type: 'tag', value: 'revenue' });
      expect(concepts.length).toBeGreaterThan(0);
      for (const c of concepts) {
        expect(c.tags).toContain('revenue');
      }
    });

    it('should return empty array for non-existent category', async () => {
      const concepts = await conceptService.getConcepts({ type: 'category', value: 'NonExistent' });
      expect(concepts).toEqual([]);
    });
  });

  describe('getConceptById', () => {
    it('should return concept for valid id', async () => {
      const concept = await conceptService.getConceptById('customer-lifetime-value');
      expect(concept).not.toBeNull();
      expect(concept?.id).toBe('customer-lifetime-value');
    });

    it('should return null for non-existent id', async () => {
      const concept = await conceptService.getConceptById('nonexistent');
      expect(concept).toBeNull();
    });
  });

  describe('getCategories', () => {
    it('should return non-empty array of categories with counts', async () => {
      const categories = await conceptService.getCategories();
      expect(categories.length).toBeGreaterThan(0);
      for (const cat of categories) {
        expect(cat.name).toBeDefined();
        expect(cat.count).toBeGreaterThan(0);
      }
    });

    it('should be sorted by count descending', async () => {
      const categories = await conceptService.getCategories();
      for (let i = 1; i < categories.length; i++) {
        expect(categories[i]!.count).toBeLessThanOrEqual(categories[i - 1]!.count);
      }
    });
  });

  describe('getTags', () => {
    it('should return non-empty array of tags with counts', async () => {
      const tags = await conceptService.getTags();
      expect(tags.length).toBeGreaterThan(0);
      for (const tag of tags) {
        expect(tag.name).toBeDefined();
        expect(tag.count).toBeGreaterThan(0);
      }
    });

    it('should be sorted by count descending', async () => {
      const tags = await conceptService.getTags();
      for (let i = 1; i < tags.length; i++) {
        expect(tags[i]!.count).toBeLessThanOrEqual(tags[i - 1]!.count);
      }
    });
  });
});
