import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { MantineProvider } from '@mantine/core';
import { LoginPage } from '../LoginPage';
import { FeatureFlagProvider } from '../../../features/FeatureFlagContext';

function renderLoginPage(onLogin = vi.fn()) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <FeatureFlagProvider>
          <LoginPage onLogin={onLogin} />
        </FeatureFlagProvider>
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

  it('sign up link navigates to /register', () => {
    renderLoginPage();
    const signUp = screen.getByTestId('signup-link');
    expect(signUp).toBeInTheDocument();
  });
});
