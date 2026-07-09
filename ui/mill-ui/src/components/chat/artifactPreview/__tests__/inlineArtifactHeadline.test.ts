import { describe, expect, it } from 'vitest';
import { inlineFacetHeadline, inlineSqlHeadline } from '../inlineArtifactHeadline';

describe('inlineSqlHeadline', () => {
  it('should strip .sql suffix and truncate long titles', () => {
    const headline = inlineSqlHeadline(
      {
        kind: 'sql',
        sql: 'select 1',
        info: { title: 'Revenue by customer segment overview report' },
      },
      undefined,
    );
    expect(headline).not.toContain('.sql');
    expect(headline.length).toBeLessThanOrEqual(40);
    expect(headline.startsWith('Revenue by customer segment')).toBe(true);
  });
});

describe('inlineFacetHeadline', () => {
  it('should prefer concept payload name', () => {
    expect(
      inlineFacetHeadline({
        kind: 'facet-proposal',
        facetTypeKey: 'concept',
        metadataEntityId: 'model-entity',
        payload: { name: 'Active customer' },
      }),
    ).toBe('Active customer');
  });

  it('should use catalog path tail when no concept name', () => {
    expect(
      inlineFacetHeadline({
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'sales.customers.email',
        catalogPath: 'sales.customers.email',
        payload: { description: 'Email column' },
      }),
    ).toBe('email');
  });
});
