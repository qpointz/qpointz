import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { MantineProvider } from '@mantine/core';
import { RegisterPage } from '../RegisterPage';

function renderRegisterPage(onRegister = vi.fn()) {
  return render(
    <MemoryRouter>
      <MantineProvider>
        <RegisterPage onRegister={onRegister} />
      </MantineProvider>
    </MemoryRouter>,
  );
}

describe('RegisterPage', () => {
  it('renders email, password, and display name inputs', () => {
    renderRegisterPage();
    expect(screen.getByTestId('email-input')).toBeInTheDocument();
    expect(screen.getByTestId('password-input')).toBeInTheDocument();
    expect(screen.getByTestId('displayname-input')).toBeInTheDocument();
  });

  it('renders the create account button', () => {
    renderRegisterPage();
    expect(screen.getByTestId('register-button')).toBeInTheDocument();
  });

  it('renders the sign-in link', () => {
    renderRegisterPage();
    expect(screen.getByTestId('signin-link')).toBeInTheDocument();
  });

  it('calls onRegister with email, password, and displayName on submit', () => {
    const onRegister = vi.fn().mockResolvedValue(undefined);
    renderRegisterPage(onRegister);

    fireEvent.change(screen.getByTestId('email-input'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'secret' } });
    fireEvent.change(screen.getByTestId('displayname-input'), { target: { value: 'Alice' } });
    fireEvent.click(screen.getByTestId('register-button'));

    expect(onRegister).toHaveBeenCalledWith('alice@example.com', 'secret', 'Alice');
  });

  it('shows ALREADY_REGISTERED error message', async () => {
    const onRegister = vi.fn().mockRejectedValue(new Error('ALREADY_REGISTERED'));
    renderRegisterPage(onRegister);

    fireEvent.change(screen.getByTestId('email-input'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByTestId('register-button'));

    await waitFor(() => expect(screen.getByTestId('register-error')).toBeInTheDocument());
    expect(screen.getByTestId('register-error')).toHaveTextContent('An account with this email already exists');
  });

  it('shows generic error message on unexpected failure', async () => {
    const onRegister = vi.fn().mockRejectedValue(new Error('REGISTRATION_FAILED'));
    renderRegisterPage(onRegister);

    fireEvent.change(screen.getByTestId('email-input'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByTestId('register-button'));

    await waitFor(() => expect(screen.getByTestId('register-error')).toBeInTheDocument());
    expect(screen.getByTestId('register-error')).toHaveTextContent('Registration failed');
  });

  it('shows REGISTRATION_DISABLED error message', async () => {
    const onRegister = vi.fn().mockRejectedValue(new Error('REGISTRATION_DISABLED'));
    renderRegisterPage(onRegister);

    fireEvent.change(screen.getByTestId('email-input'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByTestId('password-input'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByTestId('register-button'));

    await waitFor(() => expect(screen.getByTestId('register-error')).toBeInTheDocument());
    expect(screen.getByTestId('register-error')).toHaveTextContent('Registration is currently disabled');
  });
});
