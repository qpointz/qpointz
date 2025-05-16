import React, { useState } from 'react';
import ReactECharts from 'echarts-for-react';

export default function ChartViewer({ chart, result }) {
  const [expanded, setExpanded] = useState(false);
  const labels = result.rows.map(row => row[0]);
  const values = result.rows.map(row => row[1]);

  const options = {
    tooltip: {},
    xAxis: chart.type === 'bar' ? { type: 'category', data: labels } : undefined,
    yAxis: chart.type === 'bar' ? { type: 'value' } : undefined,
    series: [
      {
        type: chart.type,
        data: chart.type === 'pie' || chart.type === 'sunburst' || chart.type === 'treemap'
          ? labels.map((label, i) => ({ name: label, value: values[i] }))
          : values,
        radius: chart.type === 'pie' ? '50%' : undefined,
        label: { show: true },
      }
    ]
  };

  return (
    <div>
      <div className="mb-2 text-end">
        <button className="btn btn-sm btn-outline-secondary" onClick={() => setExpanded(!expanded)}>
          {expanded ? 'Collapse' : 'Expand'}
        </button>
      </div>
      <div style={{ width: '100%', height: expanded ? 500 : 300 }}>
        <ReactECharts option={options} style={{ height: '100%' }} />
      </div>
    </div>
  );
}
