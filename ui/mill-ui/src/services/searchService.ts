import type { SearchService, SearchResult } from '../types/search';
import { mockSchemaTree, mockFacets } from '../data/mockSchema';
import { mockConcepts } from '../data/mockConcepts';
import { mockSavedQueries } from '../data/mockQueries';
import type { SchemaEntity } from '../types/schema';

// ---------------------------------------------------------------------------
// Static view definitions (pages reachable via navigation)
// ---------------------------------------------------------------------------

const VIEWS: SearchResult[] = [
  { id: 'view-home',      name: 'Home',      type: 'view', description: 'Overview dashboard',          route: '/home' },
  { id: 'view-model',     name: 'Model',     type: 'view', description: 'Data model explorer',         route: '/model' },
  { id: 'view-knowledge', name: 'Knowledge', type: 'view', description: 'Business concepts & metrics', route: '/knowledge' },
  { id: 'view-analysis',  name: 'Analysis',  type: 'view', description: 'Query playground',             route: '/analysis' },
  { id: 'view-chat',      name: 'Chat',      type: 'view', description: 'General chat',                 route: '/chat' },
  { id: 'view-connect',   name: 'Connect',   type: 'view', description: 'Connection guides',            route: '/connect' },
  { id: 'view-admin',     name: 'Admin',     type: 'view', description: 'Administration panel',         route: '/admin' },
];

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const MAX_RESULTS = 20;

function matches(text: string | undefined | null, query: string): boolean {
  return !!text && text.toLowerCase().includes(query);
}

/** Build a route from a dotted entity id: "sales.customers.email" → "/model/sales/customers/email" */
function entityRoute(id: string): string {
  return '/model/' + id.split('.').join('/');
}

/** Build a breadcrumb from a dotted entity id: "sales.customers.email" → "sales > customers" */
function entityBreadcrumb(id: string): string {
  const parts = id.split('.');
  return parts.slice(0, -1).join(' > ');
}

/** Recursively collect schema entities matching a query */
function searchEntities(
  tree: SchemaEntity[],
  query: string,
  results: SearchResult[],
): void {
  for (const entity of tree) {
    if (results.length >= MAX_RESULTS) return;

    const facets = mockFacets[entity.id];
    const displayName = facets?.descriptive?.displayName ?? entity.name;
    const description = facets?.descriptive?.description;
    const tags = facets?.descriptive?.tags;
    const synonyms = facets?.descriptive?.synonyms;

    const nameMatch = matches(displayName, query) || matches(entity.name, query);
    const descMatch = matches(description, query);
    const tagMatch = tags?.some((t) => matches(t, query)) ?? false;
    const synMatch = synonyms?.some((s) => matches(s, query)) ?? false;

    if (nameMatch || descMatch || tagMatch || synMatch) {
      results.push({
        id: entity.id,
        name: displayName,
        type: entity.type === 'SCHEMA' ? 'schema' : entity.type === 'TABLE' ? 'table' : 'attribute',
        description: description ?? undefined,
        breadcrumb: entityBreadcrumb(entity.id) || undefined,
        route: entityRoute(entity.id),
      });
    }

    if (entity.children) {
      searchEntities(entity.children, query, results);
    }
  }
}

// ---------------------------------------------------------------------------
// Mock implementation
// ---------------------------------------------------------------------------

const mockSearchService: SearchService = {
  async search(query: string): Promise<SearchResult[]> {
    const q = query.trim().toLowerCase();
    if (q.length < 2) return [];

    const results: SearchResult[] = [];

    // 1. Views
    for (const view of VIEWS) {
      if (results.length >= MAX_RESULTS) break;
      if (matches(view.name, q) || matches(view.description, q)) {
        results.push(view);
      }
    }

    // 2. Schema entities (schemas, tables, attributes)
    searchEntities(mockSchemaTree, q, results);

    // 3. Concepts
    for (const concept of mockConcepts) {
      if (results.length >= MAX_RESULTS) break;
      const nameMatch = matches(concept.name, q);
      const descMatch = matches(concept.description, q);
      const catMatch = matches(concept.category, q);
      const tagMatch = concept.tags.some((t) => matches(t, q));

      if (nameMatch || descMatch || catMatch || tagMatch) {
        results.push({
          id: concept.id,
          name: concept.name,
          type: 'concept',
          description: concept.description.length > 80
            ? concept.description.slice(0, 80) + '…'
            : concept.description,
          breadcrumb: concept.category,
          route: `/knowledge/${concept.id}`,
        });
      }
    }

    // 4. Saved queries
    for (const sq of mockSavedQueries) {
      if (results.length >= MAX_RESULTS) break;
      const nameMatch = matches(sq.name, q);
      const descMatch = matches(sq.description, q);
      const tagMatch = sq.tags?.some((t) => matches(t, q)) ?? false;

      if (nameMatch || descMatch || tagMatch) {
        results.push({
          id: sq.id,
          name: sq.name,
          type: 'query',
          description: sq.description ?? undefined,
          route: `/analysis/${sq.id}`,
        });
      }
    }

    return results;
  },
};

// When real backend is ready, create realSearchService and change the export below
export const searchService: SearchService = mockSearchService;
