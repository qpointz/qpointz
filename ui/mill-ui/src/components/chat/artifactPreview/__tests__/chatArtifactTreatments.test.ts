import { describe, it, expect } from 'vitest';
import {
  resolveArtifactTreatment,
  treatmentAllowsExpand,
} from '../chatArtifactTreatments';

describe('chatArtifactTreatments', () => {
  it('should use condensed preview with expand for general sql-data-composite', () => {
    const treatment = resolveArtifactTreatment('general', 'sql-data-composite');
    expect(treatment.mode).toBe('condensed-preview');
    expect(treatment.transitions).toContain('expand');
    expect(treatment.transitions).toContain('open-in-analysis');
    expect(treatment.actions).toContain('run');
  });

  it('should use inline artifact strip for inline-analysis sql-data-composite', () => {
    const treatment = resolveArtifactTreatment('inline-analysis', 'sql-data-composite');
    expect(treatment.mode).toBe('inline-artifact-strip');
    expect(treatment.actions).toEqual(['apply', 'apply-and-run', 'copy']);
    expect(treatmentAllowsExpand('inline-analysis', 'sql-data-composite')).toBe(false);
  });

  it('should allow copy-only condensed preview for inline-model', () => {
    const treatment = resolveArtifactTreatment('inline-model', 'sql-data-composite');
    expect(treatment.mode).toBe('condensed-preview');
    expect(treatment.views).toEqual(['condensed']);
    expect(treatment.actions).toEqual(['copy']);
  });

  it('should fall back to prose-only for unknown kinds', () => {
    const treatment = resolveArtifactTreatment('general', 'unknown-kind' as 'sql-data-composite');
    expect(treatment.mode).toBe('prose-only');
  });

  it('should use condensed preview for general facet-proposal', () => {
    const treatment = resolveArtifactTreatment('general', 'facet-proposal');
    expect(treatment.mode).toBe('condensed-preview');
    expect(treatment.views).toEqual(['condensed']);
    expect(treatment.actions).toEqual(['copy', 'open-in-model', 'reject', 'accept']);
  });

  it('should use inline artifact strip for inline hosts facet-proposal', () => {
    expect(resolveArtifactTreatment('inline-analysis', 'facet-proposal').mode).toBe('inline-artifact-strip');
    expect(resolveArtifactTreatment('inline-model', 'facet-proposal').mode).toBe('conversation-card');
    expect(resolveArtifactTreatment('inline-knowledge', 'facet-proposal').mode).toBe('conversation-card');
  });
});
