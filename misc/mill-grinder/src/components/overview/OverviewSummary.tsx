export default function OverviewSummary() {
  return (
    <div>
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Data Product Overview - Summary</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard label="Schemas" value={12} />
        <StatCard label="Tables" value={87} />
        <StatCard label="Attributes" value={546} />
        <StatCard label="Queries Today" value={1342} />
      </div>
      <div className="bg-white rounded shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Recent Activity</h2>
        <ul className="list-disc list-inside text-gray-700">
          <li>Monthly data refresh completed</li>
          <li>New schema 'sales' added</li>
          <li>10 tables updated with new attributes</li>
          <li>Performance improved by 15%</li>
        </ul>
      </div>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="bg-white rounded-lg shadow flex flex-col items-center justify-center p-6">
      <div className="text-4xl font-extrabold text-blue-600">{value}</div>
      <div className="mt-2 text-sm font-semibold text-gray-600">{label}</div>
    </div>
  );
}