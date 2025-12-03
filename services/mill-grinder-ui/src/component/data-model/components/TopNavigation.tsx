import SidebarNavButton from "./SidebarNavButton";
import { TbTerminal2, TbCompass, TbBulb } from "react-icons/tb";

interface TopNavigationProps {
    collapsed: boolean;
}

export default function TopNavigation({ collapsed }: TopNavigationProps) {
    return (
        <>
            <SidebarNavButton
                to="/chat"
                label="Chat"
                icon={<TbTerminal2 size={20} />}
                collapsed={collapsed}
            />

            <SidebarNavButton
                to="/data-model"
                label="Data Model"
                icon={<TbCompass size={20} />}
                collapsed={collapsed}
            />

            <SidebarNavButton
                to="/concepts"
                label="Concept"
                icon={<TbBulb size={20} />}
                collapsed={collapsed}
            />
        </>
    );
}

