import type { ChatReferencesService, ConversationRef } from '../types/chatReferences';

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Deterministic hash to decide which entities have related conversations */
function simpleHash(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash + str.charCodeAt(i)) | 0;
  }
  return Math.abs(hash);
}

const mockConversationPool: ConversationRef[] = [
  { id: 'conv-rev-analysis', title: 'Revenue Analysis' },
  { id: 'conv-cust-segments', title: 'Customer Segmentation Review' },
  { id: 'conv-data-quality', title: 'Data Quality Discussion' },
  { id: 'conv-quarterly-kpi', title: 'Quarterly KPI Deep-dive' },
  { id: 'conv-schema-review', title: 'Schema Design Review' },
  { id: 'conv-perf-tuning', title: 'Query Performance Tuning' },
];

/**
 * Returns a deterministic subset of mock conversations for a given context.
 * Well-known IDs get guaranteed results; others are hash-based (~40% chance).
 */
function getMockRefsForContext(_contextType: string, contextId: string): ConversationRef[] {
  // Well-known IDs that always have related conversations (for demo)
  const guaranteed: Record<string, ConversationRef[]> = {
    'sales.customers': [
      { id: 'conv-cust-segments', title: 'Customer Segmentation Review' },
      { id: 'conv-data-quality', title: 'Data Quality Discussion' },
    ],
    'sales.orders': [
      { id: 'conv-rev-analysis', title: 'Revenue Analysis' },
      { id: 'conv-quarterly-kpi', title: 'Quarterly KPI Deep-dive' },
    ],
    'sales.orders.total_amount': [
      { id: 'conv-rev-analysis', title: 'Revenue Analysis' },
    ],
    'customer-lifetime-value': [
      { id: 'conv-rev-analysis', title: 'Revenue Analysis' },
      { id: 'conv-cust-segments', title: 'Customer Segmentation Review' },
      { id: 'conv-quarterly-kpi', title: 'Quarterly KPI Deep-dive' },
    ],
    'customer-segmentation': [
      { id: 'conv-cust-segments', title: 'Customer Segmentation Review' },
    ],
    'average-order-value': [
      { id: 'conv-rev-analysis', title: 'Revenue Analysis' },
    ],
  };

  if (guaranteed[contextId]) {
    return guaranteed[contextId];
  }

  // Hash-based: ~40% of other IDs get 1-2 refs
  const hash = simpleHash(contextId);
  if (hash % 5 < 2) {
    const count = (hash % 2) + 1;
    const start = hash % mockConversationPool.length;
    const refs: ConversationRef[] = [];
    for (let i = 0; i < count; i++) {
      refs.push(mockConversationPool[(start + i) % mockConversationPool.length]!);
    }
    return refs;
  }

  return [];
}

const mockChatReferencesService: ChatReferencesService = {
  async getConversationsForContext(contextType: string, contextId: string) {
    // Simulate network delay
    await sleep(200 + Math.random() * 300);
    return getMockRefsForContext(contextType, contextId);
  },
};

// When real backend is ready, create realChatReferencesService and change the export below
export const chatReferencesService: ChatReferencesService = mockChatReferencesService;
