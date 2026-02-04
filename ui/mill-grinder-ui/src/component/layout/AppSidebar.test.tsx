import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../../test/TestWrapper';
import { AppSidebar } from './AppSidebar';

describe('AppSidebar', () => {
    it('renders without crashing', () => {
        render(<AppSidebar />, { wrapper: TestWrapper });
        expect(screen.getByText('Chat')).toBeInTheDocument();
    });

    it('renders all navigation links', () => {
        render(<AppSidebar />, { wrapper: TestWrapper });
        expect(screen.getByText('Chat')).toBeInTheDocument();
        expect(screen.getByText('Data Model')).toBeInTheDocument();
        expect(screen.getByText('Context')).toBeInTheDocument();
    });

    it('renders children content', () => {
        render(
            <AppSidebar>
                <div data-testid="child-content">Test Content</div>
            </AppSidebar>,
            { wrapper: TestWrapper }
        );
        expect(screen.getByTestId('child-content')).toBeInTheDocument();
    });
});
