import type { SavedQuery, QueryResult } from '../types/query';

export const mockSavedQueries: SavedQuery[] = [
  {
    id: 'top-customers',
    name: 'Top Customers by Revenue',
    description: 'Customers ranked by total order value',
    sql: `SELECT
  c.customer_id,
  c.name,
  c.segment,
  COUNT(o.order_id) AS total_orders,
  SUM(o.total_amount) AS total_revenue
FROM sales.customers c
JOIN sales.orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name, c.segment
ORDER BY total_revenue DESC
LIMIT 10`,
    createdAt: Date.now() - 7 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 2 * 24 * 60 * 60 * 1000,
    tags: ['revenue', 'customer'],
  },
  {
    id: 'daily-revenue',
    name: 'Daily Revenue Summary',
    description: 'Revenue and order count aggregated by day',
    sql: `SELECT
  DATE(order_date) AS date,
  COUNT(*) AS order_count,
  SUM(total_amount) AS revenue,
  AVG(total_amount) AS avg_order_value
FROM sales.orders
WHERE order_date >= '2025-01-01'
GROUP BY DATE(order_date)
ORDER BY date DESC`,
    createdAt: Date.now() - 5 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 1 * 24 * 60 * 60 * 1000,
    tags: ['revenue', 'daily'],
  },
  {
    id: 'order-status-breakdown',
    name: 'Order Status Breakdown',
    description: 'Count and value of orders by status',
    sql: `SELECT
  status,
  COUNT(*) AS order_count,
  SUM(total_amount) AS total_value,
  AVG(total_amount) AS avg_value
FROM sales.orders
GROUP BY status
ORDER BY order_count DESC`,
    createdAt: Date.now() - 4 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 4 * 24 * 60 * 60 * 1000,
    tags: ['orders', 'operations'],
  },
  {
    id: 'customer-segments',
    name: 'Segment Analysis',
    description: 'Customer distribution and revenue by segment',
    sql: `SELECT
  c.segment,
  COUNT(DISTINCT c.customer_id) AS customer_count,
  COUNT(o.order_id) AS total_orders,
  SUM(o.total_amount) AS total_revenue,
  AVG(o.total_amount) AS avg_order_value
FROM sales.customers c
LEFT JOIN sales.orders o ON c.customer_id = o.customer_id
GROUP BY c.segment
ORDER BY total_revenue DESC`,
    createdAt: Date.now() - 3 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 3 * 24 * 60 * 60 * 1000,
    tags: ['customer', 'segmentation'],
  },
  {
    id: 'low-stock-products',
    name: 'Low Stock Products',
    description: 'Products with stock below threshold',
    sql: `SELECT
  p.product_id,
  p.name,
  p.category,
  p.stock_quantity,
  p.price,
  s.name AS supplier_name
FROM inventory.products p
JOIN inventory.suppliers s ON p.product_id = s.supplier_id
WHERE p.stock_quantity < 50
ORDER BY p.stock_quantity ASC`,
    createdAt: Date.now() - 2 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 2 * 24 * 60 * 60 * 1000,
    tags: ['inventory', 'operations'],
  },
  {
    id: 'customer-lifetime-value',
    name: 'Customer Lifetime Value',
    description: 'CLV with order frequency and recency',
    sql: `SELECT
  cm.customer_id,
  c.name,
  c.segment,
  cm.lifetime_value,
  cm.total_orders,
  cm.last_order_date
FROM analytics.customer_metrics cm
JOIN sales.customers c ON cm.customer_id = c.customer_id
ORDER BY cm.lifetime_value DESC
LIMIT 20`,
    createdAt: Date.now() - 1 * 24 * 60 * 60 * 1000,
    updatedAt: Date.now() - 1 * 24 * 60 * 60 * 1000,
    tags: ['customer', 'metric'],
  },
];

