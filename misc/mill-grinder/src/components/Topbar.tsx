import { FiUser, FiBell, FiSettings } from "react-icons/fi";

export default function Topbar() {
  return (
    <header className="h-12 bg-grey text-gray-900 flex items-center px-6 shadow border-b">
      <span className="font-bold text-lg tracking-wide">My Dashboard 2</span>
      <div className="ml-auto flex items-center gap-6">
        {/* Global nav icons on the right */}
        <button 
          className="hover:text-blue-600 transition focus:outline-none" 
          title="Notifications"
        >
          <FiBell size={20} />
        </button>
        <button 
          className="hover:text-blue-600 transition focus:outline-none" 
          title="Settings"
        >
          <FiSettings size={20} />
        </button>
        <button 
          className="hover:text-blue-600 transition focus:outline-none rounded-full border border-gray-200 p-1" 
          title="Profile"
        >
          <FiUser size={20} />
        </button>
      </div>
    </header>
  );
} 