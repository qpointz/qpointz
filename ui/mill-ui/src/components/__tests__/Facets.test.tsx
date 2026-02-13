import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { DescriptiveFacet } from '../data-model/facets/DescriptiveFacet';
import { StructuralFacet } from '../data-model/facets/StructuralFacet';
import { RelationFacet } from '../data-model/facets/RelationFacet';
import type {
  DescriptiveFacet as DescriptiveFacetType,
  StructuralFacet as StructuralFacetType,
  RelationFacet as RelationFacetType,
} from '../../types/schema';

function renderInMantine(ui: React.ReactElement) {
  return render(<MantineProvider>{ui}</MantineProvider>);
}

describe('DescriptiveFacet', () => {
  const fullFacet: DescriptiveFacetType = {
    displayName: 'Customer ID',
    description: 'Primary identifier for customers',
    businessMeaning: 'Unique reference number assigned to each customer',
    businessDomain: 'Sales',
    businessOwner: 'Data Engineering',
    tags: ['identifier', 'primary'],
    synonyms: ['cust_id', 'client_id'],
  };

  it('should render the display name', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('Display Name')).toBeInTheDocument();
    expect(screen.getByText('Customer ID')).toBeInTheDocument();
  });

  it('should render the description', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('Description')).toBeInTheDocument();
    expect(screen.getByText('Primary identifier for customers')).toBeInTheDocument();
  });

  it('should render business meaning', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('Business Meaning')).toBeInTheDocument();
    expect(screen.getByText('Unique reference number assigned to each customer')).toBeInTheDocument();
  });

  it('should render domain and owner', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('Domain')).toBeInTheDocument();
    expect(screen.getByText('Sales')).toBeInTheDocument();
    expect(screen.getByText('Owner')).toBeInTheDocument();
    expect(screen.getByText('Data Engineering')).toBeInTheDocument();
  });

  it('should render synonyms as badges', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('Synonyms')).toBeInTheDocument();
    expect(screen.getByText('cust_id')).toBeInTheDocument();
    expect(screen.getByText('client_id')).toBeInTheDocument();
  });

  it('should render tags as badges', () => {
    renderInMantine(<DescriptiveFacet facet={fullFacet} />);
    expect(screen.getByText('#identifier')).toBeInTheDocument();
    expect(screen.getByText('#primary')).toBeInTheDocument();
  });

  it('should not render missing sections', () => {
    renderInMantine(<DescriptiveFacet facet={{ displayName: 'Test' }} />);
    expect(screen.getByText('Test')).toBeInTheDocument();
    expect(screen.queryByText('Description')).not.toBeInTheDocument();
    expect(screen.queryByText('Business Meaning')).not.toBeInTheDocument();
    expect(screen.queryByText('Synonyms')).not.toBeInTheDocument();
  });
});

describe('StructuralFacet', () => {
  const fullFacet: StructuralFacetType = {
    physicalName: 'customer_id',
    physicalType: 'INTEGER',
    precision: 10,
    isPrimaryKey: true,
    isForeignKey: false,
    isUnique: true,
    nullable: false,
    defaultValue: '0',
  };

  it('should render constraint badges', () => {
    renderInMantine(<StructuralFacet facet={fullFacet} />);
    expect(screen.getByText('Constraints')).toBeInTheDocument();
    expect(screen.getByText('Primary Key')).toBeInTheDocument();
    expect(screen.getByText('Unique')).toBeInTheDocument();
    expect(screen.getByText('Not Null')).toBeInTheDocument();
  });

  it('should not show FK badge when isForeignKey is false', () => {
    renderInMantine(<StructuralFacet facet={fullFacet} />);
    expect(screen.queryByText('Foreign Key')).not.toBeInTheDocument();
  });

  it('should render FK badge when isForeignKey is true', () => {
    renderInMantine(<StructuralFacet facet={{ ...fullFacet, isForeignKey: true }} />);
    expect(screen.getByText('Foreign Key')).toBeInTheDocument();
  });

  it('should render physical name', () => {
    renderInMantine(<StructuralFacet facet={fullFacet} />);
    expect(screen.getByText('Physical Name')).toBeInTheDocument();
    expect(screen.getByText('customer_id')).toBeInTheDocument();
  });

  it('should render data type with precision', () => {
    renderInMantine(<StructuralFacet facet={fullFacet} />);
    expect(screen.getByText('Data Type')).toBeInTheDocument();
    expect(screen.getByText('INTEGER(10)')).toBeInTheDocument();
  });

  it('should render data type with precision and scale', () => {
    const facet: StructuralFacetType = { physicalType: 'DECIMAL', precision: 10, scale: 2 };
    renderInMantine(<StructuralFacet facet={facet} />);
    expect(screen.getByText('DECIMAL(10, 2)')).toBeInTheDocument();
  });

  it('should show Nullable badge when nullable is true', () => {
    renderInMantine(<StructuralFacet facet={{ nullable: true }} />);
    expect(screen.getByText('Nullable')).toBeInTheDocument();
  });

  it('should render default value', () => {
    renderInMantine(<StructuralFacet facet={fullFacet} />);
    expect(screen.getByText('Default Value')).toBeInTheDocument();
    expect(screen.getByText('0')).toBeInTheDocument();
  });
});

describe('RelationFacet', () => {
  const relations: RelationFacetType[] = [
    {
      id: 'rel-1',
      name: 'customer_orders',
      sourceEntity: 'sales.customers',
      targetEntity: 'sales.orders',
      cardinality: '1:N',
      relationType: 'FOREIGN_KEY',
      description: 'One customer has many orders',
    },
    {
      id: 'rel-2',
      name: 'order_items_link',
      sourceEntity: 'sales.orders',
      targetEntity: 'sales.order_items',
      cardinality: '1:N',
      relationType: 'LOGICAL',
    },
  ];

  it('should render "No relations defined" when empty', () => {
    renderInMantine(<RelationFacet relations={[]} />);
    expect(screen.getByText('No relations defined')).toBeInTheDocument();
  });

  it('should render relation names', () => {
    renderInMantine(<RelationFacet relations={relations} />);
    expect(screen.getByText('customer_orders')).toBeInTheDocument();
    expect(screen.getByText('order_items_link')).toBeInTheDocument();
  });

  it('should render cardinality badges', () => {
    renderInMantine(<RelationFacet relations={relations} />);
    const badges = screen.getAllByText('1:N');
    expect(badges.length).toBe(2);
  });

  it('should render relation type badges', () => {
    renderInMantine(<RelationFacet relations={relations} />);
    expect(screen.getByText('FOREIGN KEY')).toBeInTheDocument();
    expect(screen.getByText('LOGICAL')).toBeInTheDocument();
  });

  it('should render source and target entities', () => {
    renderInMantine(<RelationFacet relations={relations} />);
    expect(screen.getByText('sales.customers')).toBeInTheDocument();
    // "sales.orders" appears as both target (rel-1) and source (rel-2)
    const ordersMatches = screen.getAllByText('sales.orders');
    expect(ordersMatches.length).toBe(2);
    expect(screen.getByText('sales.order_items')).toBeInTheDocument();
  });

  it('should render relation description when present', () => {
    renderInMantine(<RelationFacet relations={relations} />);
    expect(screen.getByText('One customer has many orders')).toBeInTheDocument();
  });
});
