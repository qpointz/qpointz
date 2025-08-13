import {Stack, Tooltip, useMantineTheme} from "@mantine/core";
import type {ReactNode} from "react";
import {Link} from "react-router";
import {TbCompass, TbDatabase, TbHome, TbTerminal2} from "react-icons/tb";

export default function Navbar() {
    const theme = useMantineTheme();
    const iconColor = theme.colors.primary[8];

     const tabs: { key: string; label: string; icon: ReactNode }[] = [
          { key: "overview", label: "Overview", icon: <TbHome size={20} color={iconColor}  /> },
          { key: "data", label: "Data", icon: <TbDatabase size={20} color={iconColor}  /> },
          { key: "explore", label: "Explore", icon: <TbCompass size={20} color={iconColor} /> },
          { key: "chat", label: "Chat", icon: <TbTerminal2 size={20} color={iconColor}  /> },
     ];

    return (
        <Stack bg="primary.1" h="100%" w="100%" p={3} align="center" pt={10} >
            { tabs.map(tab => (
                <Link  key={tab.key} to={'/' + tab.key}>
                    <Tooltip label={tab.label} position="right" withArrow bg="primary.9">
                        {tab.icon}
                    </Tooltip>
                </Link>
            ))}
        </Stack>
    )
}