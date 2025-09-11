import {Group, Title, Tooltip, useMantineTheme} from "@mantine/core";
import { type ReactNode } from "react";
import { TbCompass, TbDatabase, TbTerminal2 } from "react-icons/tb";
import { Link } from "react-router";

export default function Topbar() {
    const theme = useMantineTheme();
    const iconColor = theme.colors.primary[1];
    
    const tabs: { key: string; label: string; icon: ReactNode }[] = [        
        { key: "data", label: "Data", icon: <TbDatabase size={20} color={iconColor}  /> },
        { key: "explore", label: "Explore", icon: <TbCompass size={20} color={iconColor} /> },
        { key: "chat", label: "Chat", icon: <TbTerminal2 size={20} color={iconColor}  /> },
   ];

    return (
    <Group bg="primary.9"  h="100%" p={12} justify="space-between">
        <Title order={3} c="primary.1">Mill Grinder </Title>
        
        <Group>
            { tabs.map(tab => (
                <Link  key={tab.key} to={'/' + tab.key}>
                    <Tooltip label={tab.label} position="bottom" withArrow bg="primary.9">
                        {tab.icon}
                    </Tooltip>
                </Link>
            ))}
        </Group>
    </Group>

    )
}