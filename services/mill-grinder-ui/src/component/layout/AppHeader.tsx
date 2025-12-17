import { Group, Text, ActionIcon, Burger } from '@mantine/core';
import { ThemeToggle } from './ThemeToggle';

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
                <Text fw={600} size="lg">Mill Grinder</Text>
            </Group>
            <Group>
                <ActionIcon
                    variant="subtle"
                    size="lg"
                    onClick={onToggleNavbar}
                    visibleFrom="sm"
                    aria-label="Toggle navigation"
                >
                    {navbarOpened ? '←' : '→'}
                </ActionIcon>
                <ThemeToggle />
            </Group>
        </Group>
    );
}
