/**
 * Authentication service — wraps the Mill backend auth REST API.
 *
 * All requests use credentials: 'include' so the session cookie
 * (JSESSIONID) is sent automatically on subsequent calls.
 */

/**
 * User-editable profile attributes returned by GET /auth/me and PATCH /auth/profile.
 */
export interface UserProfileResponse {
  /** Canonical users.id — read-only, never sent to the server in a patch. */
  userId: string;
  /** Optional human-readable display name chosen by the user. */
  displayName?: string;
  /** Optional email address stored in the profile. */
  email?: string;
  /** Optional locale string (e.g. "en", "fr"). */
  locale?: string;
}

/**
 * Partial-update body for PATCH /auth/profile.
 * Only non-null/undefined fields are applied; omitted fields are left unchanged.
 */
export interface UserProfilePatch {
  /** New display name, or omit to leave unchanged. */
  displayName?: string;
  /** New email address, or omit to leave unchanged. */
  email?: string;
  /** New locale string, or omit to leave unchanged. */
  locale?: string;
}

/** Shape of the response returned by POST /auth/public/login and GET /auth/me. */
export interface AuthMeResponse {
  userId: string;
  email: string | null;
  displayName: string | null;
  groups: string[];
  securityEnabled: boolean;
  /** Optional user-editable profile; null for anonymous or security-off responses. */
  profile?: UserProfileResponse | null;
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

/**
 * Registers a new local user account and opens a session.
 *
 * On `201`: returns the newly created user's [AuthMeResponse].
 * On `409`: throws `Error('ALREADY_REGISTERED')` — email is already in use.
 * On `403`: throws `Error('REGISTRATION_DISABLED')` — feature is disabled server-side.
 *
 * @param email - The email address to register; used as the local-auth subject.
 * @param password - The plaintext password to store (hashed on the server).
 * @param displayName - Optional human-readable name; omit to let the server default to email.
 * @returns The [AuthMeResponse] for the newly registered user.
 * @throws Error with message 'ALREADY_REGISTERED' on 409
 * @throws Error with message 'REGISTRATION_DISABLED' on 403
 * @throws Error with message 'REGISTRATION_FAILED' on any other failure
 */
export async function register(
  email: string,
  password: string,
  displayName?: string,
): Promise<AuthMeResponse> {
  const res = await fetch('/auth/public/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ email, password, displayName }),
  });
  if (res.status === 409) throw new Error('ALREADY_REGISTERED');
  if (res.status === 403) throw new Error('REGISTRATION_DISABLED');
  if (!res.ok) throw new Error('REGISTRATION_FAILED');
  return res.json() as Promise<AuthMeResponse>;
}

/**
 * Applies a partial update to the authenticated user's profile.
 * Only non-null/undefined fields in patch are persisted; other fields are left unchanged.
 * @param patch - Fields to update; omit a field to leave it unchanged on the server.
 * @returns The updated profile as returned by the server.
 * @throws Error with message 'UNAUTHENTICATED' on 401
 */
export async function updateProfile(patch: UserProfilePatch): Promise<UserProfileResponse> {
  const res = await fetch('/auth/profile', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(patch),
  });
  if (res.status === 401) throw new Error('UNAUTHENTICATED');
  if (!res.ok) throw new Error('UPDATE_PROFILE_FAILED');
  return res.json() as Promise<UserProfileResponse>;
}
