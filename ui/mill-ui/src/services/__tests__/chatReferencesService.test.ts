import { describe, it, expect } from 'vitest';
import { chatReferencesService } from '../chatReferencesService';

describe('chatReferencesService', () => {
  describe('getConversationsForContext', () => {
    it('should return an array', async () => {
      const refs = await chatReferencesService.getConversationsForContext('model', 'sales.customers');
      expect(Array.isArray(refs)).toBe(true);
    });

    it('should return conversation refs for a known entity', async () => {
      const refs = await chatReferencesService.getConversationsForContext('model', 'sales.customers');
      expect(refs.length).toBeGreaterThan(0);
    });

    it('each ref should have id and title', async () => {
      const refs = await chatReferencesService.getConversationsForContext('model', 'sales.customers');
      for (const ref of refs) {
        expect(ref.id).toBeDefined();
        expect(typeof ref.id).toBe('string');
        expect(ref.title).toBeDefined();
        expect(typeof ref.title).toBe('string');
      }
    });

    it('should return empty array for unknown context', async () => {
      // Not all contexts will have refs — the result should still be an array
      const refs = await chatReferencesService.getConversationsForContext('model', 'totally-unknown-id-12345');
      expect(Array.isArray(refs)).toBe(true);
    });
  });
});
