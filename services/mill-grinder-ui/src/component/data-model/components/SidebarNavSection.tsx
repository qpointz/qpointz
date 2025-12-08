import { Divider } from "@mantine/core";
import type { ReactNode } from "react";

interface SidebarNavSectionProps {
    children: ReactNode;
    collapsed: boolean;
    showDivider?: boolean;
}

export default function SidebarNavSection({ children, collapsed, showDivider = true }: SidebarNavSectionProps) {
    return (
        <>
            {!collapsed && showDivider && <Divider mt={15} />}
            {children}
        </>
    );
}

