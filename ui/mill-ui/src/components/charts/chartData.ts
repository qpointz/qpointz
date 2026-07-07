import type { ChartSnapshotRow, ChartVisualizationConfig } from './types';

/**
 * Adapts query result rows to chart snapshot rows.
 */
export function toChartSnapshotRows(
  rows: Array<Record<string, unknown>> | undefined,
): ChartSnapshotRow[] {
  if (!rows?.length) return [];
  return rows.map((row) => ({ ...row }));
}

export function chartVisualizationsFromPayload(
  visualizations: unknown,
): ChartVisualizationConfig[] {
  if (!Array.isArray(visualizations)) return [];
  const parsed: ChartVisualizationConfig[] = [];
  for (const entry of visualizations) {
    if (!entry || typeof entry !== 'object') continue;
    const map = entry as Record<string, unknown>;
    if (map.kind !== 'chart') continue;
    const chartType = typeof map.chartType === 'string' ? map.chartType : '';
    const key = typeof map.key === 'string' ? map.key : 'default';
    const encodings = (map.encodings as Record<string, { field: string; label?: string }>) ?? {};
    if (!chartType) continue;
    parsed.push({
      key,
      kind: 'chart',
      chartType,
      encodings,
      ...(typeof map.title === 'string' ? { title: map.title } : {}),
      ...(typeof map.description === 'string' ? { description: map.description } : {}),
      ...(map.options && typeof map.options === 'object'
        ? { options: map.options as Record<string, unknown> }
        : {}),
      ...(map.presentation && typeof map.presentation === 'object'
        ? { presentation: map.presentation as Record<string, unknown> }
        : {}),
    });
  }
  return parsed;
}

function encodingFields(config: ChartVisualizationConfig): string[] {
  return Object.values(config.encodings ?? {})
    .map((encoding) => encoding.field?.trim())
    .filter((field): field is string => Boolean(field));
}

function columnNameSet(columns: readonly string[]): Set<string> {
  return new Set(columns.map((name) => name.trim().toLowerCase()).filter(Boolean));
}

/**
 * Keeps only chart specs whose encoding fields exist on the current result/schema columns.
 * Prevents stale visualizations from a prior SQL artifact appearing on an unrelated query card.
 */
export function filterChartVisualizationsForColumns(
  visualizations: readonly ChartVisualizationConfig[],
  columns: readonly string[],
): ChartVisualizationConfig[] {
  if (!visualizations.length) return [];
  const available = columnNameSet(columns);
  if (!available.size) return [...visualizations];
  return visualizations.filter((chart) => {
    const fields = encodingFields(chart);
    if (!fields.length) return false;
    return fields.every((field) => available.has(field.toLowerCase()));
  });
}
