import { describe, expect, it } from 'vitest';
import {
  popoverActionsForInline,
  stripActionsForInline,
} from '../inlineArtifactActionPlacement';

describe('inlineArtifactActionPlacement', () => {
  it('should place SQL apply actions on strip and copy in popover', () => {
    const treatment = ['apply', 'apply-and-run', 'copy'] as const;
    expect(stripActionsForInline('sql-data-composite', [...treatment])).toEqual([
      'apply',
      'apply-and-run',
    ]);
    expect(popoverActionsForInline('sql-data-composite', [...treatment])).toEqual(['copy']);
  });

  it('should place facet lifecycle actions on strip and copy in popover', () => {
    const treatment = ['copy', 'open-in-model', 'reject', 'accept'] as const;
    expect(stripActionsForInline('facet-proposal', [...treatment])).toEqual([
      'accept',
      'reject',
      'open-in-model',
    ]);
    expect(popoverActionsForInline('facet-proposal', [...treatment])).toEqual(['copy']);
  });
});
