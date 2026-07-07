import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchExportFormats, resetExportFormatsCache } from '../exportService';

const sampleFormats = [{ id: 'csv', fileExtension: 'csv' }];

describe('fetchExportFormats', () => {
  afterEach(() => {
    resetExportFormatsCache();
    vi.unstubAllGlobals();
  });

  it('shouldDedupeConcurrentRequests', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      json: async () => sampleFormats,
    }));
    vi.stubGlobal('fetch', fetchMock);

    const [first, second] = await Promise.all([fetchExportFormats(), fetchExportFormats()]);

    expect(first).toEqual(sampleFormats);
    expect(second).toEqual(sampleFormats);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('shouldReturnCachedFormatsWithoutSecondNetworkCall', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      json: async () => sampleFormats,
    }));
    vi.stubGlobal('fetch', fetchMock);

    await fetchExportFormats();
    const cached = await fetchExportFormats();

    expect(cached).toEqual(sampleFormats);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});
