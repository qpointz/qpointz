import type { RelatedContentService, RelatedContentRef } from '../types/relatedContent';

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Deterministic hash for consistent mock results */
function simpleHash(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash + str.charCodeAt(i)) | 0;
  }
  return Math.abs(hash);
}

// Pool of possible related model refs (full hierarchy: schema → table → column)
const modelRefPool: RelatedContentRef[] = [
  { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
  { id: 'sales.customers', title: 'customers', type: 'model', entityType: 'TABLE' },
  { id: 'sales.customers.customer_id', title: 'customer_id', type: 'model', entityType: 'ATTRIBUTE' },
  { id: 'sales.customers.segment', title: 'segment', type: 'model', entityType: 'ATTRIBUTE' },
  { id: 'sales.orders', title: 'orders', type: 'model', entityType: 'TABLE' },
  { id: 'sales.orders.total_amount', title: 'total_amount', type: 'model', entityType: 'ATTRIBUTE' },
  { id: 'sales.orders.order_date', title: 'order_date', type: 'model', entityType: 'ATTRIBUTE' },
];

// Pool of possible related concept refs
const conceptRefPool: RelatedContentRef[] = [
  { id: 'customer-lifetime-value', title: 'Customer Lifetime Value', type: 'concept' },
  { id: 'customer-segmentation', title: 'Customer Segmentation', type: 'concept' },
  { id: 'average-order-value', title: 'Average Order Value', type: 'concept' },
  { id: 'monthly-active-customers', title: 'Monthly Active Customers', type: 'concept' },
  { id: 'churn-rate', title: 'Churn Rate', type: 'concept' },
];

// Pool of possible related analysis refs
const analysisRefPool: RelatedContentRef[] = [
  { id: 'revenue-by-segment', title: 'Revenue by Segment', type: 'analysis' },
  { id: 'top-customers', title: 'Top Customers', type: 'analysis' },
  { id: 'monthly-trend', title: 'Monthly Revenue Trend', type: 'analysis' },
];

/**
 * Deterministic mock: well-known IDs get guaranteed related content,
 * other IDs get hash-based results (~50% chance).
 */
function getMockRelatedContent(_contextType: string, contextId: string): RelatedContentRef[] {
  // Well-known IDs with guaranteed related content (for demo)
  const guaranteed: Record<string, RelatedContentRef[]> = {
    // Model entities → related concepts + analyses
    'sales.customers': [
      { id: 'customer-lifetime-value', title: 'Customer Lifetime Value', type: 'concept' },
      { id: 'customer-segmentation', title: 'Customer Segmentation', type: 'concept' },
      { id: 'top-customers', title: 'Top Customers', type: 'analysis' },
    ],
    'sales.orders': [
      { id: 'average-order-value', title: 'Average Order Value', type: 'concept' },
      { id: 'revenue-by-segment', title: 'Revenue by Segment', type: 'analysis' },
      { id: 'monthly-trend', title: 'Monthly Revenue Trend', type: 'analysis' },
    ],
    'sales.orders.total_amount': [
      { id: 'customer-lifetime-value', title: 'Customer Lifetime Value', type: 'concept' },
      { id: 'average-order-value', title: 'Average Order Value', type: 'concept' },
    ],
    'sales.customers.segment': [
      { id: 'customer-segmentation', title: 'Customer Segmentation', type: 'concept' },
      { id: 'revenue-by-segment', title: 'Revenue by Segment', type: 'analysis' },
    ],
    // Concepts → related model entities (full hierarchy) + other concepts
    'customer-lifetime-value': [
      { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
      { id: 'sales.customers', title: 'customers', type: 'model', entityType: 'TABLE' },
      { id: 'sales.orders', title: 'orders', type: 'model', entityType: 'TABLE' },
      { id: 'sales.orders.total_amount', title: 'total_amount', type: 'model', entityType: 'ATTRIBUTE' },
      { id: 'customer-segmentation', title: 'Customer Segmentation', type: 'concept' },
      { id: 'churn-rate', title: 'Churn Rate', type: 'concept' },
    ],
    'customer-segmentation': [
      { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
      { id: 'sales.customers', title: 'customers', type: 'model', entityType: 'TABLE' },
      { id: 'sales.customers.segment', title: 'segment', type: 'model', entityType: 'ATTRIBUTE' },
      { id: 'customer-lifetime-value', title: 'Customer Lifetime Value', type: 'concept' },
    ],
    'average-order-value': [
      { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
      { id: 'sales.orders', title: 'orders', type: 'model', entityType: 'TABLE' },
      { id: 'sales.orders.total_amount', title: 'total_amount', type: 'model', entityType: 'ATTRIBUTE' },
      { id: 'revenue-by-segment', title: 'Revenue by Segment', type: 'analysis' },
    ],
  };

  if (guaranteed[contextId]) {
    return guaranteed[contextId];
  }

  // Hash-based: ~50% of other IDs get refs from mixed pools
  const hash = simpleHash(contextId);
  if (hash % 2 === 0) {
    const nonModelPools = [...conceptRefPool, ...analysisRefPool];
    // Exclude self
    const filtered = nonModelPools.filter((r) => r.id !== contextId);
    const count = (hash % 3) + 1;
    const start = hash % filtered.length;
    const refs: RelatedContentRef[] = [];
    for (let i = 0; i < count && i < filtered.length; i++) {
      refs.push(filtered[(start + i) % filtered.length]!);
    }

    // ~60% chance to also include a model hierarchy
    if (hash % 5 < 3) {
      // Pick a table (with its schema ancestor) from the pool
      const tableIdx = hash % 2; // 0 → customers, 1 → orders
      if (tableIdx === 0) {
        refs.push(
          { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
          { id: 'sales.customers', title: 'customers', type: 'model', entityType: 'TABLE' },
          { id: 'sales.customers.customer_id', title: 'customer_id', type: 'model', entityType: 'ATTRIBUTE' },
        );
      } else {
        refs.push(
          { id: 'sales', title: 'sales', type: 'model', entityType: 'SCHEMA' },
          { id: 'sales.orders', title: 'orders', type: 'model', entityType: 'TABLE' },
          { id: 'sales.orders.total_amount', title: 'total_amount', type: 'model', entityType: 'ATTRIBUTE' },
        );
      }
    }

    // Exclude self from final result
    return refs.filter((r) => r.id !== contextId);
  }

  return [];
}

const mockRelatedContentService: RelatedContentService = {
  async getRelatedContent(contextType: string, contextId: string) {
    await sleep(150 + Math.random() * 250);
    return getMockRelatedContent(contextType, contextId);
  },
};

// When real backend is ready, create realRelatedContentService and change the export below
export const relatedContentService: RelatedContentService = mockRelatedContentService;
