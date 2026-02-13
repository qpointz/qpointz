import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { AccessDeniedPage } from '../common/AccessDeniedPage';

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        {ui}
      </MantineProvider>
    </MemoryRouter>,
  );
}

describe('AccessDeniedPage', () => {
  it('should render 403 code', () => {
    renderWithProviders(<AccessDeniedPage />);
    expect(screen.getByText('403')).toBeInTheDocument();
  });

  it('should render "Access denied" title', () => {
    renderWithProviders(<AccessDeniedPage />);
    expect(screen.getByText('Access denied')).toBeInTheDocument();
  });

  it('should render default message when no message prop is passed', () => {
    renderWithProviders(<AccessDeniedPage />);
    expect(screen.getByText(/don.t have permission/)).toBeInTheDocument();
  });

  it('should render custom message when message prop is provided', () => {
    renderWithProviders(<AccessDeniedPage message="Insufficient privileges for this data source" />);
    expect(screen.getByText('Insufficient privileges for this data source')).toBeInTheDocument();
  });

  it('should render navigation buttons', () => {
    renderWithProviders(<AccessDeniedPage />);
    expect(screen.getByText('Go to Home')).toBeInTheDocument();
    expect(screen.getByText('Go back')).toBeInTheDocument();
  });
});
