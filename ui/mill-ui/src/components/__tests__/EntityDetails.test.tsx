import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import type { ReactNode } from 'react';
import { EntityDetails } from '../data-model/EntityDetails';
import { FeatureFlagContext } from '../../features/FeatureFlagContext';
import { InlineChatProvider } from '../../context/InlineChatContext';
import { ChatReferencesProvider } from '../../context/ChatReferencesContext';
import { RelatedContentProvider } from '../../context/RelatedContentContext';
import { defaultFeatureFlags } from '../../features/defaults';
import type { SchemaEntity, EntityFacets, RelationFacet } from '../../types/schema';
import type { FacetTypeManifest } from '../../types/facetTypes';
import { facetTypeService } from '../../services/api';

const { modelViewFacetManifests } = vi.hoisted(() => {
  const str = (name: string, title: string) => ({
    name,
    schema: { type: 'STRING' as const, title, description: '' },
  });
  const manifests: FacetTypeManifest[] = [
    {
      typeKey: 'urn:mill/metadata/facet-type:descriptive',
      title: 'Descriptive',
      description: '',
      enabled: true,
      mandatory: false,
      targetCardinality: 'SINGLE',
      category: 'general',
      payload: {
        type: 'OBJECT',
        title: 'Descriptive',
        description: '',
        fields: [
          str('displayName', 'Display name'),
          str('description', 'Description'),
          str('businessMeaning', 'Business meaning'),
          str('businessDomain', 'Business domain'),
          str('businessOwner', 'Business owner'),
          {
            name: 'tags',
            schema: {
              type: 'ARRAY',
              title: 'Tags',
              description: '',
              items: { type: 'STRING', title: 'Tag', description: '' },
            },
          },
          {
            name: 'synonyms',
            schema: {
              type: 'ARRAY',
              title: 'Synonyms',
              description: '',
              items: { type: 'STRING', title: 'Synonym', description: '' },
            },
          },
        ],
      },
    },
    {
      typeKey: 'urn:mill/metadata/facet-type:structural',
      title: 'Structural',
      description: '',
      enabled: true,
      mandatory: false,
      targetCardinality: 'SINGLE',
      category: 'general',
      payload: {
        type: 'OBJECT',
        title: 'Structural',
        description: '',
        fields: [str('physicalName', 'Physical name'), str('type', 'Type')],
      },
    },
    {
      typeKey: 'urn:mill/metadata/facet-type:relation',
      title: 'Relations',
      description: '',
      enabled: true,
      mandatory: false,
      targetCardinality: 'MULTIPLE',
      category: 'general',
      payload: {
        type: 'OBJECT',
        title: 'Relation',
        description: '',
        fields: [
          str('id', 'Id'),
          str('name', 'Name'),
          str('sourceEntity', 'Source'),
          str('targetEntity', 'Target'),
          str('cardinality', 'Cardinality'),
          str('relationType', 'Relation type'),
          str('description', 'Description'),
        ],
      },
    },
  ];
  return { modelViewFacetManifests: manifests };
});

vi.mock('../../services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../services/api')>();
  return {
    ...actual,
    chatService: {
      async createChat() {
        return { chatId: 'mock-id', chatName: 'Mock' };
      },
      async *sendMessage() {
        yield 'mock';
      },
    },
    featureService: {
      async getFlags() {
        return { ...defaultFeatureFlags };
      },
    },
    chatReferencesService: { async getConversationsForContext() { return []; } },
    relatedContentService: { async getRelatedContent() { return []; } },
    facetTypeService: {
      ...actual.facetTypeService,
      list: vi.fn().mockResolvedValue(modelViewFacetManifests),
    },
  };
});

/** Synchronous flags — avoids async merge race where tests assert before `modelQuickBadges` applies */
const entityDetailsFeatureFlags = { ...defaultFeatureFlags, modelQuickBadges: true };

function wrapper({ children }: { children: ReactNode }) {
  return (
    <MantineProvider>
      <FeatureFlagContext.Provider value={entityDetailsFeatureFlags}>
        <InlineChatProvider>
          <ChatReferencesProvider>
            <RelatedContentProvider>
              <MemoryRouter>{children}</MemoryRouter>
            </RelatedContentProvider>
          </ChatReferencesProvider>
        </InlineChatProvider>
      </FeatureFlagContext.Provider>
    </MantineProvider>
  );
}

