import { describe, it, expect } from 'vitest';
import {
  isV1MainConversationTextPart,
  summarizeStructuredPartForward,
  V1_CONVERSATION_PRESENTATION,
  V1_TEXT_PART_TYPE,
} from '../chatTransport';

describe('chatTransport', () => {
  describe('isV1MainConversationTextPart', () => {
    it('should accept explicit V1 frozen pair', () => {
      expect(
        isV1MainConversationTextPart({
          presentation: V1_CONVERSATION_PRESENTATION,
          partType: V1_TEXT_PART_TYPE,
        }),
      ).toBe(true);
    });

    it('should default missing fields to V1 frozen pair', () => {
      expect(isV1MainConversationTextPart({})).toBe(true);
    });

    it('should reject structured presentation even when partType defaults to text', () => {
      expect(
        isV1MainConversationTextPart({
          presentation: 'structured',
          partType: 'text',
        }),
      ).toBe(false);
    });

    it('should reject non-text partType on conversation presentation', () => {
      expect(
        isV1MainConversationTextPart({
          presentation: V1_CONVERSATION_PRESENTATION,
          partType: 'sql',
        }),
      ).toBe(false);
    });
  });

  describe('summarizeStructuredPartForward', () => {
    it('should return null when itemId is missing', () => {
      expect(summarizeStructuredPartForward({ presentation: 'sql' })).toBeNull();
    });

    it('should freeze envelope with payload reference', () => {
      const wire = { itemId: 'i-1', presentation: 'structured', partType: 'facet', mode: 'replace' };
      const forward = summarizeStructuredPartForward(wire);
      expect(forward).not.toBeNull();
      expect(forward!.itemId).toBe('i-1');
      expect(forward!.presentation).toBe('structured');
      expect(forward!.partType).toBe('facet');
      expect(forward!.mode).toBe('replace');
      expect(forward!.payload).toBe(wire);
      expect(Object.isFrozen(forward)).toBe(true);
    });
  });
});
