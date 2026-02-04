import type { SchemaEntity, EntityFacets } from '../types/schema';

export const mockSchemaTree: SchemaEntity[] = [
  {
    id: 'sales',
    type: 'SCHEMA',
    name: 'sales',
    children: [
      {
        id: 'sales.customers',
        type: 'TABLE',
        name: 'customers',
        children: [
          { id: 'sales.customers.customer_id', type: 'ATTRIBUTE', name: 'customer_id' },
          { id: 'sales.customers.name', type: 'ATTRIBUTE', name: 'name' },
          { id: 'sales.customers.email', type: 'ATTRIBUTE', name: 'email' },
          { id: 'sales.customers.created_at', type: 'ATTRIBUTE', name: 'created_at' },
          { id: 'sales.customers.segment', type: 'ATTRIBUTE', name: 'segment' },
        ],
      },
      {
        id: 'sales.orders',
        type: 'TABLE',
        name: 'orders',
        children: [
          { id: 'sales.orders.order_id', type: 'ATTRIBUTE', name: 'order_id' },
          { id: 'sales.orders.customer_id', type: 'ATTRIBUTE', name: 'customer_id' },
          { id: 'sales.orders.order_date', type: 'ATTRIBUTE', name: 'order_date' },
          { id: 'sales.orders.total_amount', type: 'ATTRIBUTE', name: 'total_amount' },
          { id: 'sales.orders.status', type: 'ATTRIBUTE', name: 'status' },
        ],
      },
      {
        id: 'sales.order_items',
        type: 'TABLE',
        name: 'order_items',
        children: [
          { id: 'sales.order_items.item_id', type: 'ATTRIBUTE', name: 'item_id' },
          { id: 'sales.order_items.order_id', type: 'ATTRIBUTE', name: 'order_id' },
          { id: 'sales.order_items.product_id', type: 'ATTRIBUTE', name: 'product_id' },
          { id: 'sales.order_items.quantity', type: 'ATTRIBUTE', name: 'quantity' },
          { id: 'sales.order_items.unit_price', type: 'ATTRIBUTE', name: 'unit_price' },
        ],
      },
    ],
  },
  {
    id: 'inventory',
    type: 'SCHEMA',
    name: 'inventory',
    children: [
      {
        id: 'inventory.products',
        type: 'TABLE',
        name: 'products',
        children: [
          { id: 'inventory.products.product_id', type: 'ATTRIBUTE', name: 'product_id' },
          { id: 'inventory.products.name', type: 'ATTRIBUTE', name: 'name' },
          { id: 'inventory.products.category', type: 'ATTRIBUTE', name: 'category' },
          { id: 'inventory.products.price', type: 'ATTRIBUTE', name: 'price' },
          { id: 'inventory.products.stock_quantity', type: 'ATTRIBUTE', name: 'stock_quantity' },
        ],
      },
      {
        id: 'inventory.suppliers',
        type: 'TABLE',
        name: 'suppliers',
        children: [
          { id: 'inventory.suppliers.supplier_id', type: 'ATTRIBUTE', name: 'supplier_id' },
          { id: 'inventory.suppliers.name', type: 'ATTRIBUTE', name: 'name' },
          { id: 'inventory.suppliers.contact_email', type: 'ATTRIBUTE', name: 'contact_email' },
          { id: 'inventory.suppliers.country', type: 'ATTRIBUTE', name: 'country' },
        ],
      },
    ],
  },
  {
    id: 'analytics',
    type: 'SCHEMA',
    name: 'analytics',
    children: [
      {
        id: 'analytics.daily_sales',
        type: 'TABLE',
        name: 'daily_sales',
        children: [
          { id: 'analytics.daily_sales.date', type: 'ATTRIBUTE', name: 'date' },
          { id: 'analytics.daily_sales.total_orders', type: 'ATTRIBUTE', name: 'total_orders' },
          { id: 'analytics.daily_sales.total_revenue', type: 'ATTRIBUTE', name: 'total_revenue' },
          { id: 'analytics.daily_sales.avg_order_value', type: 'ATTRIBUTE', name: 'avg_order_value' },
        ],
      },
      {
        id: 'analytics.customer_metrics',
        type: 'TABLE',
        name: 'customer_metrics',
        children: [
          { id: 'analytics.customer_metrics.customer_id', type: 'ATTRIBUTE', name: 'customer_id' },
          { id: 'analytics.customer_metrics.lifetime_value', type: 'ATTRIBUTE', name: 'lifetime_value' },
          { id: 'analytics.customer_metrics.total_orders', type: 'ATTRIBUTE', name: 'total_orders' },
          { id: 'analytics.customer_metrics.last_order_date', type: 'ATTRIBUTE', name: 'last_order_date' },
        ],
      },
    ],
  },
];

