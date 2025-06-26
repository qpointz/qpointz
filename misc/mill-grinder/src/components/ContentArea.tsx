import DataLeftbar from "./data/DataLeftbar";
import DataContent from "./data/DataContent";
import DataRightbar from "./data/DataRightbar";

import ExploreLeftbar from "./explore/ExploreLeftbar";
import ExploreContent from "./explore/ExploreContent";
import ExploreRightbar from "./explore/ExploreRightbar";

import CopilotLeftbar from "./copilot/CopilotLeftbar";
import CopilotContent from "./copilot/CopilotContent";
import CopilotRightbar from "./copilot/CopilotRightbar";

import OverviewLayout from "./overview/OverviewLayout";
import {useParams} from "react-router-dom";

export default function ContentArea({ tool }: { tool: string }) {
    const params = useParams<{ table?: string }>();

    switch (tool) {
        case "data":
            return (
                <main className="flex flex-1 bg-gray-50 min-h-0">
                    <DataLeftbar selectedTable={params.table} />
                    <DataContent selectedTable={params.table} />
                    <DataRightbar selectedTable={params.table} />
                </main>
            );
        case "overview":
            return (
                <main className="flex flex-1 bg-gray-50 min-h-0">
                    <OverviewLayout />
                </main>
            );
        case "explore":
            return (
                <main className="flex flex-1 bg-gray-50 min-h-0">
                    <ExploreLeftbar />
                    <ExploreContent />
                    <ExploreRightbar />
                </main>
            );
        case "copilot":
            return (
                <main className="flex flex-1 bg-gray-50 min-h-0">
                    <CopilotLeftbar />
                    <CopilotContent />
                    <CopilotRightbar />
                </main>
            );
        case "stats":
            return <main className="flex-1 flex items-center justify-center">Stats tool coming soon.</main>;
        default:
            return (
                <main className="flex-1 flex items-center justify-center text-2xl text-gray-400">
                    Tool coming soon.
                </main>
            );
    }
}