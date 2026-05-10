import { describe, it, expect } from 'vitest';
import { getAppBasePath, oauth2AuthorizationHref } from '../appBaseUrl';

describe('getAppBasePath', () => {
  it('should end with a slash', () => {
    expect(getAppBasePath().endsWith('/')).toBe(true);
  });

  it('should default to /app/ when BASE_URL is / (Vitest)', () => {
    expect(getAppBasePath()).toBe('/app/');
  });
});

describe('oauth2AuthorizationHref', () => {
  it('should use servlet-root Spring Security path, not SPA base', () => {
    expect(oauth2AuthorizationHref('authentik')).toBe('/oauth2/authorization/authentik');
  });
});
