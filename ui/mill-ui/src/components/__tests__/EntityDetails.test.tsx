import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { EntityDetails } from '../data-model/EntityDetails';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { RelatedContentProvider } from '../../context/RelatedContentContext';
import { defaultFeatureFlags } from '../../features/defaults';
import type { SchemaEntity, EntityFacets } from '../../types/schema';

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

const tableEntity: SchemaEntity = {
  id: 'sales.customers',
  type: 'TABLE',
  name: 'customers',
};

const columnEntity: SchemaEntity = {
  id: 'sales.customers.customer_id',
  type: 'ATTRIBUTE',
  name: 'customer_id',
};

const fullFacets: EntityFacets = {
  descriptive: {
    displayName: 'Customers',
    description: 'Core customer records',
    businessMeaning: 'All registered customers',
    businessDomain: 'Sales',
    businessOwner: 'Sales Team',
    tags: ['core', 'customer'],
    synonyms: ['clients', 'accounts'],
  },
  structural: {
    physicalName: 'customers',
    physicalType: 'TABLE',
    isPrimaryKey: false,
    isForeignKey: false,
    isUnique: false,
    nullable: true,
  },
  relations: [
    {
      id: 'rel-1',
      name: 'customer_orders',
      sourceEntity: 'sales.customers',
      targetEntity: 'sales.orders',
      cardinality: '1:N',
      relationType: 'FOREIGN_KEY',
      description: 'Customer to orders relationship',
    },
  ],
};

const columnFacets: EntityFacets = {
  descriptive: {
    displayName: 'Customer ID',
    description: 'Primary identifier for customers',
  },
  structural: {
    physicalName: 'customer_id',
    physicalType: 'INTEGER',
    isPrimaryKey: true,
    nullable: false,
    isUnique: true,
  },
};

function renderDetails(entity: SchemaEntity = tableEntity, facets: EntityFacets = fullFacets) {
  return render(<EntityDetails entity={entity} facets={facets} />, { wrapper });
}

describe('EntityDetails', () => {
  describe('header', () => {
    it('should display entity display name from facets', () => {
      renderDetails();
      // "Customers" appears in header and possibly in descriptive panel
      const matches = screen.getAllByText('Customers');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });

    it('should display entity name when no displayName in facets', () => {
      renderDetails(tableEntity, {});
      expect(screen.getByText('customers')).toBeInTheDocument();
    });

    it('should show entity type badge', () => {
      renderDetails();
      expect(screen.getByText('Table')).toBeInTheDocument();
    });

    it('should show Column badge for ATTRIBUTE type', () => {
      renderDetails(columnEntity, columnFacets);
      expect(screen.getByText('Column')).toBeInTheDocument();
    });

    it('should display the entity ID in monospace', () => {
      renderDetails();
      // ID appears in header and possibly in relation source/target
      const matches = screen.getAllByText('sales.customers');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('quick badges', () => {
    it('should show PK badge for primary key columns', () => {
      renderDetails(columnEntity, columnFacets);
      expect(screen.getByText('PK')).toBeInTheDocument();
    });

    it('should show Not Null badge when nullable is false', () => {
      renderDetails(columnEntity, columnFacets);
      // Not Null badge appears in both quick badges and structural tab
      const matches = screen.getAllByText('Not Null');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });

    it('should show Unique badge when isUnique is true', () => {
      renderDetails(columnEntity, columnFacets);
      // Unique badge appears in both quick badges and structural tab
      const matches = screen.getAllByText('Unique');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });

    it('should show physical type badge', () => {
      renderDetails(columnEntity, columnFacets);
      // Type badge may appear in quick badges and structural tab
      const matches = screen.getAllByText('INTEGER');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('tabs', () => {
    it('should render Descriptive tab when descriptive facet is present', () => {
      renderDetails();
      expect(screen.getByText('Descriptive')).toBeInTheDocument();
    });

    it('should render Structural tab when structural facet is present', () => {
      renderDetails();
      expect(screen.getByText('Structural')).toBeInTheDocument();
    });

    it('should render Relations tab with count', () => {
      renderDetails();
      expect(screen.getByText('Relations (1)')).toBeInTheDocument();
    });

    it('should show "No detailed information" when there are no facets', () => {
      renderDetails(tableEntity, {});
      expect(screen.getByText('No detailed information available for this entity.')).toBeInTheDocument();
    });
  });

  describe('descriptive tab content', () => {
    it('should display description in the descriptive panel (default active tab)', () => {
      renderDetails();
      expect(screen.getByText('Core customer records')).toBeInTheDocument();
    });

    it('should display business domain', () => {
      renderDetails();
      expect(screen.getByText('Sales')).toBeInTheDocument();
    });

    it('should display business owner', () => {
      renderDetails();
      expect(screen.getByText('Sales Team')).toBeInTheDocument();
    });

    it('should display synonyms', () => {
      renderDetails();
      expect(screen.getByText('clients')).toBeInTheDocument();
      expect(screen.getByText('accounts')).toBeInTheDocument();
    });

    it('should display tags as badges', () => {
      renderDetails();
      expect(screen.getByText('#core')).toBeInTheDocument();
      expect(screen.getByText('#customer')).toBeInTheDocument();
    });
  });
});
