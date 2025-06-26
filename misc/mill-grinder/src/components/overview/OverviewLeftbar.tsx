import { NavLink } from "react-router-dom";

export default function OverviewLeftbar() {
  return (
    <aside className="w-56 bg-white border-r p-4 h-full flex flex-col">
      <h2 className="font-semibold mb-4">Overview Sections</h2>
      <nav className="flex flex-col space-y-2 text-gray-700 text-sm">
        <NavLink
          to="/overview/summary"
          className={({ isActive }) =>
            isActive
              ? "bg-blue-50 rounded px-2 py-1 font-semibold text-blue-700"
              : "hover:bg-gray-100 rounded px-2 py-1"
          }
        >
          Summary
        </NavLink>
        <NavLink
          to="/overview/services"
          className={({ isActive }) =>
            isActive
              ? "bg-blue-50 rounded px-2 py-1 font-semibold text-blue-700"
              : "hover:bg-gray-100 rounded px-2 py-1"
          }
        >
          Services
        </NavLink>
        <NavLink
          to="/overview/health"
          className={({ isActive }) =>
            isActive
              ? "bg-blue-50 rounded px-2 py-1 font-semibold text-blue-700"
              : "hover:bg-gray-100 rounded px-2 py-1"
          }
        >
          Health Check
        </NavLink>
      </nav>
    </aside>
  );
}