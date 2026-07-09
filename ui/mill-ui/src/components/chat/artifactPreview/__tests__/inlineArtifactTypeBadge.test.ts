import { describe, expect, it } from 'vitest';
import { resolveInlineArtifactTypeBadge } from '../inlineArtifactTypeBadgeModel';

describe('resolveInlineArtifactTypeBadge', () => {
  it('should return SQL for sql-data-composite', () => {
    expect(resolveInlineArtifactTypeBadge({ kind: 'sql-data-composite' })).toBe('SQL');
  });

  it('should return C for concept facets', () => {
    expect(
      resolveInlineArtifactTypeBadge({
        kind: 'facet-proposal',
        facetTypeKey: 'urn:mill/metadata/facet-type:concept',
      }),
    ).toBe('C');
  });

  it('should return DQ for data-quality category', () => {
    expect(
      resolveInlineArtifactTypeBadge({
        kind: 'facet-proposal',
        facetTypeKey: 'dq-rule',
        facetCategory: 'data-quality',
      }),
    ).toBe('DQ');
  });

  it('should return AI for ai category', () => {
    expect(
      resolveInlineArtifactTypeBadge({
        kind: 'facet-proposal',
        facetTypeKey: 'ai-hint',
        facetCategory: 'ai',
      }),
    ).toBe('AI');
  });

  it('should return M as fallback', () => {
    expect(
      resolveInlineArtifactTypeBadge({
        kind: 'facet-proposal',
        facetTypeKey: 'urn:mill/metadata/facet-type:descriptive',
      }),
    ).toBe('M');
  });
});
