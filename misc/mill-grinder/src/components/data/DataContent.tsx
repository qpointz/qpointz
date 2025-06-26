import { useState, useMemo } from "react";
import type { DataLeftbarProps } from './Data.tsx';
import {
  useReactTable,
  getCoreRowModel,
  flexRender,
  ColumnDef,
} from '@tanstack/react-table';

function runQuery(sql: string) {
  if (sql.toLowerCase().includes('users')) {
    return {
      columns: ['id', 'name', 'email'],
      rows: [
        [1, 'Alice', 'alice@email.com'],
        [2, 'Bob', 'bob@email.com'],
      ],
    };
  }
  return { columns: ['example'], rows: [['Run your SQL']] };
}

function QueryEditor({ sql, setSql, onRun }: { sql: string; setSql: (text: string) => void; onRun: () => void }) {
  return (
    <section>
      <div className="flex mb-2">
        <span className="text-sm font-semibold text-gray-700 flex-1">SQL Editor</span>
        <button
          onClick={onRun}
          className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 text-xs font-medium"
        >
          Run
        </button>
      </div>
      <textarea
        className="w-full h-20 p-2 border rounded font-mono resize-y text-sm focus:outline-none focus:ring-2 focus:ring-blue-100"
        value={sql}
        onChange={(e) => setSql(e.target.value)}
        spellCheck={false}
      />
    </section>
  );
}

type QueryResultTableProps = {
  columns: string[];
  rows: any[][];
};
function QueryResultTable({ columns, rows }: QueryResultTableProps) {
  const tableColumns = useMemo<ColumnDef<Record<string, any>>[]>(() => {
    return columns.map((col) => ({
      accessorKey: col,
      header: col.charAt(0).toUpperCase() + col.slice(1),
      cell: info => info.getValue(),
    }));
  }, [columns]);

  const data = useMemo(() =>
    rows.map((row) => {
      const obj: Record<string, any> = {};
      columns.forEach((col, idx) => {
        obj[col] = row[idx];
      });
      return obj;
    }), [rows, columns]);

  const table = useReactTable({
    data,
    columns: tableColumns,
    getCoreRowModel: getCoreRowModel(),
  });
  return (
    <div className="bg-white shadow rounded border p-2 overflow-auto">
      <table className="min-w-full divide-y divide-gray-200 text-sm">
        <thead>
          {table.getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th key={header.id} className="px-3 py-2 text-left border-b font-semibold text-gray-600 bg-gray-50">
                  {flexRender(header.column.columnDef.header, header.getContext())}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {table.getRowModel().rows.map(row => (
            <tr key={row.id}>
              {row.getVisibleCells().map(cell => (
                <td key={cell.id} className="px-3 py-2 border-b">
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default function DataContent({ selectedTable }: DataLeftbarProps) {
  const [sql, setSql] = useState('SELECT * FROM users;');
  const [results, setResults] = useState<{ columns: string[], rows: any[][] } | null>(null);
  const [actualTable, setActualTable] = useState<string>(null);

  function handleRun() {
    setResults(runQuery(sql));
  }

  return (
    <section className="flex-1 p-6 overflow-auto flex flex-col min-w-0">
      <QueryEditor sql={sql} setSql={setSql} onRun={handleRun} />
      <div className="mt-4 flex-1">
        {results && <QueryResultTable columns={results.columns} rows={results.rows} />}
      </div>
    </section>
  );
}
