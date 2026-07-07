import ReactECharts from 'echarts-for-react';
import { useEffect, useMemo, useRef } from 'react';
import { Box, Text } from '@mantine/core';
import { compileChartSpecToECharts, isSupportedChartType } from './compileChartSpecToECharts';
import type { ChartSnapshotRow, ChartVisualizationConfig } from './types';
import { chartTheme } from './chartTheme';

export interface ChartRendererProps {
  config: ChartVisualizationConfig;
  rows: ChartSnapshotRow[];
  truncated?: boolean;
  height?: number | string;
}

/**
 * Reusable chart renderer — accepts plain config and data props only.
 */
export function ChartRenderer({ config, rows, truncated = false, height = 280 }: ChartRendererProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const option = useMemo(() => compileChartSpecToECharts(config, rows), [config, rows]);

  useEffect(() => {
    const observer = new ResizeObserver(() => {
      window.dispatchEvent(new Event('resize'));
    });
    if (containerRef.current) observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, []);

  if (!isSupportedChartType(config.chartType)) {
    return <Text size="sm" c="dimmed">Unsupported chart type: {config.chartType}</Text>;
  }
  if (!rows.length) {
    return <Text size="sm" c="dimmed">No data loaded for chart.</Text>;
  }
  if (!option) {
    return <Text size="sm" c="dimmed">Unable to compile chart configuration.</Text>;
  }
  if (truncated) {
    return <Text size="sm" c="orange">Snapshot truncated — chart may be incomplete.</Text>;
  }

  return (
    <Box ref={containerRef} style={{ height, width: '100%' }}>
      <ReactECharts option={option} theme={chartTheme} style={{ height: '100%', width: '100%' }} notMerge lazyUpdate />
    </Box>
  );
}
