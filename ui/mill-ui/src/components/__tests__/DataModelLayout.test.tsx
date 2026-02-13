import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { defaultFeatureFlags } from '../../features/defaults';

const { testTree } = vi.hoisted(() => ({
  testTree: [
    {
      id: 'sales', type: 'SCHEMA', name: 'sales',
      children: [
        { id: 'sales.customers', type: 'TABLE', name: 'customers', children: [] },
        { id: 'sales.orders', type: 'TABLE', name: 'orders', children: [] },
      ],
    },
    {
      id: 'inventory', type: 'SCHEMA', name: 'inventory',
      children: [
        { id: 'inventory.products', type: 'TABLE', name: 'products', children: [] },
      ],
    },
  ],
}));

vi.mock('../../services/api', () => ({
  schemaService: {
    getTree: vi.fn(() => Promise.resolve(testTree)),
    getEntityById: vi.fn(() => Promise.resolve(null)),
    getEntityFacets: vi.fn(() => Promise.resolve({})),
  },
  chatService: {
    async createChat() { return { chatId: 'mock-id', chatName: 'Mock' }; },
    async *sendMessage() { yield 'mock'; },
  },
  featureService: {
    async getFlags() { return { ...defaultFeatureFlags }; },
  },
  chatReferencesService: { getConversationsForContext: vi.fn(() => Promise.resolve([])) },
}));

async function renderLayout(initialPath = '/model') {
  const { DataModelLayout } = await import('../data-model/DataModelLayout');
  const { FeatureFlagProvider } = await import('../../features/FeatureFlagContext');
  const { InlineChatProvider } = await import('../../context/InlineChatContext');
  const { ChatReferencesProvider } = await import('../../context/ChatReferencesContext');

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

  render(<DataModelLayout />, { wrapper: Wrapper });
}

describe('DataModelLayout', () => {
  it('should render the "Schema Browser" sidebar title', async () => {
    await renderLayout();
    expect(screen.getByText('Schema Browser')).toBeInTheDocument();
  });

  it('should show the empty state when no entity is selected', async () => {
    await renderLayout();
    await waitFor(() => {
      expect(screen.getByText('Data Model Explorer')).toBeInTheDocument();
    });
    expect(screen.getByText(/Select a schema, table, or column/)).toBeInTheDocument();
  });

  it('should render schema tree nodes', async () => {
    await renderLayout();
    await waitFor(() => {
      expect(screen.getByText('sales')).toBeInTheDocument();
    });
  });

  it('should auto-expand schemas to show tables', async () => {
    await renderLayout();
    await waitFor(() => {
      expect(screen.getByText('customers')).toBeInTheDocument();
    });
    expect(screen.getByText('orders')).toBeInTheDocument();
  });

  it('should render inventory schema', async () => {
    await renderLayout();
    await waitFor(() => {
      expect(screen.getByText('inventory')).toBeInTheDocument();
    });
  });
});
