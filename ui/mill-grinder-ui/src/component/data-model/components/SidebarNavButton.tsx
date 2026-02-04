import { NavLink, ActionIcon, Tooltip } from "@mantine/core";
import { Link, useLocation } from "react-router";
import type { ReactNode } from "react";

interface SidebarNavButtonProps {
    to: string;
    label: string;
    icon: ReactNode;
    active?: boolean;
    collapsed: boolean;
}

export default function SidebarNavButton({ to, label, icon, active, collapsed }: SidebarNavButtonProps) {
    const location = useLocation();
    const isActive = active !== undefined ? active : location.pathname.startsWith(to);

    if (collapsed) {
        return (
            <Tooltip label={label} position="right">
                <ActionIcon
                    component={Link}
                    to={to}
                    variant={isActive ? "light" : "subtle"}
                    size="lg"
                    mb="xs"
                >
                    {icon}
                </ActionIcon>
            </Tooltip>
        );
    }

    return (
        <NavLink 
            component={Link}
            to={to} 
            label={label} 
            leftSection={icon}
            active={isActive}
            mb="xs"
        />
    );
}

