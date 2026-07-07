import type { EChartsOption } from 'echarts';
import type { ChartSnapshotRow, ChartVisualizationConfig } from './types';

function columnValues(rows: ChartSnapshotRow[], field: string): unknown[] {
  return rows.map((row) => row[field]);
}

function encodingField(
  encodings: ChartVisualizationConfig['encodings'],
  role: string,
): string | undefined {
  return encodings[role]?.field;
}

/**
 * Compiles a semantic chart visualization config and row snapshot to ECharts options.
 */
export function compileChartSpecToECharts(
  config: ChartVisualizationConfig,
  rows: ChartSnapshotRow[],
): EChartsOption | null {
  if (!rows.length) return null;
  switch (config.chartType) {
    case 'bar':
      return compileBar(config, rows);
    case 'line':
      return compileLine(config, rows, false);
    case 'area':
      return compileLine(config, rows, true);
    case 'scatter':
      return compileScatter(config, rows);
    case 'pie':
      return compilePie(config, rows);
    default:
      return null;
  }
}

function compileBar(config: ChartVisualizationConfig, rows: ChartSnapshotRow[]): EChartsOption {
  const categoryField = encodingField(config.encodings, 'category') ?? '';
  const valueField = encodingField(config.encodings, 'value') ?? '';
  const horizontal = config.options?.orientation === 'horizontal';
  return {
    tooltip: { trigger: 'axis' },
    xAxis: horizontal ? { type: 'value' } : { type: 'category', data: columnValues(rows, categoryField) as string[] },
    yAxis: horizontal ? { type: 'category', data: columnValues(rows, categoryField) as string[] } : { type: 'value' },
    series: [
      {
        type: 'bar',
        data: columnValues(rows, valueField) as number[],
        stack: config.options?.stacked ? 'total' : undefined,
      },
    ],
  };
}

function compileLine(
  config: ChartVisualizationConfig,
  rows: ChartSnapshotRow[],
  area: boolean,
): EChartsOption {
  const xField = encodingField(config.encodings, 'x') ?? '';
  const yField = encodingField(config.encodings, 'y') ?? '';
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: columnValues(rows, xField) as string[] },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'line',
        data: columnValues(rows, yField) as number[],
        areaStyle: area ? {} : undefined,
        smooth: config.options?.smooth === true,
        stack: area && config.options?.stacked ? 'total' : undefined,
      },
    ],
  };
}

function compileScatter(config: ChartVisualizationConfig, rows: ChartSnapshotRow[]): EChartsOption {
  const xField = encodingField(config.encodings, 'x') ?? '';
  const yField = encodingField(config.encodings, 'y') ?? '';
  return {
    tooltip: { trigger: 'item' },
    xAxis: { type: 'value' },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'scatter',
        data: rows.map((row) => [row[xField], row[yField]] as [number | string, number | string]),
      },
    ],
  };
}

function compilePie(config: ChartVisualizationConfig, rows: ChartSnapshotRow[]): EChartsOption {
  const categoryField = encodingField(config.encodings, 'category') ?? '';
  const valueField = encodingField(config.encodings, 'value') ?? '';
  return {
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: config.options?.donut ? ['40%', '70%'] : '60%',
        data: rows.map((row) => ({
          name: String(row[categoryField] ?? ''),
          value: Number(row[valueField] ?? 0),
        })),
      },
    ],
  };
}

export function isSupportedChartType(chartType: string): boolean {
  return ['bar', 'line', 'area', 'scatter', 'pie'].includes(chartType);
}
