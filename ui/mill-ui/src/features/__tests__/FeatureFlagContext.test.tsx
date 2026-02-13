import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MantineProvider } from '@mantine/core';
import { FeatureFlagProvider, useFeatureFlags } from '../FeatureFlagContext';
import { defaultFeatureFlags } from '../defaults';

const mockGetFlags = vi.fn();

vi.mock('../../services/api', () => ({
  featureService: {
    getFlags: () => mockGetFlags(),
  },
}));

beforeEach(() => {
  mockGetFlags.mockReset();
});

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagProvider>{children}</FeatureFlagProvider>
    </MantineProvider>
  );
}

describe('FeatureFlagContext', () => {
  it('should provide default flags immediately', () => {
    mockGetFlags.mockResolvedValue({});
    const { result } = renderHook(() => useFeatureFlags(), { wrapper });
    expect(result.current).toEqual(defaultFeatureFlags);
  });

  it('should merge remote flags with defaults', async () => {
    mockGetFlags.mockResolvedValue({ viewChat: false, viewAdmin: false });
    const { result } = renderHook(() => useFeatureFlags(), { wrapper });

    await waitFor(() => {
      expect(result.current.viewChat).toBe(false);
      expect(result.current.viewAdmin).toBe(false);
    });

    // Other flags should still be defaults
    expect(result.current.viewModel).toBe(defaultFeatureFlags.viewModel);
    expect(result.current.viewKnowledge).toBe(defaultFeatureFlags.viewKnowledge);
  });

  it('should fall back to defaults when backend errors', async () => {
    mockGetFlags.mockRejectedValue(new Error('Network error'));
    const { result } = renderHook(() => useFeatureFlags(), { wrapper });

    // Wait a tick for the error to be caught
    await waitFor(() => {
      expect(result.current).toBeDefined();
    });

    // Flags should be the defaults — no crash
    expect(result.current.viewModel).toBe(defaultFeatureFlags.viewModel);
  });

  it('should ignore unknown keys from backend', async () => {
    mockGetFlags.mockResolvedValue({ unknownFlag: true, viewModel: false });
    const { result } = renderHook(() => useFeatureFlags(), { wrapper });

    await waitFor(() => {
      expect(result.current.viewModel).toBe(false);
    });

    // Unknown flag should not break anything — it'll be in the object but unused
    expect((result.current as Record<string, unknown>)['unknownFlag']).toBe(true);
  });
});
