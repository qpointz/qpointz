import { useState } from "react";
import type {DataLeftbarProps} from './Data.tsx'

const mockColumns = [
  { name: "id", type: "integer" },
  { name: "name", type: "varchar" },
  { name: "email", type: "varchar" },
];

const mockLogs = [
  { time: '2024-06-01 10:22', event: 'Query Executed' },
  { time: '2024-06-01 10:02', event: 'Table Created' },
];

const mockRecentQueries = [
  'SELECT * FROM users;',
  'SELECT id, total FROM orders;',
];

export default function DataRightbar({ selectedTable }: DataLeftbarProps) {
  const [openSections, setOpenSections] = useState({
    docs: true,
    info: false,
    logs: false,
  });
  const [docs, setDocs] = useState("**users** table\n\nContains user accounts... (You can edit this text)");

  function toggle(section: keyof typeof openSections) {
    setOpenSections((prev) => ({ ...prev, [section]: !prev[section] }));
  }

  return (
    <aside className="w-72 bg-white border-l p-4 flex-shrink-0 flex flex-col gap-2 overflow-auto">
      {/* Docs Section */}
      <div>
        <button
          className="w-full text-left text-xs font-semibold py-1 uppercase flex items-center gap-1 text-gray-700 hover:text-blue-600"
          onClick={() => toggle('docs')}
        >
          <span>{openSections.docs ? '▼' : '►'} Table Docs</span>
        </button>
        {openSections.docs && (
          <textarea
            className="w-full h-24 p-2 border rounded text-sm mb-2 resize-y text-gray-700 font-mono"
            value={docs}
            onChange={e => setDocs(e.target.value)}
          />
        )}
      </div>
      {/* Info Section */}
      <div>
        <button
          className="w-full text-left text-xs font-semibold py-1 uppercase flex items-center gap-1 text-gray-700 hover:text-blue-600"
          onClick={() => toggle('info')}
        >
          <span>{openSections.info ? '▼' : '►'} Table Info</span>
        </button>
        {openSections.info && (
          <>
            <table className="text-xs w-full border mb-2">
              <thead>
                <tr>
                  <th className="font-semibold text-left p-1 border-b bg-gray-50">Column</th>
                  <th className="font-semibold text-left p-1 border-b bg-gray-50">Type</th>
                </tr>
              </thead>
              <tbody>
                {mockColumns.map((col) => (
                  <tr key={col.name}>
                    <td className="p-1 border-b">{col.name}</td>
                    <td className="p-1 border-b">{col.type}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="text-xs text-gray-500 mb-2">Rows: 1002 (mocked)</div>
          </>
        )}
      </div>
      {/* Logs Section */}
      <div>
        <button
          className="w-full text-left text-xs font-semibold py-1 uppercase flex items-center gap-1 text-gray-700 hover:text-blue-600"
          onClick={() => toggle('logs')}
        >
          <span>{openSections.logs ? '▼' : '►'} Operational Info</span>
        </button>
        {openSections.logs && (
          <div>
            <div className="text-xs text-gray-400 mb-1">Logs</div>
            <ul className="text-xs mb-2">
              {mockLogs.map((log, i) => (
                <li key={i} className="mb-1 text-gray-700">[{log.time}] {log.event}</li>
              ))}
            </ul>
            <div className="text-xs text-gray-400 mb-1">Recent Queries</div>
            <ul className="text-xs">
              {mockRecentQueries.map((q, i) => (
                <li key={i} className="mb-1 text-gray-700 font-mono">{q}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </aside>
  );
}