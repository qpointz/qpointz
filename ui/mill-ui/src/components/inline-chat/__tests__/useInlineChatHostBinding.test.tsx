import { describe, expect, it } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { InlineChatProvider, useInlineChat } from '../../../context/InlineChatContext';
import { FeatureFlagContext } from '../../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../../features/defaults';
import { useInlineChatHostBinding } from '../useInlineChatHostBinding';

const inlineChatTestFlags = {
  ...defaultFeatureFlags,
  inlineChatEnabled: true,
};

function wrapper({ children }: { children: ReactNode }) {
  return (
    <FeatureFlagContext.Provider value={inlineChatTestFlags}>
      <InlineChatProvider>{children}</InlineChatProvider>
    </FeatureFlagContext.Provider>
  );
}

function useBindingHarness(
  binding: Parameters<typeof useInlineChatHostBinding>[0],
) {
  const chat = useInlineChat();
  useInlineChatHostBinding(binding);
  return chat;
}

describe('useInlineChatHostBinding', () => {
  it('should activate the bound host session when it exists', () => {
    const { result } = renderHook(
      () =>
        useBindingHarness({
          contextType: 'analysis',
          contextId: 'q1',
        }),
      { wrapper },
    );

    act(() => {
      result.current.startSession('analysis', 'q1', 'Query 1');
      result.current.startSession('analysis', 'q2', 'Query 2');
    });

    expect(result.current.state.isDrawerOpen).toBe(true);
    expect(result.current.activeSession?.contextId).toBe('q1');
  });

  it('should close drawer when host has no session', () => {
    const { result } = renderHook(
      () =>
        useBindingHarness({
          contextType: 'analysis',
          contextId: 'missing',
        }),
      { wrapper },
    );

    act(() => {
      result.current.startSession('analysis', 'q1', 'Query 1');
    });

    expect(result.current.state.isDrawerOpen).toBe(false);
  });

  it('should distinguish sessions by context type and id', () => {
    const { result } = renderHook(
      () =>
        useBindingHarness({
          contextType: 'analysis',
          contextId: 'sales.customers',
        }),
      { wrapper },
    );

    act(() => {
      result.current.startSession('model', 'sales.customers', 'customers');
      result.current.startSession('analysis', 'sales.customers', 'Query');
    });

    expect(result.current.activeSession?.contextType).toBe('analysis');
    expect(result.current.activeSession?.contextLabel).toBe('Query');
  });
});
