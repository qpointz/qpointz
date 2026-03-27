import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { StructuralFacet } from '../data-model/facets/StructuralFacet';
import type { StructuralFacet as StructuralFacetType } from '../../types/schema';

function renderInMantine(ui: React.ReactElement) {
  return render(<MantineProvider>{ui}</MantineProvider>);
}

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
