import React, { useState, useEffect, useRef } from 'react';
import ReactECharts from 'echarts-for-react';
import ReactMarkdown from 'react-markdown';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [lastSql, setLastSql] = useState(null);
  const messageEndRef = useRef(null);

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async () => {
    if (!input.trim()) return;
    const userMessage = { sender: 'user', text: input };
    setMessages(prev => [...prev, userMessage]);
    setInput('');

    const response = await fetch('/data-bot/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: input })
    });

    const data = await response.json();
    const botMessages = [];

    if (data.error) {
      botMessages.push({ sender: 'bot', text: `❌ Error: ${data.error}` });
    } else {
      if (data.comment) botMessages.push({ sender: 'bot', text: `💬 ${data.comment}` });
      if (data.query?.type === 'sql' && data.query.sql) {
        setLastSql(data.query.sql);
        botMessages.push({ sender: 'bot', text: <pre>{data.query.sql}</pre> });
      } else if (data.query?.type === 'substrait' && data.query.plan) {
        botMessages.push({ sender: 'bot', text: <pre>{data.query.plan}</pre> });
      }
      if (data.confidence) {
        botMessages.push({ sender: 'bot', text: `🔎 Confidence: ${data.confidence.rate} (${data.confidence.level})` });
      }
      if (data.output === 'table' && data.result) {
        botMessages.push({ sender: 'bot', component: renderTable(data.result.columns, data.result.rows) });
      } else if (data.output === 'chart' && data.result && data.chart) {
        botMessages.push({ sender: 'bot', component: renderChart(data.chart, data.result) });
      } else if (data.output === 'pivot' && data.pivot) {
        botMessages.push({ sender: 'bot', text: <pre>{JSON.stringify(data.pivot.config, null, 2)}</pre> });
      } else if (data.output === 'describe') {
        botMessages.push({ sender: 'bot', component: renderMarkdown(data.describe) });        
      }
    }
    console.log(data)

    setMessages(prev => [...prev, ...botMessages]);
  };

  const exportToExcel = async () => {
    if (!lastSql) return alert('No available SQL query for export');
    const response = await fetch('/data-bot/export', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sql: lastSql })
    });

    if (!response.ok) {
      alert('Failed to export to Excel');
      return;
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'result.xlsx';
    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  const renderMarkdown = (markdown) => {
    <ReactMarkdown>{markdown}</ReactMarkdown>
  }

  const renderTable = (columns, rows) => (
    <table className="table table-bordered table-sm mt-2">
      <thead>
        <tr>{columns.map((col, i) => <th key={i}>{col}</th>)}</tr>
      </thead>
      <tbody>
        {rows.map((row, i) => (
          <tr key={i}>{row.map((cell, j) => <td key={j}>{cell}</td>)}</tr>
        ))}
      </tbody>
    </table>
  );

  const dataByName = (colName, columns, rows) => {
    const idx = columns.indexOf(colName);
    const data = rows.map(row => row[idx]);
    return data;
  }

  const renderChart = (chart, result) => {
    const type = chart.type || 'bar';
    const labels = result.columns.map(row => row[0]);
    const values = result.rows.map(row => row[1]);
    let cfg = chart.config || {};
    let option = {};
    let columns = result.columns;


    switch (type) {
      case 'bar':
        option = {
          tooltip: {},
          xAxis: { type: 'category', data: dataByName(cfg.xAxis.data, columns, result.rows) },
          yAxis: { type: 'value' },
          series: [{ type: 'bar', data: dataByName(cfg.series[0].data, columns, result.rows) }]
        };
        break;
      case 'pie':
        option = {
          tooltip: {},
          series: [{
            type: 'pie',
            radius: '60%',
            data: result.rows.map(row => ({ name: row[0], value: row[1] }))
          }]
        };
        break;
      case 'radar':
        const indicators = result.rows.map(row => ({
          name: row[0],
          max: Math.max(...values) * 1.2
        }));
        option = {
          tooltip: {},
          radar: { indicator: indicators },
          series: [{
            type: 'radar',
            data: [{ value: values, name: chart.config?.label || 'Values' }]
          }]
        };
        break;
      case 'heatmap':
        option = {
          tooltip: {},
          xAxis: { type: 'category', data: labels },
          yAxis: { type: 'category', data: ['Value'] },
          visualMap: { min: 0, max: Math.max(...values), calculable: true, orient: 'horizontal' },
          series: [{
            type: 'heatmap',
            data: values.map((v, i) => [i, 0, v]),
            label: { show: true }
          }]
        };
        break;
      case 'sunburst':
        option = {
          series: {
            type: 'sunburst',
            data: result.rows.map(([name, value]) => ({ name, value })),
            radius: [0, '90%'],
            label: { rotate: 'radial' }
          }
        };
        break;
      default:
        return <div className="text-danger">Unsupported chart type: {type}</div>;
    }

    return (
      <div style={{ width: '100%', maxWidth: 500, height: 300 }}>
        <ReactECharts option={option} style={{ height: '100%' }} />
      </div>
    );
  };

  return (
    <div className="container py-4">
      <h2 className="mb-3">Data Bot</h2>
      <div className="border rounded p-3 mb-4 overflow-auto" style={{ height: "100%" }}>
        {messages.map((msg, i) => (
          <div key={i} className={msg.sender === 'user' ? 'fw-bold text-primary mb-2' : 'text-success mb-2'}>
            {msg.component || msg.text}
          </div>
        ))}
        <div ref={messageEndRef} />
      </div>
      <div className="input-group mb-2">
        <input
          className="form-control"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Enter your query..."
        />
        <button className="btn btn-primary" onClick={sendMessage}>Send</button>
        <button className="btn btn-success invisible" onClick={exportToExcel}>Export to Excel</button>
      </div>
    </div>
  );
}

export default App;
