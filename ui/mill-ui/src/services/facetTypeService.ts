import type { FacetTypeManifest } from '../types/facetTypes';

export interface FacetTypeService {
  list(params?: { targetType?: string; enabledOnly?: boolean }, signal?: AbortSignal): Promise<FacetTypeManifest[]>;
  get(typeKey: string): Promise<FacetTypeManifest>;
  create(manifest: FacetTypeManifest): Promise<FacetTypeManifest>;
  update(typeKey: string, manifest: FacetTypeManifest): Promise<FacetTypeManifest>;
  delete(typeKey: string): Promise<void>;
}

function encodeTypeKey(typeKey: string): string {
  const facetTypePrefix = 'urn:mill/metadata/facet-type:';
  const pathKey = typeKey.startsWith(facetTypePrefix)
    ? typeKey.slice(facetTypePrefix.length)
    : typeKey;
  return encodeURIComponent(pathKey);
}

async function parseJson<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.text();
    throw new Error(body || `${res.status} ${res.statusText}`);
  }
  return res.json() as Promise<T>;
}

export const facetTypeService: FacetTypeService = {
  async list(params, signal) {
    const query = new URLSearchParams();
    if (params?.targetType) query.set('targetType', params.targetType);
    if (typeof params?.enabledOnly === 'boolean') query.set('enabledOnly', String(params.enabledOnly));
    const suffix = query.size > 0 ? `?${query.toString()}` : '';
    try {
      const res = await fetch(`/api/v1/metadata/facets${suffix}`, { credentials: 'include', signal });
      return parseJson<FacetTypeManifest[]>(res);
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') return [];
      throw e;
    }
  },

  async get(typeKey) {
    const res = await fetch(`/api/v1/metadata/facets/${encodeTypeKey(typeKey)}`, { credentials: 'include' });
    return parseJson<FacetTypeManifest>(res);
  },

  async create(manifest) {
    const res = await fetch('/api/v1/metadata/facets', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(manifest),
    });
    return parseJson<FacetTypeManifest>(res);
  },

  async update(typeKey, manifest) {
    const res = await fetch(`/api/v1/metadata/facets/${encodeTypeKey(typeKey)}`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(manifest),
    });
    return parseJson<FacetTypeManifest>(res);
  },

  async delete(typeKey) {
    const res = await fetch(`/api/v1/metadata/facets/${encodeTypeKey(typeKey)}`, {
      method: 'DELETE',
      credentials: 'include',
    });
    if (!res.ok) {
      const body = await res.text();
      throw new Error(body || `${res.status} ${res.statusText}`);
    }
  },
};