const tableEntity: SchemaEntity = {
  id: 'sales.customers',
  entityType: 'TABLE',
  schemaName: 'sales',
  tableName: 'customers',
  tableType: 'TABLE',
  columns: [],
};

const columnEntity: SchemaEntity = {
  id: 'sales.customers.customer_id',
  entityType: 'COLUMN',
  schemaName: 'sales',
  tableName: 'customers',
  columnName: 'customer_id',
  fieldIndex: 0,
  type: { type: 'INTEGER', nullable: false },
};

const descriptivePayload = {
  displayName: 'Customers',
  description: 'Core customer records',
  businessMeaning: 'All registered customers',
  businessDomain: 'Sales',
  businessOwner: 'Sales Team',
  tags: ['core', 'customer'],
  synonyms: ['clients', 'accounts'],
};

const structuralTablePayload = {
  physicalName: 'customers',
  type: 'TABLE',
  isPrimaryKey: false,
  isForeignKey: false,
  isUnique: false,
  nullable: true,
};

const relationRows: RelationFacet[] = [
  {
    id: 'rel-1',
    name: 'customer_orders',
    sourceEntity: 'sales.customers',
    targetEntity: 'sales.orders',
    cardinality: '1:N',
    relationType: 'FOREIGN_KEY',
    description: 'Customer to orders relationship',
  },
];

const fullFacets: EntityFacets = {
  descriptive: descriptivePayload,
  structural: structuralTablePayload,
  relations: relationRows,
  byType: {
    'urn:mill/metadata/facet-type:descriptive': descriptivePayload,
    'urn:mill/metadata/facet-type:structural': structuralTablePayload,
    'urn:mill/metadata/facet-type:relation': relationRows,
  },
};

const columnDescriptive = {
  displayName: 'Customer ID',
  description: 'Primary identifier for customers',
};

const columnStructural = {
  physicalName: 'customer_id',
  type: 'INTEGER',
  isPrimaryKey: true,
  nullable: false,
  isUnique: true,
};

const columnFacets: EntityFacets = {
  descriptive: columnDescriptive,
  structural: columnStructural,
  byType: {
    'urn:mill/metadata/facet-type:descriptive': columnDescriptive,
    'urn:mill/metadata/facet-type:structural': columnStructural,
  },
};

function renderDetails(entity: SchemaEntity = tableEntity, facets: EntityFacets = fullFacets) {
  return render(<EntityDetails entity={entity} facets={facets} />, { wrapper });
}