// Mock result sets keyed by patterns found in the SQL
const mockResultSets: Record<string, QueryResult> = {
  'top-customers': {
    columns: [
      { name: 'customer_id', type: 'INTEGER' },
      { name: 'name', type: 'VARCHAR' },
      { name: 'segment', type: 'VARCHAR' },
      { name: 'total_orders', type: 'INTEGER' },
      { name: 'total_revenue', type: 'DECIMAL' },
    ],
    rows: [
      { customer_id: 1042, name: 'Acme Corp', segment: 'Enterprise', total_orders: 47, total_revenue: 284350.00 },
      { customer_id: 1015, name: 'TechStart Inc', segment: 'Enterprise', total_orders: 38, total_revenue: 198420.50 },
      { customer_id: 1087, name: 'Global Retail Co', segment: 'Enterprise', total_orders: 31, total_revenue: 156780.25 },
      { customer_id: 1023, name: 'DataFlow Systems', segment: 'SMB', total_orders: 29, total_revenue: 134560.00 },
      { customer_id: 1056, name: 'CloudNine Solutions', segment: 'SMB', total_orders: 25, total_revenue: 112340.75 },
      { customer_id: 1091, name: 'Summit Analytics', segment: 'Enterprise', total_orders: 22, total_revenue: 98750.00 },
      { customer_id: 1034, name: 'Bright Ideas LLC', segment: 'SMB', total_orders: 19, total_revenue: 87620.50 },
      { customer_id: 1078, name: 'Metro Services', segment: 'Consumer', total_orders: 16, total_revenue: 72140.00 },
      { customer_id: 1008, name: 'Pioneer Dynamics', segment: 'SMB', total_orders: 14, total_revenue: 65430.25 },
      { customer_id: 1062, name: 'Vista Partners', segment: 'Consumer', total_orders: 12, total_revenue: 54210.00 },
    ],
    rowCount: 10,
    executionTimeMs: 142,
  },
  'daily-revenue': {
    columns: [
      { name: 'date', type: 'DATE' },
      { name: 'order_count', type: 'INTEGER' },
      { name: 'revenue', type: 'DECIMAL' },
      { name: 'avg_order_value', type: 'DECIMAL' },
    ],
    rows: [
      { date: '2025-02-10', order_count: 34, revenue: 18420.50, avg_order_value: 541.78 },
      { date: '2025-02-09', order_count: 28, revenue: 15230.00, avg_order_value: 543.93 },
      { date: '2025-02-08', order_count: 42, revenue: 22150.75, avg_order_value: 527.40 },
      { date: '2025-02-07', order_count: 31, revenue: 16890.25, avg_order_value: 544.85 },
      { date: '2025-02-06', order_count: 37, revenue: 19540.00, avg_order_value: 528.11 },
      { date: '2025-02-05', order_count: 25, revenue: 13780.50, avg_order_value: 551.22 },
      { date: '2025-02-04', order_count: 39, revenue: 21340.75, avg_order_value: 547.20 },
      { date: '2025-02-03', order_count: 33, revenue: 17950.00, avg_order_value: 544.24 },
    ],
    rowCount: 8,
    executionTimeMs: 89,
  },
  'order-status': {
    columns: [
      { name: 'status', type: 'VARCHAR' },
      { name: 'order_count', type: 'INTEGER' },
      { name: 'total_value', type: 'DECIMAL' },
      { name: 'avg_value', type: 'DECIMAL' },
    ],
    rows: [
      { status: 'completed', order_count: 1247, total_value: 687420.50, avg_value: 551.26 },
      { status: 'processing', order_count: 183, total_value: 98340.00, avg_value: 537.38 },
      { status: 'shipped', order_count: 156, total_value: 84210.75, avg_value: 539.81 },
      { status: 'pending', order_count: 94, total_value: 51240.25, avg_value: 545.11 },
      { status: 'cancelled', order_count: 42, total_value: 22890.00, avg_value: 545.00 },
      { status: 'refunded', order_count: 18, total_value: 9870.50, avg_value: 548.36 },
    ],
    rowCount: 6,
    executionTimeMs: 67,
  },
  'segment': {
    columns: [
      { name: 'segment', type: 'VARCHAR' },
      { name: 'customer_count', type: 'INTEGER' },
      { name: 'total_orders', type: 'INTEGER' },
      { name: 'total_revenue', type: 'DECIMAL' },
      { name: 'avg_order_value', type: 'DECIMAL' },
    ],
    rows: [
      { segment: 'Enterprise', customer_count: 124, total_orders: 892, total_revenue: 489250.75, avg_order_value: 548.49 },
      { segment: 'SMB', customer_count: 356, total_orders: 1423, total_revenue: 412340.00, avg_order_value: 289.80 },
      { segment: 'Consumer', customer_count: 892, total_orders: 2140, total_revenue: 324560.25, avg_order_value: 151.66 },
    ],
    rowCount: 3,
    executionTimeMs: 112,
  },
  'low-stock': {
    columns: [
      { name: 'product_id', type: 'INTEGER' },
      { name: 'name', type: 'VARCHAR' },
      { name: 'category', type: 'VARCHAR' },
      { name: 'stock_quantity', type: 'INTEGER' },
      { name: 'price', type: 'DECIMAL' },
      { name: 'supplier_name', type: 'VARCHAR' },
    ],
    rows: [
      { product_id: 2041, name: 'Wireless Mouse Pro', category: 'Electronics', stock_quantity: 3, price: 49.99, supplier_name: 'TechSupply Co' },
      { product_id: 2087, name: 'USB-C Hub 7-Port', category: 'Electronics', stock_quantity: 8, price: 34.99, supplier_name: 'GlobalParts Ltd' },
      { product_id: 2023, name: 'Ergonomic Keyboard', category: 'Electronics', stock_quantity: 12, price: 89.99, supplier_name: 'TechSupply Co' },
      { product_id: 2056, name: 'Desk Organizer Set', category: 'Office', stock_quantity: 18, price: 24.99, supplier_name: 'OfficePro Inc' },
      { product_id: 2034, name: 'Monitor Stand Riser', category: 'Furniture', stock_quantity: 22, price: 39.99, supplier_name: 'WorkSpace Direct' },
      { product_id: 2012, name: 'Webcam HD 1080p', category: 'Electronics', stock_quantity: 31, price: 59.99, supplier_name: 'TechSupply Co' },
      { product_id: 2098, name: 'Cable Management Kit', category: 'Office', stock_quantity: 45, price: 14.99, supplier_name: 'OfficePro Inc' },
    ],
    rowCount: 7,
    executionTimeMs: 94,
  },
  'lifetime-value': {
    columns: [
      { name: 'customer_id', type: 'INTEGER' },
      { name: 'name', type: 'VARCHAR' },
      { name: 'segment', type: 'VARCHAR' },
      { name: 'lifetime_value', type: 'DECIMAL' },
      { name: 'total_orders', type: 'INTEGER' },
      { name: 'last_order_date', type: 'DATE' },
    ],
    rows: [
      { customer_id: 1042, name: 'Acme Corp', segment: 'Enterprise', lifetime_value: 284350.00, total_orders: 47, last_order_date: '2025-02-09' },
      { customer_id: 1015, name: 'TechStart Inc', segment: 'Enterprise', lifetime_value: 198420.50, total_orders: 38, last_order_date: '2025-02-08' },
      { customer_id: 1087, name: 'Global Retail Co', segment: 'Enterprise', lifetime_value: 156780.25, total_orders: 31, last_order_date: '2025-02-10' },
      { customer_id: 1023, name: 'DataFlow Systems', segment: 'SMB', lifetime_value: 134560.00, total_orders: 29, last_order_date: '2025-02-07' },
      { customer_id: 1056, name: 'CloudNine Solutions', segment: 'SMB', lifetime_value: 112340.75, total_orders: 25, last_order_date: '2025-02-06' },
      { customer_id: 1091, name: 'Summit Analytics', segment: 'Enterprise', lifetime_value: 98750.00, total_orders: 22, last_order_date: '2025-02-05' },
      { customer_id: 1034, name: 'Bright Ideas LLC', segment: 'SMB', lifetime_value: 87620.50, total_orders: 19, last_order_date: '2025-02-04' },
      { customer_id: 1078, name: 'Metro Services', segment: 'Consumer', lifetime_value: 72140.00, total_orders: 16, last_order_date: '2025-02-03' },
    ],
    rowCount: 8,
    executionTimeMs: 156,
  },
};

