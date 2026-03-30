import { describe, it, expect } from 'vitest';
import {
  appendEmptyMultipleItem,
  isRelationsEnvelope,
  multipleFacetItemValues,
  multipleInstanceCaption,
  replaceMultipleItemAt,
} from '../facetPayloadUtils';

describe('multipleFacetItemValues', () => {
  it('returns empty array for null and undefined', () => {
    expect(multipleFacetItemValues(null)).toEqual([]);
    expect(multipleFacetItemValues(undefined)).toEqual([]);
  });

  it('returns array payloads as-is', () => {
    expect(multipleFacetItemValues([])).toEqual([]);
    expect(multipleFacetItemValues([{ a: 1 }, { b: 2 }])).toEqual([{ a: 1 }, { b: 2 }]);
  });

  it('uses relations array for legacy envelope including empty', () => {
    expect(multipleFacetItemValues({ relations: [] })).toEqual([]);
    expect(multipleFacetItemValues({ relations: [{ id: 'r1' }] })).toEqual([{ id: 'r1' }]);
  });

  it('treats plain empty object as one instance', () => {
    expect(multipleFacetItemValues({})).toEqual([{}]);
  });

  it('wraps non-envelope single object as one instance', () => {
    expect(multipleFacetItemValues({ foo: 1 })).toEqual([{ foo: 1 }]);
  });

  it('unwraps legacy { value: [...] } from REST coercion for JSON array bodies', () => {
    expect(multipleFacetItemValues({ value: [{ links: [{ href: 'https://a' }] }] })).toEqual([
      { links: [{ href: 'https://a' }] },
    ]);
  });
});

describe('multipleInstanceCaption', () => {
  it('uses trimmed name when present', () => {
    expect(multipleInstanceCaption({ name: 'Alpha' }, 0, 2)).toBe('Alpha');
    expect(multipleInstanceCaption({ name: '  Beta  ' }, 1, 3)).toBe('Beta');
  });

  it('falls back to entry index when name missing or blank', () => {
    expect(multipleInstanceCaption({}, 0, 2)).toBe('Entry 1 of 2');
    expect(multipleInstanceCaption({ name: '' }, 2, 5)).toBe('Entry 3 of 5');
    expect(multipleInstanceCaption(null, 0, 1)).toBe('');
    expect(multipleInstanceCaption({}, 0, 1)).toBe('');
  });
});

describe('isRelationsEnvelope', () => {
  it('narrows when relations is an array', () => {
    const raw = { relations: [{ x: 1 }], meta: 'm' };
    expect(isRelationsEnvelope(raw)).toBe(true);
    if (isRelationsEnvelope(raw)) {
      expect(raw.relations).toHaveLength(1);
    }
  });

  it('rejects non-objects and missing relations array', () => {
    expect(isRelationsEnvelope(null)).toBe(false);
    expect(isRelationsEnvelope([])).toBe(false);
    expect(isRelationsEnvelope({})).toBe(false);
    expect(isRelationsEnvelope({ relations: null })).toBe(false);
  });
});

describe('replaceMultipleItemAt', () => {
  it('replaces item in array payload', () => {
    expect(replaceMultipleItemAt([1, 2, 3], 1, 99)).toEqual([1, 99, 3]);
  });

  it('preserves relations envelope shape', () => {
    const raw = { relations: [{ a: 1 }, { a: 2 }], extra: true };
    expect(replaceMultipleItemAt(raw, 0, { a: 9 })).toEqual({
      relations: [{ a: 9 }, { a: 2 }],
      extra: true,
    });
  });

  it('returns raw when index out of range', () => {
    const raw = [1];
    expect(replaceMultipleItemAt(raw, -1, 0)).toBe(raw);
    expect(replaceMultipleItemAt(raw, 2, 0)).toBe(raw);
    expect(replaceMultipleItemAt({ relations: [] }, 0, {})).toEqual({ relations: [] });
  });
});

describe('appendEmptyMultipleItem', () => {
  it('appends {} to array', () => {
    expect(appendEmptyMultipleItem([])).toEqual([{}]);
    expect(appendEmptyMultipleItem([{ x: 1 }])).toEqual([{ x: 1 }, {}]);
  });

  it('appends to relations envelope', () => {
    const raw = { relations: [{ id: 'a' }] };
    expect(appendEmptyMultipleItem(raw)).toEqual({ relations: [{ id: 'a' }, {}] });
  });

  it('wraps non-array non-envelope as array with new empty object', () => {
    expect(appendEmptyMultipleItem({ foo: 1 })).toEqual([{ foo: 1 }, {}]);
  });
});
