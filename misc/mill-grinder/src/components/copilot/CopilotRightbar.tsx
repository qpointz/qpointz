export default function CopilotRightbar() {
  return (
    <aside className="w-72 bg-white border-l p-4 h-full flex flex-col">
      <div className="font-semibold mb-2">Copilot Assistant</div>
      <div className="text-sm text-gray-700 mb-6">
        <div className="mb-2 font-bold">Generated SQL & Insights</div>
        <div className="text-xs text-gray-500">
          This panel will show the SQL Copilot’s latest output, documentation, or previews. Integrate with your API to display real results here.
        </div>
      </div>
      <div className="mt-4 text-xs text-gray-400">
        <hr className="mb-3"/>
        <div>Copilot: Natural language to SQL and beyond.</div>
      </div>
    </aside>
  );
}