import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../../test/TestWrapper';
import { AppHeader } from './AppHeader';

describe('AppHeader', () => {
    it('renders without crashing', () => {
        render(
            <AppHeader navbarOpened={true} onToggleNavbar={vi.fn()} />,
            { wrapper: TestWrapper }
        );
        expect(screen.getByText('Mill Grinder')).toBeInTheDocument();
    });

    it('renders theme toggle button', () => {
        render(
            <AppHeader navbarOpened={true} onToggleNavbar={vi.fn()} />,
            { wrapper: TestWrapper }
        );
        expect(screen.getByLabelText('Toggle color scheme')).toBeInTheDocument();
    });
});
