import { ActionIcon, useMantineColorScheme } from '@mantine/core';
import { TbSun, TbMoon } from 'react-icons/tb';

export function ThemeToggle() {
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();

    return (
        <ActionIcon
            variant="subtle"
            size="lg"
            onClick={toggleColorScheme}
            aria-label="Toggle color scheme"
        >
            {colorScheme === 'dark' ? <TbSun size={20} /> : <TbMoon size={20} />}
        </ActionIcon>
    );
}
