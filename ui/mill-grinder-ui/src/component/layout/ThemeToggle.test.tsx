import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../../test/TestWrapper';
import { ThemeToggle } from './ThemeToggle';

describe('ThemeToggle', () => {
    it('renders without crashing', () => {
        render(<ThemeToggle />, { wrapper: TestWrapper });
        expect(screen.getByRole('button')).toBeInTheDocument();
    });

    it('has toggle color scheme label', () => {
        render(<ThemeToggle />, { wrapper: TestWrapper });
        expect(screen.getByLabelText('Toggle color scheme')).toBeInTheDocument();
    });
});
