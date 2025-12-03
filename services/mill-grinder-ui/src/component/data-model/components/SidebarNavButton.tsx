import { Box, NavLink, UnstyledButton } from "@mantine/core";
import { Link, useLocation } from "react-router";
import { useMantineTheme } from "@mantine/core";
import { ReactNode } from "react";

interface SidebarNavButtonProps {
    to: string;
    label: string;
    icon: ReactNode;
    active?: boolean;
    collapsed: boolean;
}

export default function SidebarNavButton({ to, label, icon, active, collapsed }: SidebarNavButtonProps) {
    const theme = useMantineTheme();
    const location = useLocation();
    
    const isActive = active !== undefined ? active : location.pathname.startsWith(to);

    if (collapsed) {
        return (
            <Box p={1} mb={4} bg="transparent" style={{ borderRadius: 6 }}>
                <UnstyledButton
                    component={Link}
                    to={to}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: '100%',
                        padding: '6px',
                        borderRadius: 6,
                    }}
                    onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                    onMouseLeave={e => e.currentTarget.style.background = ''}
                >
                    {icon}
                </UnstyledButton>
            </Box>
        );
    }

    return (
        <Box p={1} mb={8} bg="transparent" style={{ borderRadius: 6 }}>
            <NavLink 
                to={to} 
                component={Link} 
                label={label} 
                p={0} 
                m={0} 
                leftSection={icon}
                active={isActive}
            />
        </Box>
    );
}

