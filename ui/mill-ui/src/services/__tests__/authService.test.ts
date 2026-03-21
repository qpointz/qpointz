import { describe, it, expect, vi, beforeEach } from 'vitest';
import { login, logout, getMe } from '../authService';

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

describe('authService', () => {
  const meResponse = {
    userId: 'u1',
    email: 'a@b.com',
    displayName: 'Alice',
    groups: ['testers'],
    securityEnabled: true,
  };

  it('login - success returns AuthMeResponse', async () => {
    mockFetch.mockReturnValue(mockResponse(200, meResponse));
    const result = await login('a@b.com', 'secret');
    expect(result.userId).toBe('u1');
    expect(mockFetch).toHaveBeenCalledWith('/auth/public/login', expect.objectContaining({ method: 'POST' }));
  });

  it('login - 401 throws INVALID_CREDENTIALS', async () => {
    mockFetch.mockReturnValue(mockResponse(401, {}));
    await expect(login('a@b.com', 'wrong')).rejects.toThrow('INVALID_CREDENTIALS');
  });

  it('logout - calls POST /auth/logout', async () => {
    mockFetch.mockReturnValue(mockResponse(200, {}));
    await logout();
    expect(mockFetch).toHaveBeenCalledWith('/auth/logout', expect.objectContaining({ method: 'POST' }));
  });

  it('getMe - success returns AuthMeResponse', async () => {
    mockFetch.mockReturnValue(mockResponse(200, meResponse));
    const result = await getMe();
    expect(result?.userId).toBe('u1');
  });

  it('getMe - 401 returns null', async () => {
    mockFetch.mockReturnValue(mockResponse(401, {}));
    const result = await getMe();
    expect(result).toBeNull();
  });
});
