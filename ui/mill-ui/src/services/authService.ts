/**
 * Authentication service — wraps the Mill backend auth REST API.
 *
 * All requests use credentials: 'include' so the session cookie
 * (JSESSIONID) is sent automatically on subsequent calls.
 */

/** Shape of the response returned by POST /auth/public/login and GET /auth/me. */
export interface AuthMeResponse {
  userId: string;
  email: string | null;
  displayName: string | null;
  groups: string[];
  securityEnabled: boolean;
}

/**
 * Authenticates the user with the backend.
 * On success the backend sets a session cookie (JSESSIONID).
 * @throws Error with message 'INVALID_CREDENTIALS' on 401
 */
export async function login(username: string, password: string): Promise<AuthMeResponse> {
  const res = await fetch('/auth/public/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ username, password }),
  });
  if (res.status === 401) throw new Error('INVALID_CREDENTIALS');
  if (!res.ok) throw new Error('LOGIN_FAILED');
  return res.json() as Promise<AuthMeResponse>;
}

/**
 * Ends the current session.
 */
export async function logout(): Promise<void> {
  await fetch('/auth/logout', {
    method: 'POST',
    credentials: 'include',
  });
}

/**
 * Returns the current user from the active session, or null if not authenticated.
 */
export async function getMe(): Promise<AuthMeResponse | null> {
  const res = await fetch('/auth/me', {
    credentials: 'include',
  });
  if (res.status === 401) return null;
  if (!res.ok) return null;
  return res.json() as Promise<AuthMeResponse>;
}
