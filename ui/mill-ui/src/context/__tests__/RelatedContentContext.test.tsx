import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MantineProvider } from '@mantine/core';
import { RelatedContentProvider, useRelatedContentContext } from '../RelatedContentContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import type { RelatedContentRef } from '../../types/relatedContent';

const mockGetRelatedContent = vi.fn<(ct: string, ci: string) => Promise<RelatedContentRef[]>>();

vi.mock('../../services/api', () => ({
  relatedContentService: {
    getRelatedContent: (...args: [string, string]) => mockGetRelatedContent(...args),
  },
  featureService: {
    async getFlags() {
      return {}; // defaults — all flags enabled
    },
  },
}));

beforeEach(() => {
  mockGetRelatedContent.mockReset();
  mockGetRelatedContent.mockResolvedValue([]);
});

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagProvider>
        <RelatedContentProvider>{children}</RelatedContentProvider>
      </FeatureFlagProvider>
    </MantineProvider>
  );
}

describe('RelatedContentContext', () => {
  describe('useRelatedContentContext outside provider', () => {
    it('should throw when used outside RelatedContentProvider', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      expect(() => {
        renderHook(() => useRelatedContentContext());
      }).toThrow('useRelatedContentContext must be used within a RelatedContentProvider');
      consoleSpy.mockRestore();
    });
  });

  describe('fetchRefsForContext', () => {
    it('should fetch refs and cache them', async () => {
      const mockRefs: RelatedContentRef[] = [
        { id: 'sales.orders', title: 'Orders', type: 'model' },
      ];
      mockGetRelatedContent.mockResolvedValue(mockRefs);

      const { result } = renderHook(() => useRelatedContentContext(), { wrapper });

      act(() => {
        result.current.fetchRefsForContext('model', 'sales.customers');
      });

      await waitFor(() => {
        const refs = result.current.getRefsForContextId('sales.customers');
        expect(refs).toHaveLength(1);
        expect(refs[0]!.title).toBe('Orders');
      });
    });

    it('should not duplicate requests for the same context', async () => {
      mockGetRelatedContent.mockResolvedValue([]);

      const { result } = renderHook(() => useRelatedContentContext(), { wrapper });

      act(() => {
        result.current.fetchRefsForContext('model', 'sales.orders');
        result.current.fetchRefsForContext('model', 'sales.orders');
      });

      await waitFor(() => {
        expect(mockGetRelatedContent).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('prefetchRefs', () => {
    it('should batch-fetch multiple context IDs', async () => {
      mockGetRelatedContent.mockResolvedValue([]);

      const { result } = renderHook(() => useRelatedContentContext(), { wrapper });

      act(() => {
        result.current.prefetchRefs('model', ['sales.customers', 'sales.orders']);
      });

      await waitFor(() => {
        expect(mockGetRelatedContent).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('getRefsForContextId', () => {
    it('should return empty array for unfetched context', () => {
      const { result } = renderHook(() => useRelatedContentContext(), { wrapper });
      const refs = result.current.getRefsForContextId('nonexistent');
      expect(refs).toEqual([]);
    });
  });

  describe('error handling', () => {
    it('should cache empty array on fetch error', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      mockGetRelatedContent.mockRejectedValue(new Error('Network error'));

      const { result } = renderHook(() => useRelatedContentContext(), { wrapper });

      act(() => {
        result.current.fetchRefsForContext('model', 'sales.customers');
      });

      await waitFor(() => {
        const refs = result.current.getRefsForContextId('sales.customers');
        expect(refs).toEqual([]);
      });

      consoleSpy.mockRestore();
    });
  });
});
