export default function ExploreRightbar() {
  return (
    <aside className="w-72 bg-white border-l p-4 h-full flex flex-col">
      <div className="font-semibold mb-2">Insights & Context</div>
      <ul className="text-sm text-gray-600 space-y-2">
        <li>Recently Explored</li>
        <li>Suggestions</li>
        <li>Popular Datasets</li>
      </ul>
    </aside>
  );
}