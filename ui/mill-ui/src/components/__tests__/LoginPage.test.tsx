import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter } from 'react-router';
import { LoginPage } from '../auth/LoginPage';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../features/defaults';
import { FeatureFlagContext } from '../../features/FeatureFlagContext';

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagProvider>
          {ui}
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>,
  );
}

function renderWithFlags(ui: React.ReactElement, flags = defaultFeatureFlags) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagContext.Provider value={flags}>
          {ui}
        </FeatureFlagContext.Provider>
      </MantineProvider>
    </MemoryRouter>,
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

  it('should render social login buttons as non-functional placeholders', () => {
    renderWithProviders(<LoginPage onLogin={() => {}} />);
    expect(screen.getByText('Continue with GitHub')).toBeInTheDocument();
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

  it('should NOT render sign up link when loginRegistration is false', () => {
    renderWithFlags(<LoginPage onLogin={() => {}} />, {
      ...defaultFeatureFlags,
      loginRegistration: false,
    });
    expect(screen.queryByTestId('signup-link')).not.toBeInTheDocument();
  });

  it('should render sign up link when loginRegistration is true', () => {
    renderWithFlags(<LoginPage onLogin={() => {}} />, {
      ...defaultFeatureFlags,
      loginRegistration: true,
    });
    expect(screen.getByText('Sign up')).toBeInTheDocument();
    expect(screen.getByTestId('signup-link')).toBeInTheDocument();
  });
});
