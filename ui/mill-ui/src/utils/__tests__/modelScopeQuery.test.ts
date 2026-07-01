import { describe, expect, it } from 'vitest';
import {
  EMPTY_READ_SCOPE_MARKER,
  chatScopeSlug,
  defaultScopeSlugs,
  formatScopeSearchParam,
  modelViewSearchFromParams,
  modelEntityLoadKey,
  modelScopeReadUrlToken,
  parseScopeSearchParam,
  READ_SCOPE_PARAM,
  nextReadScopesAfterPickerToggle,
  resolveModelScopeFromSearchParams,
  scopeDisplayLabel,
  scopeSearchParamsAfterReadScopeChange,
  scopeSlugFromUrn,
  scopesInPoolOrder,
  shouldShowScopePicker,
  writableScopeForNewFacet,
} from '../modelScopeQuery';

describe('modelScopeQuery', () => {
  it('should parse comma-separated scope param preserving order', () => {
    expect(parseScopeSearchParam('global,chat-abc')).toEqual(['global', 'chat-abc']);
    expect(parseScopeSearchParam(' chat-abc , global ')).toEqual(['chat-abc', 'global']);
  });

  it('should dedupe scope param segments', () => {
    expect(parseScopeSearchParam('global,global,chat-x')).toEqual(['global', 'chat-x']);
  });

  it('should return empty array for blank scope param', () => {
    expect(parseScopeSearchParam(null)).toEqual([]);
    expect(parseScopeSearchParam('')).toEqual([]);
    expect(parseScopeSearchParam('  ,  ')).toEqual([]);
  });

  it('should format scope slugs as comma-separated string', () => {
    expect(formatScopeSearchParam(['global', 'chat-abc'])).toBe('global,chat-abc');
  });

  it('should build chat scope slug from conversation id', () => {
    expect(chatScopeSlug('conv-1')).toBe('chat-conv-1');
  });

  it('should default to global when scope param absent', () => {
    expect(resolveModelScopeFromSearchParams(new URLSearchParams()).declaredScopes).toEqual(
      defaultScopeSlugs(),
    );
    expect(resolveModelScopeFromSearchParams(new URLSearchParams()).readScopes).toEqual(['global']);
  });

  it('should treat all declared scopes as active when readScope is absent', () => {
    const state = resolveModelScopeFromSearchParams(new URLSearchParams('scope=global,chat-x'));
    expect(state.declaredScopes).toEqual(['global', 'chat-x']);
    expect(state.readScopes).toEqual(['global', 'chat-x']);
    expect(state.scopeQuery).toBe('global,chat-x');
  });

  it('should resolve readScope subset in pool order', () => {
    const state = resolveModelScopeFromSearchParams(
      new URLSearchParams('scope=global,chat-x&readScope=global'),
    );
    expect(state.declaredScopes).toEqual(['global', 'chat-x']);
    expect(state.readScopes).toEqual(['global']);
    expect(state.scopeQuery).toBe('global');
  });

  it('should treat explicit empty readScope as no active scopes', () => {
    const emptyParam = resolveModelScopeFromSearchParams(
      new URLSearchParams(`scope=global,chat-x&readScope=${EMPTY_READ_SCOPE_MARKER}`),
    );
    expect(emptyParam.readScopes).toEqual([]);
    expect(emptyParam.scopeQuery).toBe('');

    const legacyEmpty = resolveModelScopeFromSearchParams(
      new URLSearchParams('scope=global,chat-x&readScope='),
    );
    expect(legacyEmpty.readScopes).toEqual([]);
  });

  it('should label known scope slugs', () => {
    expect(scopeDisplayLabel('global')).toBe('Global');
    expect(scopeDisplayLabel('chat-abc')).toBe('Chat');
    expect(scopeDisplayLabel('team-eng')).toBe('team-eng');
  });

  it('should extract slug from scope urn', () => {
    expect(scopeSlugFromUrn('urn:mill/metadata/scope:chat-abc')).toBe('chat-abc');
    expect(scopeSlugFromUrn('urn:mill:scope:global')).toBe('global');
  });

  it('should pick writable scope as last non-global active scope', () => {
    expect(writableScopeForNewFacet(['global'])).toBe('global');
    expect(writableScopeForNewFacet(['global', 'chat-abc'])).toBe('chat-abc');
    expect(writableScopeForNewFacet(['chat-a', 'chat-b'])).toBe('chat-b');
  });

  it('should report multi-option picker when declared pool has multiple scopes', () => {
    expect(shouldShowScopePicker(['global'])).toBe(false);
    expect(shouldShowScopePicker(['global', 'chat-x'])).toBe(true);
  });

  it('should preserve scope pool when narrowing readScope', () => {
    const next = scopeSearchParamsAfterReadScopeChange(
      new URLSearchParams('scope=global,chat-x'),
      ['global', 'chat-x'],
      ['global'],
    );
    expect(next.get('scope')).toBe('global,chat-x');
    expect(next.get(READ_SCOPE_PARAM)).toBe('global');
  });

  it('should omit readScope when all declared scopes are active', () => {
    const next = scopeSearchParamsAfterReadScopeChange(
      new URLSearchParams(`scope=global,chat-x&${READ_SCOPE_PARAM}=global`),
      ['global', 'chat-x'],
      ['global', 'chat-x'],
    );
    expect(next.get('scope')).toBe('global,chat-x');
    expect(next.get(READ_SCOPE_PARAM)).toBeNull();
  });

  it('should distinguish implicit all-scopes URL from explicit readScope subset in load key', () => {
    const allDeclared = new URLSearchParams('scope=global,chat-x');
    const globalOnly = new URLSearchParams('scope=global,chat-x&readScope=global');
    expect(modelScopeReadUrlToken(allDeclared)).toBe('__all_declared__');
    expect(modelScopeReadUrlToken(globalOnly)).toBe('global');
    expect(modelEntityLoadKey('model-entity', 'global,chat-x', allDeclared)).not.toBe(
      modelEntityLoadKey('model-entity', 'global', globalOnly),
    );
    expect(modelEntityLoadKey('model-entity', 'global,chat-x', allDeclared)).not.toBe(
      modelEntityLoadKey('model-entity', 'global,chat-x', globalOnly),
    );
  });

  it('should filter read scopes to declared pool order', () => {
    expect(scopesInPoolOrder(['global', 'chat-a', 'chat-b'], ['chat-b', 'global'])).toEqual([
      'global',
      'chat-b',
    ]);
  });

  it('should serialize model view search string', () => {
    const params = new URLSearchParams();
    params.set('scope', 'global,chat-x');
    expect(modelViewSearchFromParams(params)).toBe('?scope=global%2Cchat-x');
    expect(modelViewSearchFromParams(new URLSearchParams())).toBe('');
  });

  it('should persist empty readScope in URL', () => {
    const next = scopeSearchParamsAfterReadScopeChange(
      new URLSearchParams('scope=global,chat-x'),
      ['global', 'chat-x'],
      [],
    );
    expect(next.get('scope')).toBe('global,chat-x');
    expect(next.get(READ_SCOPE_PARAM)).toBe(EMPTY_READ_SCOPE_MARKER);
    expect(resolveModelScopeFromSearchParams(next).readScopes).toEqual([]);
  });

  it('should round-trip uncheck global then re-check global', () => {
    const pool = ['global', 'chat-x'];
    let params = new URLSearchParams('scope=global,chat-x');
    params = scopeSearchParamsAfterReadScopeChange(
      params,
      pool,
      nextReadScopesAfterPickerToggle(pool, pool, 'global', false),
    );
    expect(resolveModelScopeFromSearchParams(params).readScopes).toEqual(['chat-x']);
    params = scopeSearchParamsAfterReadScopeChange(
      params,
      pool,
      nextReadScopesAfterPickerToggle(
        pool,
        resolveModelScopeFromSearchParams(params).readScopes,
        'global',
        true,
      ),
    );
    expect(resolveModelScopeFromSearchParams(params).readScopes).toEqual(['global', 'chat-x']);
  });

  it('should round-trip uncheck chat then re-check chat', () => {
    const pool = ['global', 'chat-x'];
    let params = new URLSearchParams('scope=global,chat-x');
    params = scopeSearchParamsAfterReadScopeChange(
      params,
      pool,
      nextReadScopesAfterPickerToggle(pool, pool, 'chat-x', false),
    );
    expect(resolveModelScopeFromSearchParams(params).readScopes).toEqual(['global']);
    params = scopeSearchParamsAfterReadScopeChange(
      params,
      pool,
      nextReadScopesAfterPickerToggle(
        pool,
        resolveModelScopeFromSearchParams(params).readScopes,
        'chat-x',
        true,
      ),
    );
    expect(resolveModelScopeFromSearchParams(params).readScopes).toEqual(['global', 'chat-x']);
  });

  it('should allow unchecking every scope', () => {
    expect(nextReadScopesAfterPickerToggle(['global', 'chat-x'], ['global'], 'global', false)).toEqual([]);
  });
});
