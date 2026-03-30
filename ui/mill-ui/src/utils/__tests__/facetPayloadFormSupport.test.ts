import { describe, expect, it } from 'vitest';
import type { FacetPayloadSchema } from '../../types/facetTypes';
import {
  effectiveFacetPayloadSchemaForEdit,
  facetPayloadSchemaFormSupported,
  facetTypeContentSchemaRequiresExpertMode,
} from '../facetPayloadFormSupport';

const leaf = (type: FacetPayloadSchema['type'], extra: Partial<FacetPayloadSchema> = {}): FacetPayloadSchema =>
  ({ type, title: 't', description: 'd', ...extra } as FacetPayloadSchema);

describe('facetPayloadSchemaFormSupported', () => {
  it('accepts nested objects of scalars', () => {
    const schema = leaf('OBJECT', {
      fields: [
        { name: 'a', schema: leaf('STRING'), required: true },
        {
          name: 'b',
          schema: leaf('OBJECT', {
            fields: [{ name: 'c', schema: leaf('NUMBER'), required: true }],
          }),
          required: true,
        },
      ],
    });
    expect(facetPayloadSchemaFormSupported(schema)).toBe(true);
  });

  it('accepts array of string, enum, number', () => {
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', { items: leaf('STRING') })
      )
    ).toBe(true);
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', {
          items: leaf('ENUM', { values: [{ value: 'x', description: 'X' }] }),
        })
      )
    ).toBe(true);
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', { items: leaf('NUMBER') })
      )
    ).toBe(true);
  });

  it('wraps MULTIPLE whole facet as ARRAY for edit schema', () => {
    const inner = leaf('OBJECT', {
      fields: [{ name: 'id', schema: leaf('STRING'), required: true }],
    });
    const wrapped = effectiveFacetPayloadSchemaForEdit(
      { targetCardinality: 'MULTIPLE', payload: inner },
      null
    );
    expect(wrapped?.type).toBe('ARRAY');
    expect(wrapped && wrapped.type === 'ARRAY' && wrapped.items).toEqual(inner);
  });

  it('accepts array of object when inner object is form-supported', () => {
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', {
          items: leaf('OBJECT', {
            fields: [{ name: 'id', schema: leaf('STRING'), required: true }],
          }),
        })
      )
    ).toBe(true);
  });

  it('rejects array without items or array of unsupported item types', () => {
    expect(facetPayloadSchemaFormSupported(leaf('ARRAY'))).toBe(false);
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', { items: leaf('ARRAY', { items: leaf('STRING') }) })
      )
    ).toBe(false);
    expect(
      facetPayloadSchemaFormSupported(
        leaf('ARRAY', {
          items: leaf('OBJECT', {
            fields: [
              {
                name: 'flags',
                schema: leaf('ARRAY', { items: leaf('BOOLEAN') }),
                required: true,
              },
            ],
          }),
        })
      )
    ).toBe(false);
  });
});

describe('facetTypeContentSchemaRequiresExpertMode', () => {
  it('is false for object of scalars and array of string', () => {
    expect(
      facetTypeContentSchemaRequiresExpertMode(
        leaf('OBJECT', {
          fields: [
            { name: 'a', schema: leaf('STRING'), required: true },
            { name: 't', schema: leaf('ARRAY', { items: leaf('STRING') }), required: false },
          ],
        })
      )
    ).toBe(false);
  });

  it('is true for array of object or nested array', () => {
    expect(
      facetTypeContentSchemaRequiresExpertMode(
        leaf('OBJECT', {
          fields: [
            {
              name: 'links',
              schema: leaf('ARRAY', {
                items: leaf('OBJECT', { fields: [{ name: 'href', schema: leaf('STRING'), required: true }] }),
              }),
              required: false,
            },
          ],
        })
      )
    ).toBe(true);
    expect(facetTypeContentSchemaRequiresExpertMode(leaf('ARRAY', { items: leaf('ARRAY', { items: leaf('STRING') }) }))).toBe(
      true
    );
    expect(facetTypeContentSchemaRequiresExpertMode(leaf('ARRAY'))).toBe(true);
  });
});
