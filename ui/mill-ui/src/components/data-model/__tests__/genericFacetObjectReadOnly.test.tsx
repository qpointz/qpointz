import { describe, expect, it } from 'vitest';
import type { ReactNode } from 'react';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import {
  renderGenericFacetObjectReadOnly,
  renderGenericFacetValueReadOnly,
} from '../genericFacetObjectReadOnly';

function renderNode(node: ReactNode) {
  return render(<MantineProvider>{node}</MantineProvider>);
}

describe('genericFacetObjectReadOnly', () => {
  it('should render key-value rows for schema-less OBJECT maps (flow effectiveMapping)', () => {
    renderNode(
      renderGenericFacetObjectReadOnly({
        type: 'regex',
        pattern: '(?<table>[^/]+)\\.csv$',
      }),
    );
    expect(screen.getByText('pattern')).toBeInTheDocument();
    expect(screen.getByText('type')).toBeInTheDocument();
    expect(screen.getByText('regex')).toBeInTheDocument();
    expect(
      screen.getByText((content) => content.includes('?<table>') && content.includes('.csv')),
    ).toBeInTheDocument();
  });

  it('should show em dash for empty object', () => {
    renderNode(renderGenericFacetObjectReadOnly({}));
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('should render nested object with indentation', () => {
    renderNode(
      renderGenericFacetValueReadOnly({
        outer: { inner: 42 },
      }),
    );
    expect(screen.getByText('outer')).toBeInTheDocument();
    expect(screen.getByText('inner')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
  });
});
