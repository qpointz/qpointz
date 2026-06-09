import type { AnalysisDialect, AnalysisService } from '../types/analysis';

export const ANALYSIS_QUERIES_BASE = '/api/v1/analysis/queries';
export const ANALYSIS_DIALECT_PATH = '/api/v1/analysis/dialect';

async function httpGetDialect(): Promise<AnalysisDialect> {
  const res = await fetch(ANALYSIS_DIALECT_PATH, { credentials: 'include' });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Analysis dialect failed (${res.status}): ${text}`);
  }
  return (await res.json()) as AnalysisDialect;
}

/**
 * HTTP client for Analysis-scoped configuration ({@code /api/v1/analysis/**}).
 */
export const analysisService: AnalysisService = {
  getDialect: httpGetDialect,
};
