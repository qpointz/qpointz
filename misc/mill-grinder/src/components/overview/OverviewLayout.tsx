import { Routes, Route, NavLink } from "react-router-dom";
import OverviewLeftbar from "./OverviewLeftbar";
import OverviewRightbar from "./OverviewRightbar";
import OverviewSummary from "./OverviewSummary";
import OverviewServices from "./OverviewServices";
import OverviewHealth from "./OverviewHealth";

export default function OverviewLayout() {
  return (    
    <main className="flex flex-1 bg-gray-50 min-h-0">      
      <OverviewLeftbar />
      <section className="flex-1 p-8 overflow-auto">
        <Routes>
          <Route path="summary" element={<OverviewSummary />} />
          <Route path="services" element={<OverviewServices />} />
          <Route path="health" element={<OverviewHealth />} />
          <Route path="*" element={<OverviewSummary />} />
        </Routes>
      </section>
      <OverviewRightbar />
    </main>
  );
}