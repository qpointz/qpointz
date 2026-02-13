import { describe, it, expect } from 'vitest';
import {
  mockConcepts,
  getConceptById,
  getCategories,
  getTags,
  filterConcepts,
} from '../mockConcepts';

describe('mockConcepts', () => {
  it('should contain at least one concept', () => {
    expect(mockConcepts.length).toBeGreaterThan(0);
  });

  it('should have unique IDs', () => {
    const ids = mockConcepts.map((c) => c.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  it('should have non-empty name, category, and description for every concept', () => {
    for (const concept of mockConcepts) {
      expect(concept.name).toBeTruthy();
      expect(concept.category).toBeTruthy();
      expect(concept.description).toBeTruthy();
    }
  });

  it('should have tags as arrays', () => {
    for (const concept of mockConcepts) {
      expect(Array.isArray(concept.tags)).toBe(true);
      expect(concept.tags.length).toBeGreaterThan(0);
    }
  });
});

describe('getConceptById', () => {
  it('should return a concept by ID', () => {
    const concept = getConceptById('customer-lifetime-value');
    expect(concept).toBeDefined();
    expect(concept!.name).toContain('Lifetime Value');
  });

  it('should return undefined for unknown ID', () => {
    expect(getConceptById('nonexistent')).toBeUndefined();
  });
});

describe('getCategories', () => {
  it('should return category names with counts', () => {
    const categories = getCategories();
    expect(categories.length).toBeGreaterThan(0);

    for (const cat of categories) {
      expect(cat.name).toBeTruthy();
      expect(cat.count).toBeGreaterThan(0);
    }
  });

  it('should have total count matching concepts count', () => {
    const categories = getCategories();
    const total = categories.reduce((sum, c) => sum + c.count, 0);
    expect(total).toBe(mockConcepts.length);
  });
});

describe('getTags', () => {
  it('should return tag names with counts', () => {
    const tags = getTags();
    expect(tags.length).toBeGreaterThan(0);

    for (const tag of tags) {
      expect(tag.name).toBeTruthy();
      expect(tag.count).toBeGreaterThan(0);
    }
  });

  it('should have unique tag names', () => {
    const tags = getTags();
    const names = tags.map((t) => t.name);
    expect(new Set(names).size).toBe(names.length);
  });
});

describe('filterConcepts', () => {
  it('should return all concepts when filter is null', () => {
    const result = filterConcepts(null, null);
    expect(result.length).toBe(mockConcepts.length);
  });

  it('should filter by category', () => {
    const result = filterConcepts('category', 'Analytics');
    expect(result.length).toBeGreaterThan(0);
    for (const concept of result) {
      expect(concept.category).toBe('Analytics');
    }
  });

  it('should filter by tag', () => {
    const result = filterConcepts('tag', 'revenue');
    expect(result.length).toBeGreaterThan(0);
    for (const concept of result) {
      expect(concept.tags).toContain('revenue');
    }
  });

  it('should return empty array for nonexistent category', () => {
    const result = filterConcepts('category', 'NonExistentCategory');
    expect(result).toHaveLength(0);
  });

  it('should return empty array for nonexistent tag', () => {
    const result = filterConcepts('tag', 'nonexistent-tag');
    expect(result).toHaveLength(0);
  });
});
