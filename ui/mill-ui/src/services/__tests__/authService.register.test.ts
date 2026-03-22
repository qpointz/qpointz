import { describe, it, expect, vi, beforeEach } from 'vitest';
import { register } from '../authService';

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

describe('authService.register', () => {
  const meResponse = {
    userId: 'u-new',
    email: 'alice@example.com',
    displayName: 'Alice',
    groups: [],
    securityEnabled: true,
  };

  it('success 201 returns AuthMeResponse', async () => {
    mockFetch.mockReturnValue(mockResponse(201, meResponse));
    const result = await register('alice@example.com', 'secret', 'Alice');
    expect(result.userId).toBe('u-new');
    expect(mockFetch).toHaveBeenCalledWith(
      '/auth/public/register',
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('409 throws ALREADY_REGISTERED', async () => {
    mockFetch.mockReturnValue(mockResponse(409, {}));
    await expect(register('alice@example.com', 'secret')).rejects.toThrow('ALREADY_REGISTERED');
  });

  it('403 throws REGISTRATION_DISABLED', async () => {
    mockFetch.mockReturnValue(mockResponse(403, {}));
    await expect(register('alice@example.com', 'secret')).rejects.toThrow('REGISTRATION_DISABLED');
  });

  it('500 throws REGISTRATION_FAILED', async () => {
    mockFetch.mockReturnValue(mockResponse(500, {}));
    await expect(register('alice@example.com', 'secret')).rejects.toThrow('REGISTRATION_FAILED');
  });

  it('sends displayName in request body when provided', async () => {
    mockFetch.mockReturnValue(mockResponse(201, meResponse));
    await register('alice@example.com', 'secret', 'Alice');
    const [, init] = mockFetch.mock.calls[0]!;
    const body = JSON.parse((init as RequestInit).body as string);
    expect(body.displayName).toBe('Alice');
    expect(body.email).toBe('alice@example.com');
    expect(body.password).toBe('secret');
  });

  it('omits displayName from body when not provided', async () => {
    mockFetch.mockReturnValue(mockResponse(201, meResponse));
    await register('alice@example.com', 'secret');
    const [, init] = mockFetch.mock.calls[0]!;
    const body = JSON.parse((init as RequestInit).body as string);
    expect(body.displayName).toBeUndefined();
  });
});
