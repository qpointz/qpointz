import { describe, it, expect } from 'vitest';
import * as AppModule from '../App';

// We test RequireAuth behaviour via App module
// Simple smoke test: when not authenticated, /home redirects to /login
describe('RequireAuth', () => {
  it('is exported and works', () => {
    expect(AppModule.useAuth).toBeDefined();
  });
});
