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

  it('should host-apply sql for inline-analysis', () => {
    const treatment = resolveArtifactTreatment('inline-analysis', 'sql-data-composite');
    expect(treatment.mode).toBe('host-apply');
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
});
