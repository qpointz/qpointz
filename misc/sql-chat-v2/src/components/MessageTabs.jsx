import React, { useState } from 'react';
import ChartViewer from './ChartViewer';
import TableViewer from './TableViewer';
import SqlViewer from './SqlViewer';
import ExportMenu from './ExportMenu';

export default function MessageTabs({ message }) {
  const [tab, setTab] = useState('visual');
  const { response, error } = message;

  if (error) return <div className="alert alert-danger">Error: {error}</div>;

  const result = response?.result;
  const chart = response?.chart;
  const output = response?.output;

  const hasVisual = output === 'chart' || output === 'table';
  const hasSql = response?.query?.sql;

  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <ul className="nav nav-tabs card-header-tabs">
          {hasVisual && (
            <li className="nav-item">
              <button className={`nav-link ${tab === 'visual' ? 'active' : ''}`} onClick={() => setTab('visual')}>
                Visual
              </button>
            </li>
          )}
          {hasSql && (
            <li className="nav-item">
              <button className={`nav-link ${tab === 'sql' ? 'active' : ''}`} onClick={() => setTab('sql')}>
                SQL
              </button>
            </li>
          )}
        </ul>
        {hasSql && (
          <ExportMenu onExport={(type) => {
            fetch(`/data-bot/export?format=${type}`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ sql: response.query.sql })
            })
              .then(res => res.blob())
              .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `result.${type === 'excel' ? 'xlsx' : 'csv'}`;
                document.body.appendChild(a);
                a.click();
                a.remove();
              });
          }} />
        )}
      </div>
      <div className="card-body">
        {tab === 'visual' && response && (
          output === 'chart'
            ? <ChartViewer chart={chart} result={result} />
            : <TableViewer columns={result?.columns} rows={result?.rows} />
        )}
        {tab === 'sql' && hasSql && <SqlViewer sql={response.query.sql} />}
      </div>
    </div>
  );
}
