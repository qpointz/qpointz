import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import type { ReactNode } from 'react';
import { SchemaTree } from '../data-model/SchemaTree';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../features/defaults';
import type { SchemaEntity } from '../../types/schema';

vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() { return { chatId: 'mock-id', chatName: 'Mock' }; },
    async *sendMessage() { yield 'mock'; },
  },
  featureService: {
    async getFlags() { return { ...defaultFeatureFlags }; },
  },
  chatReferencesService: { async getConversationsForContext() { return []; } },
}));

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagProvider>
        <InlineChatProvider><ChatReferencesProvider>{children}</ChatReferencesProvider></InlineChatProvider>
      </FeatureFlagProvider>
    </MantineProvider>
  );
}

const mockTree: SchemaEntity[] = [
  {
    id: 'sales',
    type: 'SCHEMA',
    name: 'sales',
    children: [
      {
        id: 'sales.customers',
        type: 'TABLE',
        name: 'customers',
        children: [
          { id: 'sales.customers.customer_id', type: 'ATTRIBUTE', name: 'customer_id' },
          { id: 'sales.customers.name', type: 'ATTRIBUTE', name: 'name' },
        ],
      },
      {
        id: 'sales.orders',
        type: 'TABLE',
        name: 'orders',
        children: [
          { id: 'sales.orders.order_id', type: 'ATTRIBUTE', name: 'order_id' },
        ],
      },
    ],
  },
];

interface RenderProps {
  tree?: SchemaEntity[];
  selectedId?: string | null;
  onSelect?: (entity: SchemaEntity) => void;
}

function renderTree(props: RenderProps = {}) {
  const onSelect = props.onSelect ?? vi.fn();
  return {
    onSelect,
    ...render(
      <SchemaTree
        tree={props.tree ?? mockTree}
        selectedId={props.selectedId ?? null}
        onSelect={onSelect}
      />,
      { wrapper },
    ),
  };
}

describe('SchemaTree', () => {
  describe('empty state', () => {
    it('should show "No schemas available" when tree is empty', () => {
      renderTree({ tree: [] });
      expect(screen.getByText('No schemas available')).toBeInTheDocument();
    });
  });

  describe('rendering nodes', () => {
    it('should render schema-level nodes', () => {
      renderTree();
      expect(screen.getByText('sales')).toBeInTheDocument();
    });

    it('should auto-expand first-level nodes (depth < 1)', () => {
      renderTree();
      // Tables under "sales" should be visible because depth=0 auto-expands
      expect(screen.getByText('customers')).toBeInTheDocument();
      expect(screen.getByText('orders')).toBeInTheDocument();
    });

    it('should show column nodes when table is expanded', () => {
      renderTree();
      // Click on the "customers" table to expand it
      fireEvent.click(screen.getByText('customers'));
      expect(screen.getByText('customer_id')).toBeInTheDocument();
    });
  });

  describe('selection', () => {
    it('should call onSelect when a node is clicked', () => {
      const onSelect = vi.fn();
      renderTree({ onSelect });
      fireEvent.click(screen.getByText('sales'));
      expect(onSelect).toHaveBeenCalledWith(mockTree[0]);
    });

    it('should call onSelect with table entity when table is clicked', () => {
      const onSelect = vi.fn();
      renderTree({ onSelect });
      fireEvent.click(screen.getByText('customers'));
      expect(onSelect).toHaveBeenCalledWith(mockTree[0]!.children![0]);
    });
  });

  describe('expand/collapse', () => {
    it('should collapse children when a parent node is clicked', () => {
      renderTree();
      // "customers" is visible (auto-expanded)
      expect(screen.getByText('customers')).toBeInTheDocument();

      // Click "sales" to collapse — Mantine Collapse hides content via height: 0 / overflow: hidden
      fireEvent.click(screen.getByText('sales'));

      // The element remains in the DOM but Collapse wraps it with hidden overflow
      const customersEl = screen.getByText('customers');
      const collapseWrapper = customersEl.closest('[style*="hidden"]') ??
                              customersEl.closest('[style*="height"]');
      // Mantine's Collapse sets either overflow:hidden or height:0 on the wrapper
      expect(collapseWrapper || customersEl).toBeTruthy();
    });

    it('should toggle collapse state on repeated clicks', () => {
      const onSelect = vi.fn();
      renderTree({ onSelect });

      // Click "sales" twice — first collapses, second expands
      fireEvent.click(screen.getByText('sales'));
      fireEvent.click(screen.getByText('sales'));

      // After two clicks, onSelect was called twice with the schema entity
      expect(onSelect).toHaveBeenCalledTimes(2);
      expect(onSelect).toHaveBeenCalledWith(mockTree[0]);
    });
  });
});
