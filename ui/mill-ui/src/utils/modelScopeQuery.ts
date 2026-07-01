/** Default metadata scope slug when `?scope=` is absent. */
export const GLOBAL_SCOPE_SLUG = 'global';

/** Query param for the active metadata read subset (subset of {@link SCOPE_PARAM}). */
export const READ_SCOPE_PARAM = 'readScope';

/** Query param listing all scopes in play for this model-view session. */
export const SCOPE_PARAM = 'scope';

/**
 * URL marker for explicitly zero active read scopes (`readScope=none`).
 * Avoids ambiguous empty params that some routers normalize away.
 */
export const EMPTY_READ_SCOPE_MARKER = 'none';

const METADATA_SCOPE_PREFIX = 'urn:mill/metadata/scope:';

/**
 * Slug for chat-scoped metadata (`urn:mill/metadata/scope:chat-<chatId>`).
 *
 * @param chatId conversation id
 */
export function chatScopeSlug(chatId: string): string {
  const id = chatId.trim();
  if (!id) {
    throw new Error('chatId must not be blank');
  }
  return `chat-${id}`;
}

/**
 * Active scopes when the URL omits `?scope=`.
 */
export function defaultScopeSlugs(): string[] {
  return [GLOBAL_SCOPE_SLUG];
}

/**
 * Parses a comma-separated scope query value into ordered, deduplicated slugs.
 *
 * @param param raw scope search param or null
 */
export function parseScopeSearchParam(param: string | null | undefined): string[] {
  if (!param?.trim()) {
    return [];
  }
  const seen = new Set<string>();
  const ordered: string[] = [];
  for (const raw of param.split(',')) {
    const slug = raw.trim();
    if (!slug || seen.has(slug)) {
      continue;
    }
    seen.add(slug);
    ordered.push(slug);
  }
  return ordered;
}

/**
 * Formats scope slugs for a comma-separated query parameter.
 *
 * @param slugs ordered scope slugs
 */
export function formatScopeSearchParam(slugs: string[]): string {
  return slugs.filter((s) => s.trim().length > 0).join(',');
}

/**
 * Keeps {@link subset} slugs that appear in {@link pool}, in pool order.
 *
 * @param pool declared scope order
 * @param subset candidate active slugs
 */
export function scopesInPoolOrder(pool: string[], subset: string[]): string[] {
  const allowed = new Set(subset);
  return pool.filter((slug) => allowed.has(slug));
}

/** Declared scopes plus the active read subset derived from model-view URL params. */
export interface ModelScopeUrlState {
  /** Full scope pool from `scope` (defaults to global). */
  declaredScopes: string[];
  /** Active scopes used for metadata reads (subset of {@link declaredScopes}). */
  readScopes: string[];
  /** Comma-separated {@link readScopes} for schema/metadata API calls. */
  scopeQuery: string;
}

/**
 * Resolves declared and active scopes from model-view search params.
 *
 * `scope` lists every scope in the session; optional `readScope` lists the checked
 * subset. When `readScope` is absent, all declared scopes are active. When present
 * (including empty), only the listed scopes are active.
 *
 * @param params current URL search params
 */
export function resolveModelScopeFromSearchParams(params: URLSearchParams): ModelScopeUrlState {
  const declaredParsed = parseScopeSearchParam(params.get(SCOPE_PARAM));
  const declaredScopes = declaredParsed.length > 0 ? declaredParsed : defaultScopeSlugs();

  const readScopeExplicit = params.has(READ_SCOPE_PARAM);
  const readScopeRaw = params.get(READ_SCOPE_PARAM);
  const readParsed =
    readScopeRaw === EMPTY_READ_SCOPE_MARKER || readScopeRaw === ''
      ? []
      : parseScopeSearchParam(readScopeRaw);
  const readFromParam = scopesInPoolOrder(declaredScopes, readParsed);
  const readScopes = readScopeExplicit ? readFromParam : declaredScopes;

  return {
    declaredScopes,
    readScopes,
    scopeQuery: formatScopeSearchParam(readScopes),
  };
}

/**
 * @deprecated use {@link resolveModelScopeFromSearchParams}
 */
export function activeScopesFromUrlParam(param: string | null | undefined): string[] {
  const parsed = parseScopeSearchParam(param);
  return parsed.length > 0 ? parsed : defaultScopeSlugs();
}

/**
 * Local display label for a scope slug (no server lookup).
 *
 * @param slug scope slug from URL
 */
export function scopeDisplayLabel(slug: string): string {
  if (slug === GLOBAL_SCOPE_SLUG) {
    return 'Global';
  }
  if (slug.startsWith('chat-')) {
    return 'Chat';
  }
  return slug;
}

/**
 * Extracts the scope slug from a metadata scope URN.
 *
 * @param scopeUrn full or canonical scope URN
 */
