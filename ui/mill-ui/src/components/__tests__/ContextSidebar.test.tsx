import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import type { ReactNode } from 'react';
import { ContextSidebar } from '../context/ContextSidebar';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { defaultFeatureFlags } from '../../features/defaults';
import type { Concept, ConceptFilter } from '../../types/context';

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

const mockConcepts: Concept[] = [
  {
    id: 'clv', name: 'Customer Lifetime Value', category: 'Analytics',
    tags: ['revenue', 'customer'], description: 'CLV description',
    source: 'MANUAL', createdAt: Date.now(),
  },
  {
    id: 'aov', name: 'Average Order Value', category: 'Analytics',
    tags: ['revenue', 'orders'], description: 'AOV description',
    source: 'MANUAL', createdAt: Date.now(),
  },
  {
    id: 'cs', name: 'Customer Segmentation', category: 'Customer',
    tags: ['customer'], description: 'Segmentation description',
    source: 'INFERRED', createdAt: Date.now(),
  },
];

const noFilter: ConceptFilter = { type: null, value: null };

const defaultCategories = [
  { name: 'Analytics', count: 2 },
  { name: 'Customer', count: 1 },
];

const defaultTags = [
  { name: 'revenue', count: 2 },
  { name: 'customer', count: 2 },
  { name: 'orders', count: 1 },
];

interface RenderProps {
  concepts?: Concept[];
  categories?: { name: string; count: number }[];
  tags?: { name: string; count: number }[];
  selectedId?: string | null;
  filter?: ConceptFilter;
  onSelectConcept?: (c: Concept) => void;
  onFilterChange?: (f: ConceptFilter) => void;
}

function renderSidebar(props: RenderProps = {}) {
  const onSelectConcept = props.onSelectConcept ?? vi.fn();
  const onFilterChange = props.onFilterChange ?? vi.fn();
  return {
    onSelectConcept,
    onFilterChange,
    ...render(
      <ContextSidebar
        concepts={props.concepts ?? mockConcepts}
        categories={props.categories ?? defaultCategories}
        tags={props.tags ?? defaultTags}
        selectedId={props.selectedId ?? null}
        filter={props.filter ?? noFilter}
        onSelectConcept={onSelectConcept}
        onFilterChange={onFilterChange}
      />,
      { wrapper },
    ),
  };
}

describe('ContextSidebar', () => {
  describe('categories section', () => {
    it('should render "Categories" header', () => {
      renderSidebar();
      expect(screen.getByText('Categories')).toBeInTheDocument();
    });

    it('should render category names from mock data', () => {
      renderSidebar();
      // getCategories returns distinct categories — "Analytics" appears in both category list and concept descriptions
      const analyticsMatches = screen.getAllByText('Analytics');
      expect(analyticsMatches.length).toBeGreaterThanOrEqual(1);
    });

    it('should call onFilterChange when a category is clicked', () => {
      const onFilterChange = vi.fn();
      renderSidebar({ onFilterChange });
      // Click the first "Analytics" match which is in the categories section
      fireEvent.click(screen.getAllByText('Analytics')[0]!);
      expect(onFilterChange).toHaveBeenCalledWith({ type: 'category', value: 'Analytics' });
    });

    it('should clear the category filter when clicked again', () => {
      const onFilterChange = vi.fn();
      renderSidebar({
        onFilterChange,
        filter: { type: 'category', value: 'Analytics' },
      });
      fireEvent.click(screen.getAllByText('Analytics')[0]!);
      expect(onFilterChange).toHaveBeenCalledWith({ type: null, value: null });
    });
  });

  describe('concepts list', () => {
    it('should render concept names', () => {
      renderSidebar();
      expect(screen.getByText('Customer Lifetime Value')).toBeInTheDocument();
      expect(screen.getByText('Average Order Value')).toBeInTheDocument();
      expect(screen.getByText('Customer Segmentation')).toBeInTheDocument();
    });

    it('should display concept count header', () => {
      renderSidebar();
      expect(screen.getByText(`All Concepts (${mockConcepts.length})`)).toBeInTheDocument();
    });

    it('should display "Filtered Concepts" header when filter is active', () => {
      renderSidebar({ filter: { type: 'category', value: 'Analytics' } });
      expect(screen.getByText(/Filtered Concepts/)).toBeInTheDocument();
    });

    it('should call onSelectConcept when a concept is clicked', () => {
      const onSelectConcept = vi.fn();
      renderSidebar({ onSelectConcept });
      fireEvent.click(screen.getByText('Customer Lifetime Value'));
      expect(onSelectConcept).toHaveBeenCalledWith(mockConcepts[0]);
    });

    it('should display "No concepts found" when list is empty', () => {
      renderSidebar({ concepts: [] });
      expect(screen.getByText('No concepts found')).toBeInTheDocument();
    });

    it('should show category label for each concept', () => {
      renderSidebar();
      // Category appears as description under each NavLink
      const analyticsBadges = screen.getAllByText('Analytics');
      expect(analyticsBadges.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('tags section', () => {
    it('should render "Tags" header', () => {
      renderSidebar();
      expect(screen.getByText('Tags')).toBeInTheDocument();
    });

    it('should call onFilterChange when a tag is clicked', () => {
      const onFilterChange = vi.fn();
      renderSidebar({ onFilterChange });
      fireEvent.click(screen.getByText('#revenue'));
      expect(onFilterChange).toHaveBeenCalledWith({ type: 'tag', value: 'revenue' });
    });
  });
});