export const mockFacets: Record<string, EntityFacets> = {
  // Schema facets
  'sales': {
    descriptive: {
      displayName: 'Sales',
      description: 'Core sales data including customers, orders, and transactions',
      businessDomain: 'Sales & Commerce',
      businessOwner: 'Sales Team',
    },
  },
  'inventory': {
    descriptive: {
      displayName: 'Inventory',
      description: 'Product catalog and supplier information',
      businessDomain: 'Supply Chain',
      businessOwner: 'Operations Team',
    },
  },
  'analytics': {
    descriptive: {
      displayName: 'Analytics',
      description: 'Aggregated metrics and reporting tables',
      businessDomain: 'Business Intelligence',
      businessOwner: 'Analytics Team',
    },
  },

  // Table facets
  'sales.customers': {
    descriptive: {
      displayName: 'Customers',
      description: 'Master customer data including contact information and segmentation',
      businessMeaning: 'Represents all registered customers in the system',
      tags: ['master-data', 'pii'],
    },
    structural: {
      physicalName: 'customers',
    },
    relations: [
      {
        id: 'rel-1',
        name: 'customer_orders',
        sourceEntity: 'sales.customers',
        targetEntity: 'sales.orders',
        cardinality: '1:N',
        relationType: 'FOREIGN_KEY',
        description: 'A customer can have many orders',
      },
    ],
  },
  'sales.orders': {
    descriptive: {
      displayName: 'Orders',
      description: 'Customer orders with status and totals',
      businessMeaning: 'Transaction records for customer purchases',
      tags: ['transactional'],
    },
    relations: [
      {
        id: 'rel-2',
        name: 'order_customer',
        sourceEntity: 'sales.orders',
        targetEntity: 'sales.customers',
        cardinality: 'N:1',
        relationType: 'FOREIGN_KEY',
        description: 'Each order belongs to one customer',
      },
      {
        id: 'rel-3',
        name: 'order_items',
        sourceEntity: 'sales.orders',
        targetEntity: 'sales.order_items',
        cardinality: '1:N',
        relationType: 'FOREIGN_KEY',
        description: 'An order contains multiple line items',
      },
    ],
  },

  // Attribute facets
  'sales.customers.customer_id': {
    descriptive: {
      displayName: 'Customer ID',
      description: 'Unique identifier for each customer',
      synonyms: ['cust_id', 'customer_key'],
    },
    structural: {
      physicalName: 'customer_id',
      physicalType: 'INTEGER',
      isPrimaryKey: true,
      isForeignKey: false,
      isUnique: true,
      nullable: false,
    },
  },
  'sales.customers.name': {
    descriptive: {
      displayName: 'Customer Name',
      description: 'Full name of the customer',
      tags: ['pii'],
    },
    structural: {
      physicalName: 'name',
      physicalType: 'VARCHAR',
      precision: 255,
      nullable: false,
    },
  },
  'sales.customers.email': {
    descriptive: {
      displayName: 'Email Address',
      description: 'Primary contact email for the customer',
      tags: ['pii', 'contact'],
    },
    structural: {
      physicalName: 'email',
      physicalType: 'VARCHAR',
      precision: 255,
      isUnique: true,
      nullable: false,
    },
  },
  'sales.customers.segment': {
    descriptive: {
      displayName: 'Customer Segment',
      description: 'Customer classification (Enterprise, SMB, Consumer)',
      businessMeaning: 'Used for pricing tiers and support levels',
    },
    structural: {
      physicalName: 'segment',
      physicalType: 'VARCHAR',
      precision: 50,
      nullable: true,
      defaultValue: 'Consumer',
    },
  },
  'sales.orders.order_id': {
    descriptive: {
      displayName: 'Order ID',
      description: 'Unique order identifier',
    },
    structural: {
      physicalName: 'order_id',
      physicalType: 'INTEGER',
      isPrimaryKey: true,
      isUnique: true,
      nullable: false,
    },
  },
  'sales.orders.customer_id': {
    descriptive: {
      displayName: 'Customer ID',
      description: 'Reference to the customer who placed the order',
    },
    structural: {
      physicalName: 'customer_id',
      physicalType: 'INTEGER',
      isForeignKey: true,
      nullable: false,
    },
    relations: [
      {
        id: 'rel-4',
        name: 'fk_customer',
        sourceEntity: 'sales.orders.customer_id',
        targetEntity: 'sales.customers.customer_id',
        cardinality: 'N:1',
        relationType: 'FOREIGN_KEY',
        description: 'References the customer table',
      },
    ],
  },
  'sales.orders.total_amount': {
    descriptive: {
      displayName: 'Total Amount',
      description: 'Total order value including tax and shipping',
      businessMeaning: 'Revenue metric for financial reporting',
      tags: ['financial', 'metric'],
    },
    structural: {
      physicalName: 'total_amount',
      physicalType: 'DECIMAL',
      precision: 10,
      scale: 2,
      nullable: false,
    },
  },
  'inventory.products.product_id': {
    descriptive: {
      displayName: 'Product ID',
      description: 'Unique product identifier',
    },
    structural: {
      physicalName: 'product_id',
      physicalType: 'INTEGER',
      isPrimaryKey: true,
      isUnique: true,
      nullable: false,
    },
  },
  'inventory.products.price': {
    descriptive: {
      displayName: 'Unit Price',
      description: 'Current selling price of the product',
      tags: ['financial'],
    },
    structural: {
      physicalName: 'price',
      physicalType: 'DECIMAL',
      precision: 10,
      scale: 2,
      nullable: false,
    },
  },
  'analytics.customer_metrics.lifetime_value': {
    descriptive: {
      displayName: 'Customer Lifetime Value',
      description: 'Total revenue generated by this customer',
      businessMeaning: 'Key metric for customer segmentation and retention analysis',
      tags: ['metric', 'financial', 'analytics'],
    },
    structural: {
      physicalName: 'lifetime_value',
      physicalType: 'DECIMAL',
      precision: 12,
      scale: 2,
      nullable: true,
    },
  },
};

export function getEntityFacets(entityId: string): EntityFacets {
  return mockFacets[entityId] || {};
}

export function findEntityById(id: string, tree: SchemaEntity[] = mockSchemaTree): SchemaEntity | null {
  for (const entity of tree) {
    if (entity.id === id) return entity;
    if (entity.children) {
      const found = findEntityById(id, entity.children);
      if (found) return found;
    }
  }
  return null;
}
