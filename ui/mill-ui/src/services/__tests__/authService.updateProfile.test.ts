import { describe, it, expect, vi, beforeEach } from 'vitest';
import { updateProfile } from '../authService';
import type { UserProfilePatch, UserProfileResponse } from '../authService';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function mockResponse(status: number, body: unknown) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  });
}

beforeEach(() => mockFetch.mockReset());

describe('authService.updateProfile', () => {
  const profileResponse: UserProfileResponse = {
    userId: 'u1',
    displayName: 'Alice',
    email: 'alice@example.com',
    locale: 'en',
  };

  it('sends PATCH to /auth/profile with correct body and returns UserProfileResponse', async () => {
    mockFetch.mockReturnValue(mockResponse(200, profileResponse));

    const patch: UserProfilePatch = { displayName: 'Alice', email: 'alice@example.com', locale: 'en' };
    const result = await updateProfile(patch);

    expect(mockFetch).toHaveBeenCalledWith(
      '/auth/profile',
      expect.objectContaining({
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(patch),
      })
    );
    expect(result.userId).toBe('u1');
    expect(result.displayName).toBe('Alice');
    expect(result.locale).toBe('en');
  });

  it('throws UNAUTHENTICATED on 401', async () => {
    mockFetch.mockReturnValue(mockResponse(401, {}));
    await expect(updateProfile({ displayName: 'X' })).rejects.toThrow('UNAUTHENTICATED');
  });

  it('throws UPDATE_PROFILE_FAILED on non-401 error status', async () => {
    mockFetch.mockReturnValue(mockResponse(500, {}));
    await expect(updateProfile({ locale: 'fr' })).rejects.toThrow('UPDATE_PROFILE_FAILED');
  });
});
