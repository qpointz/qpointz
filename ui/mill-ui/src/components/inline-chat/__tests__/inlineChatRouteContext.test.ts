import { describe, expect, it } from 'vitest';
import {
  resolveInlineChatRouteContextId,
  resolveInlineChatRouteContextType,
} from '../inlineChatRouteContext';

describe('inlineChatRouteContext', () => {
  describe('resolveInlineChatRouteContextType', () => {
    it('should map inline-capable routes to context types', () => {
      expect(resolveInlineChatRouteContextType('/analysis')).toBe('analysis');
      expect(resolveInlineChatRouteContextType('/analysis/q1')).toBe('analysis');
      expect(resolveInlineChatRouteContextType('/model')).toBe('model');
      expect(resolveInlineChatRouteContextType('/model/sales/customers')).toBe('model');
      expect(resolveInlineChatRouteContextType('/knowledge')).toBe('knowledge');
      expect(resolveInlineChatRouteContextType('/knowledge/clv')).toBe('knowledge');
    });

    it('should return null for non-inline routes', () => {
      expect(resolveInlineChatRouteContextType('/chat')).toBeNull();
      expect(resolveInlineChatRouteContextType('/overview')).toBeNull();
    });
  });

  describe('resolveInlineChatRouteContextId', () => {
    it('should resolve analysis query ids from the path', () => {
      expect(resolveInlineChatRouteContextId('/analysis')).toBe('__analysis__');
      expect(resolveInlineChatRouteContextId('/analysis/top-orders')).toBe('top-orders');
    });

    it('should join model path segments with dots', () => {
      expect(resolveInlineChatRouteContextId('/model')).toBe('__model__');
      expect(resolveInlineChatRouteContextId('/model/sales/customers')).toBe('sales.customers');
    });

    it('should resolve knowledge concept ids', () => {
      expect(resolveInlineChatRouteContextId('/knowledge')).toBe('__knowledge__');
      expect(resolveInlineChatRouteContextId('/knowledge/customer-lifetime-value')).toBe(
        'customer-lifetime-value',
      );
    });
  });
});
