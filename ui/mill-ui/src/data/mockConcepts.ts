import type { Concept } from '../types/context';

export const mockConcepts: Concept[] = [
  {
    id: 'customer-lifetime-value',
    name: 'Customer Lifetime Value (CLV)',
    category: 'Analytics',
    tags: ['revenue', 'customer', 'metric'],
    description: 'The total revenue a business can expect from a single customer account throughout their entire relationship. CLV helps businesses make decisions about customer acquisition costs, retention strategies, and segmentation.',
    sql: 'SELECT customer_id, SUM(total_amount) as lifetime_value FROM sales.orders GROUP BY customer_id',
    relatedEntities: ['sales.customers', 'sales.orders', 'analytics.customer_metrics'],
    source: 'MANUAL',
    createdAt: Date.now() - 30 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'average-order-value',
    name: 'Average Order Value (AOV)',
    category: 'Analytics',
    tags: ['revenue', 'orders', 'metric'],
    description: 'The average amount spent each time a customer places an order. Calculated by dividing total revenue by number of orders.',
    sql: 'SELECT AVG(total_amount) as aov FROM sales.orders',
    relatedEntities: ['sales.orders', 'analytics.daily_sales'],
    source: 'MANUAL',
    createdAt: Date.now() - 25 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'customer-segmentation',
    name: 'Customer Segmentation',
    category: 'Customer',
    tags: ['customer', 'classification', 'marketing'],
    description: 'Classification of customers into groups based on shared characteristics such as purchase behavior, demographics, or engagement level. Used for targeted marketing and personalized experiences.',
    relatedEntities: ['sales.customers', 'analytics.customer_metrics'],
    source: 'MANUAL',
    createdAt: Date.now() - 20 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'order-fulfillment-rate',
    name: 'Order Fulfillment Rate',
    category: 'Operations',
    tags: ['orders', 'operations', 'metric'],
    description: 'The percentage of orders that are successfully fulfilled and delivered. A key operational metric for supply chain efficiency.',
    sql: "SELECT COUNT(CASE WHEN status = 'completed' THEN 1 END) * 100.0 / COUNT(*) FROM sales.orders",
    relatedEntities: ['sales.orders', 'sales.order_items'],
    source: 'INFERRED',
    createdAt: Date.now() - 15 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'inventory-turnover',
    name: 'Inventory Turnover',
    category: 'Operations',
    tags: ['inventory', 'operations', 'metric'],
    description: 'How many times inventory is sold and replaced over a period. Higher turnover indicates efficient inventory management.',
    relatedEntities: ['inventory.products', 'sales.order_items'],
    source: 'MANUAL',
    createdAt: Date.now() - 10 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'product-category',
    name: 'Product Category',
    category: 'Product',
    tags: ['product', 'classification', 'catalog'],
    description: 'Hierarchical classification of products for organization, navigation, and reporting purposes.',
    relatedEntities: ['inventory.products'],
    source: 'MANUAL',
    createdAt: Date.now() - 8 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'supplier-performance',
    name: 'Supplier Performance Score',
    category: 'Operations',
    tags: ['supplier', 'operations', 'metric'],
    description: 'Composite score evaluating supplier reliability, quality, and delivery times.',
    relatedEntities: ['inventory.suppliers', 'inventory.products'],
    source: 'INFERRED',
    createdAt: Date.now() - 5 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'revenue-by-segment',
    name: 'Revenue by Customer Segment',
    category: 'Analytics',
    tags: ['revenue', 'customer', 'segmentation', 'report'],
    description: 'Breakdown of total revenue by customer segment (Enterprise, SMB, Consumer) for strategic planning.',
    sql: 'SELECT c.segment, SUM(o.total_amount) as revenue FROM sales.customers c JOIN sales.orders o ON c.customer_id = o.customer_id GROUP BY c.segment',
    relatedEntities: ['sales.customers', 'sales.orders'],
    source: 'MANUAL',
    createdAt: Date.now() - 3 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'churn-risk',
    name: 'Churn Risk Indicator',
    category: 'Customer',
    tags: ['customer', 'retention', 'risk'],
    description: 'Predictive indicator of customer likelihood to stop purchasing. Based on recency, frequency, and monetary value analysis.',
    relatedEntities: ['sales.customers', 'analytics.customer_metrics'],
    source: 'INFERRED',
    createdAt: Date.now() - 1 * 24 * 60 * 60 * 1000,
  },
  {
    id: 'daily-revenue',
    name: 'Daily Revenue',
    category: 'Analytics',
    tags: ['revenue', 'daily', 'metric'],
    description: 'Total revenue generated per day, used for trend analysis and forecasting.',
    sql: 'SELECT DATE(order_date) as date, SUM(total_amount) as revenue FROM sales.orders GROUP BY DATE(order_date)',
    relatedEntities: ['sales.orders', 'analytics.daily_sales'],
    source: 'MANUAL',
    createdAt: Date.now(),
  },
];

export function getConceptById(id: string): Concept | undefined {
  return mockConcepts.find(c => c.id === id);
}

export function getCategories(): { name: string; count: number }[] {
  const categoryMap = new Map<string, number>();
  for (const concept of mockConcepts) {
    categoryMap.set(concept.category, (categoryMap.get(concept.category) || 0) + 1);
  }
  return Array.from(categoryMap.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count);
}

export function getTags(): { name: string; count: number }[] {
  const tagMap = new Map<string, number>();
  for (const concept of mockConcepts) {
    for (const tag of concept.tags) {
      tagMap.set(tag, (tagMap.get(tag) || 0) + 1);
    }
  }
  return Array.from(tagMap.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count);
}

export function filterConcepts(type: 'category' | 'tag' | null, value: string | null): Concept[] {
  if (!type || !value) return mockConcepts;
  
  if (type === 'category') {
    return mockConcepts.filter(c => c.category === value);
  }
  
  if (type === 'tag') {
    return mockConcepts.filter(c => c.tags.includes(value));
  }
  
  return mockConcepts;
}
