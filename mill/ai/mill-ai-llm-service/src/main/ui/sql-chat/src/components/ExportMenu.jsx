import React from 'react';

export default function ExportMenu({ onExport }) {
  return (
    <div className="dropdown mb-2">
      <button
        className="btn btn-outline-secondary dropdown-toggle btn-sm"
        type="button"
        data-bs-toggle="dropdown"
        aria-expanded="false"
      >
        Export to...
      </button>
      <ul className="dropdown-menu">
        <li><button className="dropdown-item" onClick={() => onExport('excel')}>Excel</button></li>
        <li><button className="dropdown-item" onClick={() => onExport('csv')}>CSV</button></li>
      </ul>
    </div>
  );
}
