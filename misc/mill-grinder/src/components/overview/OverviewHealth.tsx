import { useState } from "react";

export default function OverviewHealth() {
  // Example health check data with statuses
  const [checks] = useState([
    { label: "Schema consistency", status: "OK" },
    { label: "Data freshness", status: "Not OK" },
    { label: "Service availability", status: "OK" },
    { label: "Query performance", status: "OK" },
    { label: "Backup status", status: "Not OK" },
  ]);

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Health Check</h1>
      <ul className="divide-y divide-gray-200 bg-white rounded shadow p-6">
        {checks.map(({ label, status }) => (
          <li key={label} className="flex justify-between py-3 items-center">
            <span className="text-gray-900">{label}</span>
            <span className={`font-semibold ${status === "OK" ? "text-green-600" : "text-red-600"}`}>
              {status}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}