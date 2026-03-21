import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router';
import { MantineProvider } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { ProfileLayout } from '../ProfileLayout';
import { FeatureFlagProvider } from '../../../features/FeatureFlagContext';

const mockUpdateProfile = vi.fn();

vi.mock('../../../App', () => ({
  useAuth: () => ({
    user: {
      userId: 'u1',
      displayName: 'Alice Smith',
      email: 'alice@example.com',
      groups: [],
      securityEnabled: true,
      profile: {
        userId: 'u1',
        displayName: 'Alice Smith',
        email: 'alice@profile.com',
        locale: 'en',
      },
    },
    loading: false,
    isAuthenticated: true,
    securityEnabled: true,
    login: vi.fn(),
    logout: vi.fn(),
    updateProfile: mockUpdateProfile,
  }),
}));

/**
 * Renders ProfileLayout within the correct router/route setup so that
 * useParams can resolve the `:section?` param.
 */
function renderProfile(path = '/profile') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <MantineProvider>
        <Notifications />
        <FeatureFlagProvider>
          <Routes>
            <Route path="/profile/:section?" element={<ProfileLayout />} />
            <Route path="/profile" element={<ProfileLayout />} />
          </Routes>
        </FeatureFlagProvider>
      </MantineProvider>
    </MemoryRouter>
  );
}

beforeEach(() => {
  mockUpdateProfile.mockReset();
});

describe('ProfileLayout', () => {
  it('renders real display name and email from auth context', () => {
    renderProfile();
    expect(screen.getByText('Alice Smith')).toBeInTheDocument();
    expect(screen.getByText('alice@profile.com')).toBeInTheDocument();
  });

  it('General section — Save calls updateProfile with displayName and email', async () => {
    mockUpdateProfile.mockResolvedValue(undefined);
    renderProfile('/profile/general');

    const saveBtn = await screen.findByTestId('general-save-btn');
    fireEvent.click(saveBtn);

    await waitFor(() => expect(mockUpdateProfile).toHaveBeenCalledOnce());
    expect(mockUpdateProfile).toHaveBeenCalledWith(
      expect.objectContaining({ displayName: 'Alice Smith' })
    );
  });

  it('Settings section — Save calls updateProfile with locale', async () => {
    mockUpdateProfile.mockResolvedValue(undefined);
    renderProfile('/profile/settings');

    const saveBtn = await screen.findByTestId('settings-save-btn');
    fireEvent.click(saveBtn);

    await waitFor(() => expect(mockUpdateProfile).toHaveBeenCalledOnce());
    expect(mockUpdateProfile).toHaveBeenCalledWith(
      expect.objectContaining({ locale: 'en' })
    );
  });

  it('Access section — shows coming-soon placeholder', async () => {
    renderProfile('/profile/access');
    const placeholder = await screen.findByTestId('access-placeholder');
    expect(placeholder).toHaveTextContent('coming soon');
  });
});
