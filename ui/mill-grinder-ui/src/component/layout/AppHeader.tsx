import { ActionIcon, Burger, Group, Text, Tooltip } from '@mantine/core';
import { ThemeToggle } from './ThemeToggle';
import { TbLayoutSidebarLeftCollapse, TbLayoutSidebarLeftExpand } from 'react-icons/tb';

interface AppHeaderProps {
    navbarOpened: boolean;
    onToggleNavbar: () => void;
}

export function AppHeader({ navbarOpened, onToggleNavbar }: AppHeaderProps) {
    return (
        <Group h="100%" px="md" justify="space-between">
            <Group>
                <Burger
                    opened={navbarOpened}
                    onClick={onToggleNavbar}
                    hiddenFrom="sm"
                    size="sm"
                />
                <Group gap="xs">
                    <img
                        src={`${import.meta.env.BASE_URL}logo.png`}
                        width={22}
                        height={22}
                        alt=""
                        style={{ display: 'block' }}
                    />
                    <Text fw={650} size="lg">
                        Mill Grinder
                    </Text>
                </Group>
            </Group>
            <Group>
                <Tooltip label={navbarOpened ? 'Collapse sidebar' : 'Expand sidebar'} position="bottom" withArrow>
                    <ActionIcon
                        variant="subtle"
                        size="lg"
                        onClick={onToggleNavbar}
                        visibleFrom="sm"
                        aria-label={navbarOpened ? 'Collapse sidebar' : 'Expand sidebar'}
                    >
                        {navbarOpened ? (
                            <TbLayoutSidebarLeftCollapse size={20} />
                        ) : (
                            <TbLayoutSidebarLeftExpand size={20} />
                        )}
                    </ActionIcon>
                </Tooltip>
                <ThemeToggle />
            </Group>
        </Group>
    );
}
