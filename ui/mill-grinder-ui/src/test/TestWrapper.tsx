import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { theme } from '../theme';

interface TestWrapperProps {
    children: React.ReactNode;
    initialEntries?: string[];
}

export function TestWrapper({ children, initialEntries = ['/'] }: TestWrapperProps) {
    return (
        <MemoryRouter initialEntries={initialEntries}>
            <MantineProvider theme={theme}>
                {children}
            </MantineProvider>
        </MemoryRouter>
    );
}