// Fallback generic result for custom queries
const genericResult: QueryResult = {
  columns: [
    { name: 'id', type: 'INTEGER' },
    { name: 'name', type: 'VARCHAR' },
    { name: 'value', type: 'DECIMAL' },
    { name: 'created_at', type: 'TIMESTAMP' },
  ],
  rows: [
    { id: 1, name: 'Row 1', value: 1250.00, created_at: '2025-02-10 09:15:22' },
    { id: 2, name: 'Row 2', value: 890.50, created_at: '2025-02-09 14:30:45' },
    { id: 3, name: 'Row 3', value: 2340.75, created_at: '2025-02-08 11:22:10' },
    { id: 4, name: 'Row 4', value: 445.00, created_at: '2025-02-07 16:45:33' },
    { id: 5, name: 'Row 5', value: 1780.25, created_at: '2025-02-06 08:12:55' },
  ],
  rowCount: 5,
  executionTimeMs: 78,
};

/**
 * Match a SQL string to the best mock result set.
 * Uses simple keyword matching against the SQL content.
 */
export function getResultForQuery(sql: string): QueryResult {
  const lowerSql = sql.toLowerCase();

  if (lowerSql.includes('lifetime_value') || lowerSql.includes('customer_metrics')) {
    return mockResultSets['lifetime-value']!;
  }
  if (lowerSql.includes('stock_quantity') || lowerSql.includes('stock')) {
    return mockResultSets['low-stock']!;
  }
  if (lowerSql.includes('segment') && lowerSql.includes('group by')) {
    return mockResultSets['segment']!;
  }
  if (lowerSql.includes('status') && lowerSql.includes('group by')) {
    return mockResultSets['order-status']!;
  }
  if (lowerSql.includes('date(') || lowerSql.includes('daily') || (lowerSql.includes('order_date') && lowerSql.includes('group by'))) {
    return mockResultSets['daily-revenue']!;
  }
  if (lowerSql.includes('customer') && (lowerSql.includes('revenue') || lowerSql.includes('sum'))) {
    return mockResultSets['top-customers']!;
  }

  return genericResult;
}

export function getSavedQueryById(id: string): SavedQuery | undefined {
  return mockSavedQueries.find(q => q.id === id);
}
