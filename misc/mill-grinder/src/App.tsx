import { Routes, Route, Navigate } from "react-router-dom";
import Topbar from "./components/Topbar";
import Sidebar from "./components/Sidebar";
import ContentArea from "./components/ContentArea";

export default function App() {
    return (
        <div className="flex flex-col h-screen">
            <Topbar />
            <div className="flex flex-1 min-h-0">
                <Sidebar />
                <Routes>
                    <Route path="/" element={<Navigate replace to="/overview" />} />
                    <Route path="/overview/*" element={<ContentArea tool="overview" />} />
                    <Route path="/data/:table?" element={<ContentArea tool="data" />} />
                    <Route path="/explore/*" element={<ContentArea tool="explore" />} />
                    <Route path="/copilot/*" element={<ContentArea tool="copilot" />} />
                    <Route path="/stats/*" element={<ContentArea tool="stats" />} />
                    <Route path="*" element={<div>404 Not Found</div>} />
                </Routes>
            </div>
        </div>
    );
}