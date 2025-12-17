import { Divider, ScrollArea, Stack } from '@mantine/core';
import type { ReactNode } from 'react';
import { NavItem } from './NavItem';
import { TbTerminal2, TbCompass, TbFocusCentered } from 'react-icons/tb';

interface AppSidebarProps {
    children?: ReactNode;
}

export function AppSidebar({ children }: AppSidebarProps) {
    return (
        <Stack h="100%" gap={0}>
            {/* Top Navigation */}
            <Stack gap="xs" p="xs">
                <NavItem
                    to="/chat"
                    label="Chat"
                    icon={<TbTerminal2 size={20} />}
                />
                <NavItem
                    to="/data-model"
                    label="Data Model"
                    icon={<TbCompass size={20} />}
                />
                <NavItem
                    to="/context"
                    label="Context"
                    icon={<TbFocusCentered size={20} />}
                />
            </Stack>

            <Divider my="sm" />

            {/* View-specific content */}
            <ScrollArea style={{ flex: 1 }} p="xs">
                {children}
            </ScrollArea>
        </Stack>
    );
}
