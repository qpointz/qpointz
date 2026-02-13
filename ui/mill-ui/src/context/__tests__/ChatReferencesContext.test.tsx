import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MantineProvider } from '@mantine/core';
import { ChatReferencesProvider, useChatReferencesContext } from '../ChatReferencesContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import type { ConversationRef } from '../../types/chatReferences';

const mockGetConversationsForContext = vi.fn<(ct: string, ci: string) => Promise<ConversationRef[]>>();

vi.mock('../../services/api', () => ({
  chatReferencesService: {
    getConversationsForContext: (...args: [string, string]) => mockGetConversationsForContext(...args),
  },
  featureService: {
    async getFlags() {
      return {}; // defaults — all flags enabled
    },
  },
}));

beforeEach(() => {
  mockGetConversationsForContext.mockReset();
  mockGetConversationsForContext.mockResolvedValue([]);
});

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagProvider>
        <ChatReferencesProvider>{children}</ChatReferencesProvider>
      </FeatureFlagProvider>
    </MantineProvider>
  );
}

describe('ChatReferencesContext', () => {
  describe('useChatReferencesContext outside provider', () => {
    it('should throw when used outside ChatReferencesProvider', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      expect(() => {
        renderHook(() => useChatReferencesContext());
      }).toThrow('useChatReferencesContext must be used within a ChatReferencesProvider');
      consoleSpy.mockRestore();
    });
  });

  describe('fetchRefsForContext', () => {
    it('should fetch refs and cache them', async () => {
      const mockRefs: ConversationRef[] = [
        { id: 'conv-1', title: 'Revenue Analysis' },
      ];
      mockGetConversationsForContext.mockResolvedValue(mockRefs);

      const { result } = renderHook(() => useChatReferencesContext(), { wrapper });

      act(() => {
        result.current.fetchRefsForContext('model', 'sales.customers');
      });

      await waitFor(() => {
        const refs = result.current.getRefsForContextId('sales.customers');
        expect(refs).toHaveLength(1);
        expect(refs[0]!.title).toBe('Revenue Analysis');
      });
    });

    it('should not duplicate requests for the same context', async () => {
      mockGetConversationsForContext.mockResolvedValue([]);

      const { result } = renderHook(() => useChatReferencesContext(), { wrapper });

      act(() => {
        result.current.fetchRefsForContext('model', 'sales.orders');
        result.current.fetchRefsForContext('model', 'sales.orders');
        result.current.fetchRefsForContext('model', 'sales.orders');
      });

      await waitFor(() => {
        expect(mockGetConversationsForContext).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('prefetchRefs', () => {
    it('should batch-fetch multiple context IDs', async () => {
      mockGetConversationsForContext.mockResolvedValue([]);

      const { result } = renderHook(() => useChatReferencesContext(), { wrapper });

      act(() => {
        result.current.prefetchRefs('model', ['sales.customers', 'sales.orders']);
      });

      await waitFor(() => {
        expect(mockGetConversationsForContext).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('getRefsForContextId', () => {
    it('should return empty array for unfetched context', () => {
      const { result } = renderHook(() => useChatReferencesContext(), { wrapper });
      const refs = result.current.getRefsForContextId('nonexistent');
      expect(refs).toEqual([]);
    });
  });

  describe('error handling', () => {
    it('should cache empty array on fetch error', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      mockGetConversationsForContext.mockRejectedValue(new Error('Network error'));

      const { result } = renderHook(() => useChatReferencesContext(), { wrapper });

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
