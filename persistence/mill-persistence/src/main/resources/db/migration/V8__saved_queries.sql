-- WI-256: saved-query catalog for mill-ui Analysis (fixture seeds mirror mockQueries.ts).

CREATE TABLE saved_query (
    id           VARCHAR(128)  NOT NULL PRIMARY KEY,
    name         VARCHAR(512)  NOT NULL,
    description  VARCHAR(2048),
    sql_text     TEXT          NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,
    tags_json    TEXT
);

CREATE INDEX idx_saved_query_updated_at ON saved_query (updated_at DESC);

-- Fixture seeds (stable ids for dev smoke and UI tests).
INSERT INTO saved_query (id, name, description, sql_text, created_at, updated_at, tags_json) VALUES
(
    'top-customers',
    'Top Customers by Revenue',
    'Customers ranked by total order value',
    'SELECT
  c.customer_id,
  c.name,
  c.segment,
  COUNT(o.order_id) AS total_orders,
  SUM(o.total_amount) AS total_revenue
FROM sales.customers c
JOIN sales.orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name, c.segment
ORDER BY total_revenue DESC
LIMIT 10',
    TIMESTAMP '2025-01-28 12:00:00',
    TIMESTAMP '2025-02-08 12:00:00',
    '["revenue","customer"]'
),
(
    'daily-revenue',
    'Daily Revenue Summary',
    'Revenue and order count aggregated by day',
    'SELECT
  DATE(order_date) AS date,
  COUNT(*) AS order_count,
  SUM(total_amount) AS revenue,
  AVG(total_amount) AS avg_order_value
FROM sales.orders
WHERE order_date >= ''2025-01-01''
GROUP BY DATE(order_date)
ORDER BY date DESC',
    TIMESTAMP '2025-01-30 12:00:00',
    TIMESTAMP '2025-02-09 12:00:00',
    '["revenue","daily"]'
),
(
    'order-status-breakdown',
    'Order Status Breakdown',
    'Count and value of orders by status',
    'SELECT
  status,
  COUNT(*) AS order_count,
  SUM(total_amount) AS total_value,
  AVG(total_amount) AS avg_value
FROM sales.orders
GROUP BY status
ORDER BY order_count DESC',
    TIMESTAMP '2025-01-31 12:00:00',
    TIMESTAMP '2025-01-31 12:00:00',
    '["orders","operations"]'
),
(
    'customer-segments',
    'Segment Analysis',
    'Customer distribution and revenue by segment',
    'SELECT
  c.segment,
  COUNT(DISTINCT c.customer_id) AS customer_count,
  COUNT(o.order_id) AS total_orders,
  SUM(o.total_amount) AS total_revenue,
  AVG(o.total_amount) AS avg_order_value
FROM sales.customers c
LEFT JOIN sales.orders o ON c.customer_id = o.customer_id
GROUP BY c.segment
ORDER BY total_revenue DESC',
    TIMESTAMP '2025-02-01 12:00:00',
    TIMESTAMP '2025-02-01 12:00:00',
    '["customer","segmentation"]'
),
(
    'low-stock-products',
    'Low Stock Products',
    'Products with stock below threshold',
    'SELECT
  p.product_id,
  p.name,
  p.category,
  p.stock_quantity,
  p.price,
  s.name AS supplier_name
FROM inventory.products p
JOIN inventory.suppliers s ON p.product_id = s.supplier_id
WHERE p.stock_quantity < 50
ORDER BY p.stock_quantity ASC',
    TIMESTAMP '2025-02-02 12:00:00',
    TIMESTAMP '2025-02-02 12:00:00',
    '["inventory","operations"]'
),
(
    'customer-lifetime-value',
    'Customer Lifetime Value',
    'CLV with order frequency and recency',
    'SELECT
  cm.customer_id,
  c.name,
  c.segment,
  cm.lifetime_value,
  cm.total_orders,
  cm.last_order_date
FROM analytics.customer_metrics cm
JOIN sales.customers c ON cm.customer_id = c.customer_id
ORDER BY cm.lifetime_value DESC
LIMIT 20',
    TIMESTAMP '2025-02-03 12:00:00',
    TIMESTAMP '2025-02-03 12:00:00',
    '["customer","metric"]'
);
