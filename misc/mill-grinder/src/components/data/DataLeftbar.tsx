import React, {useEffect, useState} from "react";
import type {DataLeftbarProps} from './Data.tsx'
import {NavLink} from "react-router-dom";

const schemaData = [
  {
    schema: "public",
    tables: ["users", "orders", "products"],
  },
  {
    schema: "analytics",
    tables: ["sessions", "events"],
  },
];



export default function DataLeftbar({ selectedTable }: DataLeftbarProps) {
  const [activeTable, setActiveTable] = useState<{ schema: string; table: string }>({
    schema: "public",
    table: "users",
  });

  useEffect(() => {
    if (!selectedTable) return;
    for (const schema of schemaData) {
      if (schema.tables.includes(selectedTable)) {
        setActiveTable({ schema: schema.schema, table: selectedTable });
        break;
      }
    }
  }, [selectedTable]);
  return (
      <aside className="w-56 bg-white border-r p-4 flex flex-col h-full">
        <div className="flex-1 overflow-y-auto">
          {schemaData.map((schema) => (
              <div key={schema.schema} className="mb-4">
                <div className="font-semibold text-xs text-gray-400">{schema.schema}</div>
                <ul>
                  {schema.tables.map((table) => (
                      <li>
                        <NavLink
                            to={"/data/"+ table}
                            onClick={() => setActiveTable({ schema: schema.schema, table })}
                            className={({ isActive }) =>
                                isActive
                                    ? "bg-blue-50 rounded px-2 py-1 font-semibold text-blue-700 text-sm"
                                    : "hover:bg-gray-100 rounded px-2 py-1 text-sm"
                            }
                        >
                          {table}
                        </NavLink>
                      </li>
                  ))}
                </ul>
              </div>
          ))}
        </div>
      </aside>
  );
}