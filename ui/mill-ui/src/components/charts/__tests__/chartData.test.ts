import { describe, expect, it } from 'vitest';
import { filterChartVisualizationsForColumns } from '../chartData';
import type { ChartVisualizationConfig } from '../types';

const countriesChart: ChartVisualizationConfig = {
  key: 'default',
  kind: 'chart',
  chartType: 'bar',
  title: 'Top countries',
  encodings: {
    category: { field: 'country' },
    value: { field: 'client_count' },
  },
};

const exchangesChart: ChartVisualizationConfig = {
  key: 'default',
  kind: 'chart',
  chartType: 'bar',
  title: 'Top exchanges',
  encodings: {
    category: { field: 'exchange' },
    value: { field: 'cnt' },
  },
};

describe('filterChartVisualizationsForColumns', () => {
  it('shouldKeepOnlyChartsMatchingAvailableColumns', () => {
    const filtered = filterChartVisualizationsForColumns(
      [countriesChart, exchangesChart],
      ['exchange', 'cnt'],
    );
    expect(filtered).toEqual([exchangesChart]);
  });

  it('shouldKeepMultipleChartsWhenAllMatchSameResultShape', () => {
    const pieVariant: ChartVisualizationConfig = {
      ...countriesChart,
      key: 'pie',
      chartType: 'pie',
    };
    const filtered = filterChartVisualizationsForColumns(
      [countriesChart, pieVariant, exchangesChart],
      ['country', 'client_count'],
    );
    expect(filtered).toEqual([countriesChart, pieVariant]);
  });
});
