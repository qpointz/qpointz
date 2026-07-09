import { describe, expect, it } from 'vitest';
import {
  setAnalysisHostExecuting,
  isAnalysisHostExecuting,
  subscribeAnalysisHostExecuting,
  setAnalysisAppliedArtifactKey,
  isAnalysisAppliedArtifact,
  subscribeAnalysisAppliedArtifact,
  normalizeAnalysisEditorSql,
} from '../analysisHostState';

describe('analysisHostState', () => {
  it('should publish executing flag to subscribers', () => {
    const seen: boolean[] = [];
    const unsubscribe = subscribeAnalysisHostExecuting(() => {
      seen.push(isAnalysisHostExecuting());
    });

    setAnalysisHostExecuting(true);
    setAnalysisHostExecuting(false);
    unsubscribe();

    expect(seen).toEqual([true, false]);
  });

  it('should normalize editor SQL text', () => {
    expect(normalizeAnalysisEditorSql('  select  1 \n')).toBe('select 1');
  });

  it('should track applied artifact key independently of SQL text', () => {
    const seen: string[] = [];
    const unsubscribe = subscribeAnalysisAppliedArtifact(() => {
      seen.push(isAnalysisAppliedArtifact('art-a') ? 'a' : 'no');
    });

    setAnalysisAppliedArtifactKey('art-a');
    setAnalysisAppliedArtifactKey('art-b');
    unsubscribe();

    expect(seen).toEqual(['a', 'no']);
    expect(isAnalysisAppliedArtifact('art-a')).toBe(false);
    expect(isAnalysisAppliedArtifact('art-b')).toBe(true);
  });
});