describe('EntityDetails', () => {
  describe('header', () => {
    it('should display entity display name from facets', () => {
      renderDetails();
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

    it('should show Column badge for COLUMN type', () => {
      renderDetails(columnEntity, columnFacets);
      expect(screen.getByText('Column')).toBeInTheDocument();
    });

    it('should display the metadata facet target id in monospace', () => {
      renderDetails();
      const urn = 'urn:mill/model/table:sales.customers';
      const matches = screen.getAllByText(urn);
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
      const matches = screen.getAllByText('Not Null');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });

    it('should show Unique badge when isUnique is true', () => {
      renderDetails(columnEntity, columnFacets);
      const matches = screen.getAllByText('Unique');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });

    it('should show physical type badge', () => {
      renderDetails(columnEntity, columnFacets);
      const matches = screen.getAllByText('INTEGER');
      expect(matches.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('category tabs and facet boxes', () => {
    it('should render General category tab when facets are present', () => {
      renderDetails();
      expect(screen.getByRole('tab', { name: 'General' })).toBeInTheDocument();
    });

    it('should render facet box titles from descriptors', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('Descriptive')).toBeInTheDocument();
        expect(screen.getByText('Structural')).toBeInTheDocument();
        // Relation MULTIPLE cards use "{title} · {instance caption}" (registry title is often
        // "Relations"; payload fallback title is "Relation").
        expect(screen.getByText(/^(Relations|Relation) · customer_orders$/)).toBeInTheDocument();
      });
    });

    it('should show empty state when there are no facets', () => {
      renderDetails(tableEntity, {});
      expect(screen.getByText('No metadata facets available for this entity yet.')).toBeInTheDocument();
    });
  });

  describe('standard descriptor read view', () => {
    it('should display description from descriptive facet payload', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('Core customer records')).toBeInTheDocument();
      });
    });

    it('should display business domain', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('Sales')).toBeInTheDocument();
      });
    });

    it('should display business owner', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('Sales Team')).toBeInTheDocument();
      });
    });

    it('should display synonyms', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('clients')).toBeInTheDocument();
        expect(screen.getByText('accounts')).toBeInTheDocument();
      });
    });

    it('should display tag values from standard array renderer', async () => {
      renderDetails();
      await waitFor(() => {
        expect(screen.getByText('core')).toBeInTheDocument();
        expect(screen.getByText('customer')).toBeInTheDocument();
      });
    });

    it('should surface relation instance data (read view or JSON fallback)', async () => {
      renderDetails();
      await waitFor(() => {
        const textMatch = screen.queryByText('customer_orders');
        const inTextarea = screen
          .queryAllByRole('textbox')
          .some((el) => (el as HTMLTextAreaElement).value.includes('customer_orders'));
        expect(textMatch ?? inTextarea).toBeTruthy();
        const ordersMatch =
          screen.queryByText('sales.orders') ||
          screen.queryAllByRole('textbox').some((el) => (el as HTMLTextAreaElement).value.includes('sales.orders'));
        expect(ordersMatch).toBeTruthy();
      }, { timeout: 5000 });
    });
  });

  describe('MULTIPLE cardinality (WI-109)', () => {
    it('should show Add entry for empty MULTIPLE relation facet, not a single generic JSON card', async () => {
      const emptyRelationFacets: EntityFacets = {
        byType: {
          'urn:mill/metadata/facet-type:relation': [],
        },
      };
      renderDetails(tableEntity, emptyRelationFacets);
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add entry/i })).toBeInTheDocument();
        expect(screen.getByText(/no entries for this facet yet/i)).toBeInTheDocument();
      });
    });

    it('should show Add entry when envelope has empty relations array (zero instances)', async () => {
      const emptyEnvelope: EntityFacets = {
        byType: {
          'urn:mill/metadata/facet-type:relation': { relations: [] },
        },
      };
      renderDetails(tableEntity, emptyEnvelope);
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add entry/i })).toBeInTheDocument();
        expect(screen.getByText(/no entries for this facet yet/i)).toBeInTheDocument();
      });
    });

    it('should render MULTIPLE rows when manifest has no payload schema (JSON read-only per row)', async () => {
      const manifestsNoPayload: FacetTypeManifest[] = modelViewFacetManifests.map((m) => {
        if (m.typeKey !== 'urn:mill/metadata/facet-type:relation') return m;
        return {
          typeKey: m.typeKey,
          title: m.title,
          description: m.description,
          enabled: m.enabled,
          mandatory: m.mandatory,
          targetCardinality: 'MULTIPLE',
          category: m.category,
        } as unknown as FacetTypeManifest;
      });
      vi.mocked(facetTypeService.list).mockResolvedValueOnce(manifestsNoPayload);

      const rawRelationFacets: EntityFacets = {
        byType: {
          'urn:mill/metadata/facet-type:relation': [
            { name: 'rel-a', sourceEntity: 's.t1', targetEntity: 's.t2' },
          ],
        },
      };
      renderDetails(tableEntity, rawRelationFacets);
      await waitFor(() => {
        expect(screen.getByText(/rel-a/)).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /add entry/i })).not.toBeInTheDocument();
      });
    });

    it('should render two MULTIPLE instance cards when byType holds an array of two payloads', async () => {
      const twoRelations: EntityFacets = {
        byType: {
          'urn:mill/metadata/facet-type:relation': [
            { name: 'first', sourceEntity: 'a', targetEntity: 'b' },
            { name: 'second', sourceEntity: 'c', targetEntity: 'd' },
          ],
        },
      };
      renderDetails(tableEntity, twoRelations);
      await waitFor(() => {
        expect(screen.getByText(/first/)).toBeInTheDocument();
        expect(screen.getByText(/second/)).toBeInTheDocument();
      });
    });

    it('should treat sole {} relation payload as one instance (one JPA row / one FacetInstanceDto)', async () => {
      const soleEmptyObject: EntityFacets = {
        byType: {
          'urn:mill/metadata/facet-type:relation': {},
        },
      };
      renderDetails(tableEntity, soleEmptyObject);
      await waitFor(() => {
        expect(screen.queryByText(/entry 1 of 1/i)).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /add entry/i })).not.toBeInTheDocument();
      });
    });
  });
});
