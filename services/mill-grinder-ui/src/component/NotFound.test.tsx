import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../test/TestWrapper';
import NotFound from './NotFound';

describe('NotFound', () => {
    it('renders without crashing', () => {
        render(<NotFound />, { wrapper: TestWrapper });
        expect(screen.getByText('Page Not Found')).toBeInTheDocument();
    });

    it('renders descriptive message', () => {
        render(<NotFound />, { wrapper: TestWrapper });
        expect(screen.getByText("The page you're looking for doesn't exist.")).toBeInTheDocument();
    });

    it('renders home link', () => {
        render(<NotFound />, { wrapper: TestWrapper });
        const homeLink = screen.getByRole('link', { name: /go to home/i });
        expect(homeLink).toHaveAttribute('href', '/');
    });
});
