import type { FacetTypeManifest } from '../types/facetTypes';
import { facetTypePathSegment } from '../utils/urnSlug';
import { facetTypeManifestFromWire, facetTypeManifestToWire } from './facetTypeWire';

function unwrapFacetTypeListPayload(raw: unknown): unknown[] {
  if (Array.isArray(raw)) {
    return raw;
  }
  if (raw != null && typeof raw === 'object' && !Array.isArray(raw)) {
    const o = raw as Record<string, unknown>;
    const content = o.content;
    if (Array.isArray(content)) {
      return content;
    }
    const data = o.data;
    if (Array.isArray(data)) {
      return data;
    }
  }
  return [];
}

export interface FacetTypeService {
  list(params?: { targetType?: string; enabledOnly?: boolean }, signal?: AbortSignal): Promise<FacetTypeManifest[]>;
  get(typeKey: string): Promise<FacetTypeManifest>;
  create(manifest: FacetTypeManifest): Promise<FacetTypeManifest>;
  update(typeKey: string, manifest: FacetTypeManifest): Promise<FacetTypeManifest>;
  delete(typeKey: string): Promise<void>;
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
      const raw = await parseJson<unknown>(res);
      const rows = unwrapFacetTypeListPayload(raw);
      const out: FacetTypeManifest[] = [];
      for (const r of rows) {
        try {
          out.push(facetTypeManifestFromWire(r));
        } catch (e) {
          console.warn('[facetTypeService.list] skip invalid facet type row', e, r);
        }
      }
      return out;
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') return [];
      throw e;
    }
  },

  async get(typeKey) {
    const res = await fetch(`/api/v1/metadata/facets/${facetTypePathSegment(typeKey)}`, { credentials: 'include' });
    const raw = await parseJson<unknown>(res);
    return facetTypeManifestFromWire(raw);
  },

  async create(manifest) {
    const res = await fetch('/api/v1/metadata/facets', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(facetTypeManifestToWire(manifest)),
    });
    const raw = await parseJson<unknown>(res);
    return facetTypeManifestFromWire(raw);
  },

  async update(typeKey, manifest) {
    const res = await fetch(`/api/v1/metadata/facets/${facetTypePathSegment(typeKey)}`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(facetTypeManifestToWire(manifest)),
    });
    const raw = await parseJson<unknown>(res);
    return facetTypeManifestFromWire(raw);
  },

  async delete(typeKey) {
    const res = await fetch(`/api/v1/metadata/facets/${facetTypePathSegment(typeKey)}`, {
      method: 'DELETE',
      credentials: 'include',
    });
    if (!res.ok) {
      const body = await res.text();
      throw new Error(body || `${res.status} ${res.statusText}`);
    }
  },
};

