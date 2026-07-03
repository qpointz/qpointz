import { describe, expect, it } from 'vitest';
import type { FacetTypeManifest } from '../../types/facetTypes';
import { facetTypeManifestToJsonSchema } from '../facetJsonSchema';

describe('facetTypeManifestToJsonSchema', () => {
  it('generates JSON Schema from facet payload schema', () => {
    const manifest: FacetTypeManifest = {
      typeKey: 'urn:mill/metadata/facet-type:governance',
      title: 'Governance',
      description: 'Governance metadata',
      enabled: true,
      mandatory: false,
      targetCardinality: 'MULTIPLE',
      applicableTo: ['urn:mill/metadata/entity-type:table'],
      category: 'general',
      schemaVersion: '1.0',
      payload: {
        type: 'OBJECT',
        title: 'Governance payload',
        description: 'Governance fields',
        fields: [
          {
            name: 'owner',
            required: true,
            stereotype: 'email',
            schema: {
              type: 'STRING',
              title: 'Owner',
              description: 'Owner email',
              format: 'email',
            },
          },
          {
            name: 'status',
            required: false,
            schema: {
              type: 'ENUM',
              title: 'Status',
              description: 'Review status',
              default: 'draft',
              values: [
                { value: 'draft', description: 'Draft' },
                { value: 'approved', description: 'Approved' },
              ],
            },
          },
        ],
      },
    };

    const schema = facetTypeManifestToJsonSchema(manifest);

    expect(schema.$schema).toBe('http://json-schema.org/draft-07/schema#');
    expect(schema['x-mill-facetTypeUrn']).toBe(manifest.typeKey);
    expect(schema['x-mill-targetCardinality']).toBe('MULTIPLE');
    expect(schema.required).toEqual(['owner']);
    expect(schema.properties).toMatchObject({
      owner: {
        type: 'string',
        format: 'email',
        'x-mill-stereotype': ['email'],
      },
      status: {
        type: 'string',
        enum: ['draft', 'approved'],
        default: 'draft',
      },
    });
  });
});
