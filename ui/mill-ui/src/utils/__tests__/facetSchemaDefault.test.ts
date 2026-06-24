import { describe, expect, it } from 'vitest';
import type { FacetPayloadSchema } from '../../types/facetTypes';
import {
  applyFacetPayloadSchemaDefaults,
  facetFieldEffectiveValue,
  facetSchemaDefaultDisplay,
  facetSchemaDefaultFromInput,
  facetSchemaSupportsDefault,
} from '../facetSchemaDefault';

const leaf = (type: FacetPayloadSchema['type'], extra: Partial<FacetPayloadSchema> = {}): FacetPayloadSchema =>
  ({ type, title: 't', description: 'd', ...extra } as FacetPayloadSchema);

describe('facetSchemaDefault', () => {
  it('supports defaults on scalar schema nodes only', () => {
    expect(facetSchemaSupportsDefault(leaf('STRING'))).toBe(true);
    expect(facetSchemaSupportsDefault(leaf('ENUM', { values: [{ value: 'a', description: 'A' }] }))).toBe(true);
    expect(facetSchemaSupportsDefault(leaf('OBJECT', { fields: [] }))).toBe(false);
    expect(facetSchemaSupportsDefault(leaf('ARRAY', { items: leaf('STRING') }))).toBe(false);
  });

  it('round-trips boolean default display', () => {
    expect(facetSchemaDefaultDisplay(leaf('BOOLEAN', { default: true }))).toBe('true');
    expect(facetSchemaDefaultFromInput(leaf('BOOLEAN'), 'true')).toBe(true);
    expect(facetSchemaDefaultFromInput(leaf('BOOLEAN'), '')).toBeUndefined();
  });

  it('round-trips enum default when value is declared', () => {
    const schema = leaf('ENUM', {
      values: [
        { value: 'sql_like', description: 'LIKE' },
        { value: 'sql_regex', description: 'Regex' },
      ],
      default: 'sql_like',
    });
    expect(facetSchemaDefaultDisplay(schema)).toBe('sql_like');
    expect(facetSchemaDefaultFromInput(schema, 'sql_regex')).toBe('sql_regex');
    expect(facetSchemaDefaultFromInput(schema, 'unknown')).toBeUndefined();
  });

  it('uses schema default for missing boolean and enum fields', () => {
    const enabled = leaf('BOOLEAN', { default: true });
    expect(facetFieldEffectiveValue(enabled, undefined)).toBe(true);
    expect(facetFieldEffectiveValue(enabled, false)).toBe(false);

    const dialect = leaf('ENUM', {
      values: [{ value: 'sql_like', description: 'LIKE' }],
      default: 'sql_like',
    });
    expect(facetFieldEffectiveValue(dialect, undefined)).toBe('sql_like');
  });

  it('applyFacetPayloadSchemaDefaults fills missing scalar defaults on object payloads', () => {
    const schema = leaf('OBJECT', {
      fields: [
        { name: 'enabled', schema: leaf('BOOLEAN', { default: true }), required: false },
        { name: 'name', schema: leaf('STRING'), required: true },
      ],
    } as FacetPayloadSchema);
    expect(applyFacetPayloadSchemaDefaults(schema, {})).toEqual({ enabled: true });
    expect(applyFacetPayloadSchemaDefaults(schema, { enabled: false })).toEqual({ enabled: false });
  });
});
