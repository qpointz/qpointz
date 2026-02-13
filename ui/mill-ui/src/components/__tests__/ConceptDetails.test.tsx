import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { ConceptDetails } from '../context/ConceptDetails';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { RelatedContentProvider } from '../../context/RelatedContentContext';
import { defaultFeatureFlags } from '../../features/defaults';
import type { Concept } from '../../types/context';

vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() { return { chatId: 'mock-id', chatName: 'Mock' }; },
    async *sendMessage() { yield 'mock'; },
  },
  featureService: {
    async getFlags() { return { ...defaultFeatureFlags }; },
  },
  chatReferencesService: { async getConversationsForContext() { return []; } },
  relatedContentService: { async getRelatedContent() { return []; } },
}));

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagProvider>
        <InlineChatProvider>
          <ChatReferencesProvider>
            <RelatedContentProvider>
              <MemoryRouter>{children}</MemoryRouter>
            </RelatedContentProvider>
          </ChatReferencesProvider>
        </InlineChatProvider>
      </FeatureFlagProvider>
    </MantineProvider>
  );
}

const baseConcept: Concept = {
  id: 'customer-lifetime-value',
  name: 'Customer Lifetime Value (CLV)',
  category: 'Analytics',
  tags: ['revenue', 'customer', 'metric'],
  description: 'The total revenue a business can expect from a single customer.',
  sql: 'SELECT customer_id, SUM(total_amount) as lifetime_value FROM sales.orders GROUP BY customer_id',
  relatedEntities: ['sales.customers', 'sales.orders'],
  source: 'MANUAL',
  createdAt: Date.now() - 30 * 24 * 60 * 60 * 1000,
};

function renderDetails(concept: Concept = baseConcept) {
  return render(<ConceptDetails concept={concept} />, { wrapper });
}

describe('ConceptDetails', () => {
  describe('header', () => {
    it('should display the concept name', () => {
      renderDetails();
      expect(screen.getByText('Customer Lifetime Value (CLV)')).toBeInTheDocument();
    });

    it('should display the concept category badge', () => {
      renderDetails();
      expect(screen.getByText('Analytics')).toBeInTheDocument();
    });

    it('should display the source badge', () => {
      renderDetails();
      expect(screen.getByText('MANUAL')).toBeInTheDocument();
    });
  });

  describe('description section', () => {
    it('should display the description', () => {
      renderDetails();
      expect(screen.getByText(/total revenue a business can expect/)).toBeInTheDocument();
    });

    it('should show the "Description" label', () => {
      renderDetails();
      expect(screen.getByText('Description')).toBeInTheDocument();
    });
  });

  describe('tags section', () => {
    it('should render tags as badges', () => {
      renderDetails();
      expect(screen.getByText('#revenue')).toBeInTheDocument();
      expect(screen.getByText('#customer')).toBeInTheDocument();
      expect(screen.getByText('#metric')).toBeInTheDocument();
    });

    it('should show "Tags" label', () => {
      renderDetails();
      expect(screen.getByText('Tags')).toBeInTheDocument();
    });
  });

  describe('SQL definition section', () => {
    it('should show "SQL Definition" label when concept has SQL', () => {
      renderDetails();
      expect(screen.getByText('SQL Definition')).toBeInTheDocument();
    });

    it('should not show SQL section when concept has no SQL', () => {
      const noSqlConcept: Concept = { ...baseConcept, sql: undefined };
      renderDetails(noSqlConcept);
      expect(screen.queryByText('SQL Definition')).not.toBeInTheDocument();
    });
  });

  describe('related entities section', () => {
    it('should display related entity names', () => {
      renderDetails();
      expect(screen.getByText('sales.customers')).toBeInTheDocument();
      expect(screen.getByText('sales.orders')).toBeInTheDocument();
    });

    it('should show "Related Entities" label', () => {
      renderDetails();
      expect(screen.getByText('Related Entities')).toBeInTheDocument();
    });

    it('should not show related entities section when empty', () => {
      const noRelated: Concept = { ...baseConcept, relatedEntities: [] };
      renderDetails(noRelated);
      expect(screen.queryByText('Related Entities')).not.toBeInTheDocument();
    });
  });

  describe('metadata section', () => {
    it('should show creation date', () => {
      renderDetails();
      expect(screen.getByText(/Created:/)).toBeInTheDocument();
    });

    it('should not show metadata when createdAt is missing', () => {
      const noDate: Concept = { ...baseConcept, createdAt: undefined };
      renderDetails(noDate);
      expect(screen.queryByText(/Created:/)).not.toBeInTheDocument();
    });
  });
});
