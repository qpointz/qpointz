import React from 'react';

export default function TableViewer({ columns, rows }) {
  if (!columns || !rows) return null;
  return (
    <div className="table-responsive">
      <table className="table table-bordered table-sm">
        <thead>
          <tr>{columns.map((col, i) => <th key={i}>{col}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i}>
              {row.map((cell, j) => <td key={j}>{cell}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
