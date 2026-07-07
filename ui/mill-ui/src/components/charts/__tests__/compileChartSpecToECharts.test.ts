import { describe, expect, it } from 'vitest';
import { compileChartSpecToECharts } from '../compileChartSpecToECharts';
import type { ChartVisualizationConfig } from '../types';

describe('compileChartSpecToECharts', () => {
  const rows = [
    { country: 'US', client_count: 10 },
    { country: 'UK', client_count: 5 },
  ];

  it('should compile bar chart options', () => {
    const config: ChartVisualizationConfig = {
      key: 'default',
      kind: 'chart',
      chartType: 'bar',
      encodings: {
        category: { field: 'country' },
        value: { field: 'client_count' },
      },
    };
    const option = compileChartSpecToECharts(config, rows);
    const series = option?.series;
    const firstSeries = Array.isArray(series) ? series[0] : series;
    expect(firstSeries).toMatchObject({ type: 'bar' });
  });

  it('should return null for unsupported chart type', () => {
    const config: ChartVisualizationConfig = {
      key: 'default',
      kind: 'chart',
      chartType: 'treemap',
      encodings: {},
    };
    expect(compileChartSpecToECharts(config, rows)).toBeNull();
  });
});
