import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter, Route, Routes, createMemoryRouter, RouterProvider } from 'react-router';
import type { ReactNode } from 'react';
import { defaultFeatureFlags } from '../../features/defaults';
import type { FacetResolvedRow, SchemaEntity } from '../../types/schema';

const { testTree } = vi.hoisted(() => ({
  testTree: [
    {
      id: 'model-entity',
      type: 'MODEL' as const,
      name: 'Model',
      children: [
        {
          id: 'sales', type: 'SCHEMA' as const, name: 'sales',
          children: [
            { id: 'sales.customers', type: 'TABLE' as const, name: 'customers', children: [] },
            { id: 'sales.orders', type: 'TABLE' as const, name: 'orders', children: [] },
          ],
        },
        {
          id: 'inventory', type: 'SCHEMA' as const, name: 'inventory',
          children: [
            { id: 'inventory.products', type: 'TABLE' as const, name: 'products', children: [] },
          ],
        },
      ],
    },
  ],
}));

vi.mock('../../services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../services/api')>();
  return {
    ...actual,
    schemaService: {
      ...actual.schemaService,
      getContext: vi.fn(() => Promise.resolve({
        selectedContext: 'global',
        availableScopes: [{ id: 'global', slug: 'global', displayName: 'Global' }],
      })),
      getTree: vi.fn(() => Promise.resolve(testTree)),
      getEntityById: vi.fn(() => Promise.resolve(null)),
      getTable: vi.fn(() => Promise.resolve(null)),
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
  };
});

