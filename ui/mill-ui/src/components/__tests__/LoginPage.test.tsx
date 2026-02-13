import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { LoginPage } from '../auth/LoginPage';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MantineProvider>
      <FeatureFlagProvider>
        {ui}
      </FeatureFlagProvider>
    </MantineProvider>,
  );
}

describe('LoginPage', () => {
  it('should render the brand name', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('DataChat')).toBeInTheDocument();
  });

  it('should render "Sign in to your workspace" subtitle', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('Sign in to your workspace')).toBeInTheDocument();
  });

  it('should render social login buttons', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('Continue with GitHub')).toBeInTheDocument();
    expect(screen.getByText('Continue with Google')).toBeInTheDocument();
    expect(screen.getByText('Continue with Microsoft')).toBeInTheDocument();
    expect(screen.getByText('Continue with AWS')).toBeInTheDocument();
    expect(screen.getByText('Continue with Azure AD')).toBeInTheDocument();
  });

  it('should render email and password fields', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Your password')).toBeInTheDocument();
  });

  it('should render Sign in button', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('should call onLogin when a social button is clicked', () => {
    const onLogin = vi.fn();
    renderWithProviders(<LoginPage onLogin={onLogin} />);
    fireEvent.click(screen.getByText('Continue with GitHub'));
    expect(onLogin).toHaveBeenCalledTimes(1);
  });

  it('should call onLogin when form is submitted', () => {
    const onLogin = vi.fn();
    renderWithProviders(<LoginPage onLogin={onLogin} />);
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));
    expect(onLogin).toHaveBeenCalledTimes(1);
  });

  it('should render forgot password link', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('Forgot password?')).toBeInTheDocument();
  });

  it('should render sign up link', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('Sign up')).toBeInTheDocument();
  });
});
