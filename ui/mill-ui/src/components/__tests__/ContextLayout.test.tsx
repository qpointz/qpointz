import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { ContextLayout } from '../context/ContextLayout';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { defaultFeatureFlags } from '../../features/defaults';

vi.mock('../../services/api', () => ({
  conceptService: {
    getConcepts: vi.fn(async () => (await import('../../data/mockConcepts')).mockConcepts),
    getConceptById: vi.fn(async (id: string) => (await import('../../data/mockConcepts')).getConceptById(id) ?? null),
    getCategories: vi.fn(async () => (await import('../../data/mockConcepts')).getCategories()),
    getTags: vi.fn(async () => (await import('../../data/mockConcepts')).getTags()),
  },
  chatService: {
    async createChat() { return { chatId: 'mock-id', chatName: 'Mock' }; },
    async *sendMessage() { yield 'mock'; },
  },
  featureService: {
    async getFlags() { return { ...defaultFeatureFlags }; },
  },
  chatReferencesService: { async getConversationsForContext() { return []; } },
}));

function renderLayout(initialPath = '/knowledge') {
  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <MantineProvider>
        <FeatureFlagProvider>
          <InlineChatProvider>
            <ChatReferencesProvider>
              <MemoryRouter initialEntries={[initialPath]}>
                {children}
              </MemoryRouter>
            </ChatReferencesProvider>
          </InlineChatProvider>
        </FeatureFlagProvider>
      </MantineProvider>
    );
  }
  return render(<ContextLayout />, { wrapper: Wrapper });
}

describe('ContextLayout', () => {
  it('should render the "Knowledge" sidebar title', () => {
    renderLayout();
    expect(screen.getByText('Knowledge')).toBeInTheDocument();
  });

  it('should show the empty state when no concept is selected', () => {
    renderLayout();
    expect(screen.getByText('Context Explorer')).toBeInTheDocument();
    expect(screen.getByText(/Browse business concepts/)).toBeInTheDocument();
  });

  it('should render concept list in sidebar', async () => {
    renderLayout();
    // From mockConcepts data - loaded asynchronously
    await waitFor(() => {
      expect(screen.getByText('Customer Lifetime Value (CLV)')).toBeInTheDocument();
    });
  });

  it('should render category filters', async () => {
    renderLayout();
    await waitFor(() => {
      expect(screen.getByText('Categories')).toBeInTheDocument();
    });
  });

  it('should render tag filters', async () => {
    renderLayout();
    await waitFor(() => {
      expect(screen.getByText('Tags')).toBeInTheDocument();
    });
  });
});
