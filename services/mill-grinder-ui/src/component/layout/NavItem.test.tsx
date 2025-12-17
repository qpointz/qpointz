import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TestWrapper } from '../../test/TestWrapper';
import { NavItem } from './NavItem';
import { TbHome } from 'react-icons/tb';

describe('NavItem', () => {
    it('renders without crashing', () => {
        render(
            <NavItem to="/test" label="Test Label" />,
            { wrapper: TestWrapper }
        );
        expect(screen.getByText('Test Label')).toBeInTheDocument();
    });

    it('renders with icon', () => {
        render(
            <NavItem to="/test" label="Test" icon={<TbHome data-testid="icon" />} />,
            { wrapper: TestWrapper }
        );
        expect(screen.getByTestId('icon')).toBeInTheDocument();
    });

    it('renders as link to correct path', () => {
        render(
            <NavItem to="/test-path" label="Test" />,
            { wrapper: TestWrapper }
        );
        const link = screen.getByRole('link');
        expect(link).toHaveAttribute('href', '/test-path');
    });
});
