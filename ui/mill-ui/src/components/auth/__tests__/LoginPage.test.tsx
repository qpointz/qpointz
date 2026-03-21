import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { MantineProvider } from '@mantine/core';
import { LoginPage } from '../LoginPage';
import { FeatureFlagContext } from '../../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../../features/defaults';

function renderLoginPage(onLogin = vi.fn(), loginRegistration = false) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagContext.Provider value={{ ...defaultFeatureFlags, loginRegistration }}>
          <LoginPage onLogin={onLogin} />
        </FeatureFlagContext.Provider>
      </MantineProvider>
    </MemoryRouter>
  );
}

describe('LoginPage', () => {
  it('shows error alert on INVALID_CREDENTIALS', async () => {
    const onLogin = vi.fn().mockRejectedValue(new Error('INVALID_CREDENTIALS'));
    renderLoginPage(onLogin);
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'a@b.com' } });
    fireEvent.change(screen.getByPlaceholderText('Your password'), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByTestId('signin-button'));
    await waitFor(() => expect(screen.getByTestId('login-error')).toBeInTheDocument());
    expect(screen.getByTestId('login-error')).toHaveTextContent('Invalid email or password');
  });

  it('sign up link hidden by default (loginRegistration=false)', () => {
    renderLoginPage(vi.fn(), false);
    expect(screen.queryByTestId('signup-link')).not.toBeInTheDocument();
  });

  it('sign up link visible when loginRegistration=true', () => {
    renderLoginPage(vi.fn(), true);
    expect(screen.getByTestId('signup-link')).toBeInTheDocument();
  });
});
