import React from "react";
const ReactECharts = React.lazy(() => import('echarts-for-react'));

export default function ChartView(data: any) {
    const { chart, container } = data;

    const labels = container?.data.map((r: string)=> r[0]);
    const values = container.data.map((r: any) => r[1]);

    const options = {
        tooltip: {},
        xAxis: chart.type === 'bar' ? { type: 'category', data: labels } : undefined,
        yAxis: chart.type === 'bar' ? { type: 'value' } : undefined,
        series: [
            {
                type: chart.type,
                data: chart.type === 'pie' || chart.type === 'sunburst' || chart.type === 'treemap'
                    ? labels.map((label: string, i: number) => ({ name: label, value: values[i] }))
                    : values,
                radius: chart.type === 'pie' ? '50%' : undefined,
                label: { show: true },
            }
        ]
    };

    return (
        <ReactECharts option={options} style={{ height: '100%', minHeight:'300px' }} />
    )
}