export function scopeSlugFromUrn(scopeUrn: string): string {
  const t = scopeUrn.trim();
  if (t.startsWith(METADATA_SCOPE_PREFIX)) {
    return t.slice(METADATA_SCOPE_PREFIX.length);
  }
  const legacyPrefix = 'urn:mill:scope:';
  if (t.startsWith(legacyPrefix)) {
    return t.slice(legacyPrefix.length);
  }
  return t;
}

/**
 * Scope slug used for new facet writes: last non-global active scope, else global.
 *
 * @param activeScopes ordered active read scope slugs
 */
export function writableScopeForNewFacet(activeScopes: string[]): string {
  if (activeScopes.length === 0) {
    return GLOBAL_SCOPE_SLUG;
  }
  const nonGlobal = activeScopes.filter((s) => s !== GLOBAL_SCOPE_SLUG);
  return nonGlobal.length > 0 ? nonGlobal[nonGlobal.length - 1]! : GLOBAL_SCOPE_SLUG;
}

/**
 * Whether the scope picker should show multiple options (declared pool has 2+ scopes).
 * The picker control itself is always visible in the model explorer.
 *
 * @param declaredScopes full scope pool from `scope`
 */
export function shouldShowScopePicker(declaredScopes: string[]): boolean {
  return declaredScopes.length >= 2;
}

/**
 * Next active read scopes after a picker checkbox toggle.
 *
 * @param declaredScopes full scope pool from URL
 * @param activeScopes currently active scopes
 * @param slug scope slug toggled
 * @param checked whether the checkbox was checked
 * @returns next active scopes (may be empty)
 */
export function nextReadScopesAfterPickerToggle(
  declaredScopes: string[],
  activeScopes: string[],
  slug: string,
  checked: boolean,
): string[] {
  if (checked) {
    const nextSet = new Set([...activeScopes, slug]);
    return declaredScopes.filter((s) => nextSet.has(s));
  }
  return activeScopes.filter((s) => s !== slug);
}

/**
 * Updates `readScope` while preserving the declared `scope` pool.
 *
 * @param current current search params
 * @param declaredScopes scopes listed in `scope`
 * @param nextReadSlugs next checked scopes (subset of pool; may be empty)
 */
export function scopeSearchParamsAfterReadScopeChange(
  current: URLSearchParams,
  declaredScopes: string[],
  nextReadSlugs: string[],
): URLSearchParams {
  const next = new URLSearchParams(current);
  const pool =
    declaredScopes.length > 0 ? declaredScopes : defaultScopeSlugs();
  const readScopes = scopesInPoolOrder(pool, nextReadSlugs);

  if (pool.length === 1 && pool[0] === GLOBAL_SCOPE_SLUG) {
    next.delete(SCOPE_PARAM);
  } else {
    next.set(SCOPE_PARAM, formatScopeSearchParam(pool));
  }

  const allActive =
    readScopes.length === pool.length && pool.every((slug, index) => readScopes[index] === slug);
  if (allActive) {
    next.delete(READ_SCOPE_PARAM);
  } else if (readScopes.length === 0) {
    next.set(READ_SCOPE_PARAM, EMPTY_READ_SCOPE_MARKER);
  } else {
    next.set(READ_SCOPE_PARAM, formatScopeSearchParam(readScopes));
  }
  return next;
}

/**
 * URL token for how {@link READ_SCOPE_PARAM} is encoded.
 * When the param is omitted, all declared scopes are active — distinct from an explicit subset
 * even when {@link resolveModelScopeFromSearchParams} yields the same {@link scopeQuery}.
 *
 * @param params current model-view search params
 */
export function modelScopeReadUrlToken(params: URLSearchParams): string {
  if (!params.has(READ_SCOPE_PARAM)) {
    return '__all_declared__';
  }
  return params.get(READ_SCOPE_PARAM) ?? '';
}

/**
 * Cache-busting key for entity + facet loads in the model explorer.
 * Includes {@link scopeQuery} and the raw {@link READ_SCOPE_PARAM} presence/value so returning
 * to an all-scopes URL (readScope omitted) still refetches after a narrower read pass.
 *
 * @param entityId schema explorer entity id
 * @param scopeQuery comma-joined active read scopes
 * @param params current model-view search params
 */
export function modelEntityLoadKey(
  entityId: string,
  scopeQuery: string,
  params: URLSearchParams,
): string {
  return `${entityId}\0${scopeQuery}\0${modelScopeReadUrlToken(params)}`;
}

/**
 * Serializes search params for in-app model routes (`?a=1` or empty).
 *
 * @param params search params to preserve on navigation
 */
export function modelViewSearchFromParams(params: URLSearchParams): string {
  const serialized = params.toString();
  return serialized ? `?${serialized}` : '';
}

/**
 * @deprecated use {@link scopeSearchParamsAfterReadScopeChange}
 */
export function scopeSearchParamsAfterChange(
  current: URLSearchParams,
  nextSlugs: string[],
): URLSearchParams {
  const declared = parseScopeSearchParam(current.get(SCOPE_PARAM));
  const pool = declared.length > 0 ? declared : defaultScopeSlugs();
  return scopeSearchParamsAfterReadScopeChange(current, pool, nextSlugs);
}
