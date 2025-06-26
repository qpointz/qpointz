import type { ReactNode } from "react";
import { HiOutlineHome, HiOutlineCommandLine } from "react-icons/hi2";
import { HiOutlineDatabase } from "react-icons/hi";
import { FiCompass } from "react-icons/fi";
import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
    const location = useLocation();
    const TABS: { key: string; label: string; icon: ReactNode }[] = [
        { key: "overview", label: "Overview", icon: <HiOutlineHome size={20} /> },
        { key: "data", label: "Data", icon: <HiOutlineDatabase size={20} /> },
        { key: "explore", label: "Explore", icon: <FiCompass size={20} /> },
        { key: "copilot", label: "Copilot", icon: <HiOutlineCommandLine size={20} /> },
    ];
    return (
        <aside className="h-full flex flex-col w-20 bg-blue-950 border-r border-blue-800 py-2 items-center">
            <nav className="flex flex-col gap-2 flex-1 mt-3">
                {TABS.map(tab => (
                    <Link
                        key={tab.key}
                        to={tab.key === "data" ? "/data" : `/${tab.key}`}
                        className={`w-14 h-14 flex flex-col items-center justify-center rounded transition
              ${
                            location.pathname.startsWith(`/${tab.key}`) ||
                            (tab.key === "data" && location.pathname.startsWith('/data'))
                                ? "bg-white text-blue-900 shadow border border-blue-200"
                                : "text-blue-200 hover:text-white hover:bg-blue-800"
                        }`}
                        title={tab.label}
                    >
                        {tab.icon}
                        <span className="text-[11px] mt-1 font-semibold tracking-wide" style={{ letterSpacing: '0.03em' }}>
              {tab.label}
            </span>
                    </Link>
                ))}
            </nav>
        </aside>
    );
}