async function renderLayout(initialPath = '/model') {
  const { DataModelLayout } = await import('../data-model/DataModelLayout');
  const { FeatureFlagProvider } = await import('../../features/FeatureFlagContext');
  const { InlineChatProvider } = await import('../../context/InlineChatContext');
  const { ChatReferencesProvider } = await import('../../context/ChatReferencesContext');
  const { RelatedContentProvider } = await import('../../context/RelatedContentContext');

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <MantineProvider>
        <FeatureFlagProvider>
          <InlineChatProvider>
            <ChatReferencesProvider>
              <RelatedContentProvider>
                <MemoryRouter initialEntries={[initialPath]}>
                  <Routes>
                    <Route path="/model/:schema?/:table?/:attribute?" element={children} />
                  </Routes>
                </MemoryRouter>
              </RelatedContentProvider>
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

  it('should load tree with global scope by default', async () => {
    const { schemaService } = await import('../../services/api');
    await renderLayout();
    await waitFor(() => {
      expect(schemaService.getTree).toHaveBeenCalledWith('global');
    });
  });

  it('should show scope picker when URL declares multiple scopes', async () => {
    await renderLayout('/model?scope=global,chat-test');
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Scopes (2)' })).toBeInTheDocument();
    });
  });

  it('should always show scope picker including for default global-only navigation', async () => {
    await renderLayout('/model');
    await waitFor(() => {
      expect(screen.getByText('sales')).toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: 'Scopes (1)' })).toBeInTheDocument();
  });

  it('should pass comma-separated scope to tree when URL declares chat scope', async () => {
    const { schemaService } = await import('../../services/api');
    await renderLayout('/model?scope=global,chat-abc');
    await waitFor(() => {
      expect(schemaService.getTree).toHaveBeenCalledWith('global,chat-abc');
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

  it('should enrich table columns when deep-linked to a column after the tree is ready', async () => {
    const { schemaService } = await import('../../services/api');
    const columnEntity = {
      id: 'sales.customers.customer_id',
      entityType: 'COLUMN' as const,
      schemaName: 'sales',
      tableName: 'customers',
      columnName: 'customer_id',
      fieldIndex: 0,
      type: { type: 'VARCHAR', nullable: true },
    };
    const tableDetail = {
      id: 'sales.customers',
      entityType: 'TABLE' as const,
      schemaName: 'sales',
      tableName: 'customers',
      tableType: 'TABLE',
      columns: [columnEntity],
    };

    let resolveEntity: (entity: typeof columnEntity) => void = () => {};
    vi.mocked(schemaService.getEntityById).mockImplementation(
      () => new Promise((resolve) => { resolveEntity = resolve; }),
    );
    vi.mocked(schemaService.getTable).mockResolvedValue(tableDetail);

    await renderLayout('/model/sales/customers/customer_id');

    await waitFor(() => {
      expect(screen.getByText('customers')).toBeInTheDocument();
      expect(vi.mocked(schemaService.getTable)).toHaveBeenCalledWith(
        'sales',
        'customers',
        'global',
        'none',
      );
      expect(screen.getAllByText('customer_id').length).toBeGreaterThanOrEqual(1);
    });

    await act(async () => {
      resolveEntity(columnEntity);
    });
  });

  it('should not refetch the explorer tree when selecting a table in the tree', async () => {
    const { schemaService } = await import('../../services/api');
    const tableEntity = {
      id: 'sales.customers',
      entityType: 'TABLE' as const,
      schemaName: 'sales',
      tableName: 'customers',
      tableType: 'TABLE',
      columns: [
        {
          id: 'sales.customers.customer_id',
          entityType: 'COLUMN' as const,
          schemaName: 'sales',
          tableName: 'customers',
          columnName: 'customer_id',
          fieldIndex: 0,
          type: { type: 'VARCHAR', nullable: true },
        },
      ],
    };

    vi.mocked(schemaService.getEntityById).mockImplementation((id) => {
      if (id === 'sales.customers') {
        return Promise.resolve(tableEntity);
      }
      return Promise.resolve(null);
    });

    await renderLayout();

    await waitFor(() => {
      expect(screen.getByText('customers')).toBeInTheDocument();
    });

    const getTreeCallsBefore = vi.mocked(schemaService.getTree).mock.calls.length;

    await act(async () => {
      screen.getByText('customers').click();
    });

    await waitFor(() => {
      expect(schemaService.getEntityById).toHaveBeenCalledWith('sales.customers', 'global');
    });

    expect(vi.mocked(schemaService.getTree).mock.calls.length).toBe(getTreeCallsBefore);
    expect(screen.queryByText('Loading model...')).not.toBeInTheDocument();
  });

  it('should enrich table columns when deep-linked to a column before the tree finishes loading', async () => {
    const { schemaService } = await import('../../services/api');
    const columnEntity = {
      id: 'sales.customers.customer_id',
      entityType: 'COLUMN' as const,
      schemaName: 'sales',
      tableName: 'customers',
      columnName: 'customer_id',
      fieldIndex: 0,
      type: { type: 'VARCHAR', nullable: true },
    };
    const tableDetail = {
      id: 'sales.customers',
      entityType: 'TABLE' as const,
      schemaName: 'sales',
      tableName: 'customers',
      tableType: 'TABLE',
      columns: [columnEntity],
    };

    let resolveTree: (tree: typeof testTree) => void = () => {};
    vi.mocked(schemaService.getTree).mockImplementation(
      () => new Promise((resolve) => { resolveTree = resolve; }),
    );
    vi.mocked(schemaService.getEntityById).mockImplementation((id) => {
      if (id === 'sales.customers.customer_id') {
        return Promise.resolve(columnEntity);
      }
      return Promise.resolve(null);
    });
    vi.mocked(schemaService.getTable).mockResolvedValue(tableDetail);

    await renderLayout('/model/sales/customers/customer_id');

    await waitFor(() => {
      expect(schemaService.getEntityById).toHaveBeenCalledWith('sales.customers.customer_id', 'global', expect.any(AbortSignal));
    });

    await act(async () => {
      resolveTree(testTree);
    });

    await waitFor(() => {
      expect(vi.mocked(schemaService.getTable)).toHaveBeenCalledWith(
        'sales',
        'customers',
        'global',
        'none',
      );
      expect(screen.getAllByText('customer_id').length).toBeGreaterThanOrEqual(1);
    });
  });

  it('should refetch entity when read scopes are cleared and re-enabled', async () => {
    const { schemaService } = await import('../../services/api');
    const conceptFacet: FacetResolvedRow = {
      uid: 'concept-1',
      facetTypeUrn: 'urn:mill/metadata/facet-type:concept',
      origin: 'CAPTURED',
      scopeUrn: 'urn:mill/metadata/scope:chat-abc',
      originId: 'concept-1',
      payload: { name: 'Test concept' },
    };
    const modelEntity: SchemaEntity = {
      id: 'model-entity',
      entityType: 'MODEL' as const,
      schemaName: '' as const,
      metadataEntityId: 'urn:mill:metadata:entity:model',
      facetsResolved: [conceptFacet],
    };

    vi.mocked(schemaService.getEntityById).mockImplementation((_id, scope) => {
      if (!scope) {
        return Promise.resolve(null);
      }
      return Promise.resolve(modelEntity);
    });

    const { DataModelLayout } = await import('../data-model/DataModelLayout');
    const { FeatureFlagProvider } = await import('../../features/FeatureFlagContext');
    const { InlineChatProvider } = await import('../../context/InlineChatContext');
    const { ChatReferencesProvider } = await import('../../context/ChatReferencesContext');
    const { RelatedContentProvider } = await import('../../context/RelatedContentContext');

    const router = createMemoryRouter(
      [{ path: '/model/:schema?/:table?/:attribute?', element: <DataModelLayout /> }],
      { initialEntries: ['/model/model-entity?scope=global,chat-abc'] },
    );

    render(
      <MantineProvider>
        <FeatureFlagProvider>
          <InlineChatProvider>
            <ChatReferencesProvider>
              <RelatedContentProvider>
                <RouterProvider router={router} />
              </RelatedContentProvider>
            </ChatReferencesProvider>
          </InlineChatProvider>
        </FeatureFlagProvider>
      </MantineProvider>,
    );

    await waitFor(() => {
      expect(schemaService.getEntityById).toHaveBeenCalledWith(
        'model-entity',
        'global,chat-abc',
        expect.any(AbortSignal),
      );
    });

    const callsBefore = vi.mocked(schemaService.getEntityById).mock.calls.length;

    await act(async () => {
      await router.navigate('/model/model-entity?scope=global,chat-abc&readScope=none');
    });

    await waitFor(() => {
      expect(screen.getByText('No scopes selected')).toBeInTheDocument();
    });

    await act(async () => {
      await router.navigate('/model/model-entity?scope=global,chat-abc&readScope=chat-abc');
    });

    await waitFor(() => {
      const calls = vi.mocked(schemaService.getEntityById).mock.calls;
      expect(calls.length).toBeGreaterThan(callsBefore);
      expect(calls[calls.length - 1]?.[1]).toBe('chat-abc');
    });
  });
});
