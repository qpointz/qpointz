/**
 * Returns the URL prefix for the deployed SPA (always ends with {@code /}).
 * Uses {@link import.meta.env.BASE_URL} from Vite; falls back to {@code /app/} when tests
 * resolve {@code BASE_URL} as {@code /} so asset and router paths match {@code mill.ui.app-base-path}.
 */
export function getAppBasePath(): string {
  const raw = import.meta.env.BASE_URL;
  if (raw && raw !== '/') {
    return raw.endsWith('/') ? raw : `${raw}/`;
  }
  return '/app/';
}

/**
 * Browser URL to start Spring Security {@code oauth2Login} for a registration.
 *
 * <p>Mill serves the SPA under {@code mill.ui.app-base-path} (e.g. {@code /app/}), but OAuth2
 * authorization requests use Spring Security defaults at the <strong>servlet context root</strong>
 * ({@code /oauth2/authorization/{registrationId}}), not under the SPA prefix. Using the SPA base
 * here would hit {@code /app/oauth2/...}, which is not the registered endpoint and can be blocked
 * by {@code /app/**} security rules (redirect back to {@code /app/login}).
 *
 * @param registrationId Spring client registration id (e.g. {@code authentik})
 * @return path suitable for an {@code <a href>} (leading slash, no host)
 */
export function oauth2AuthorizationHref(registrationId: string): string {
  return `/oauth2/authorization/${registrationId}`;
}
