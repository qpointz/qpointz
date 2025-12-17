import { NavLink } from '@mantine/core';
import { Link, useLocation } from 'react-router';
import type { ReactNode } from 'react';

interface NavItemProps {
    to: string;
    label: string;
    icon?: ReactNode;
    active?: boolean;
    rightSection?: ReactNode;
    onClick?: () => void;
}

export function NavItem({ to, label, icon, active, rightSection, onClick }: NavItemProps) {
    const location = useLocation();
    const isActive = active !== undefined ? active : location.pathname.startsWith(to);

    return (
        <NavLink
            component={Link}
            to={to}
            label={label}
            leftSection={icon}
            rightSection={rightSection}
            active={isActive}
            onClick={onClick}
        />
    );
}
