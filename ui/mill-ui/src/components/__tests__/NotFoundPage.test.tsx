import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { NotFoundPage } from '../common/NotFoundPage';

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        {ui}
      </MantineProvider>
    </MemoryRouter>,
  );
}

describe('NotFoundPage', () => {
  it('should render 404 code', () => {
    renderWithProviders(<NotFoundPage />);
    expect(screen.getByText('404')).toBeInTheDocument();
  });

  it('should render "Page not found" title', () => {
    renderWithProviders(<NotFoundPage />);
    expect(screen.getByText('Page not found')).toBeInTheDocument();
  });

  it('should render default message when no message prop is passed', () => {
    renderWithProviders(<NotFoundPage />);
    expect(screen.getByText(/doesn.t exist or may have been moved/)).toBeInTheDocument();
  });

  it('should render custom message when message prop is provided', () => {
    renderWithProviders(<NotFoundPage message="Table 'foo' not found" />);
    expect(screen.getByText("Table 'foo' not found")).toBeInTheDocument();
  });

  it('should render navigation buttons', () => {
    renderWithProviders(<NotFoundPage />);
    expect(screen.getByText('Go to Home')).toBeInTheDocument();
    expect(screen.getByText('Go back')).toBeInTheDocument();
  });
});
