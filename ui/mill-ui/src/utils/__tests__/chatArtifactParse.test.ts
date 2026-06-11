import { describe, it, expect } from 'vitest';
import { parseChatStructuredPart } from '../chatArtifactParse';

describe('parseChatStructuredPart', () => {
  it('should parse sql structured part', () => {
    const a = parseChatStructuredPart({
      type: 'item.part.updated',
      presentation: 'structured',
      partType: 'sql',
      mode: 'replace',
      content: JSON.stringify({ sql: 'SELECT 1', dialectId: 'calcite' }),
    });
    expect(a).toEqual({ kind: 'sql', sql: 'SELECT 1', dialectId: 'calcite' });
  });

  it('should parse facet-proposal with serializedPayload', () => {
    const a = parseChatStructuredPart({
      presentation: 'structured',
      partType: 'facet-proposal',
      content: JSON.stringify({
        facetTypeKey: 'urn:test',
        metadataEntityId: 'ent-1',
        serializedPayload: { note: 'x' },
      }),
    });
    expect(a).toEqual({
      kind: 'facet-proposal',
      facetTypeKey: 'urn:test',
      metadataEntityId: 'ent-1',
      payload: { note: 'x' },
    });
  });

  it('should parse schema-capture structured part', () => {
    const a = parseChatStructuredPart({
      presentation: 'structured',
      partType: 'schema-capture',
      content: JSON.stringify({
        captureType: 'description',
        targetEntityId: 'retail.orders',
        targetEntityType: 'TABLE',
        serializedPayload: { summary: 'Orders table' },
      }),
    });
    expect(a).toEqual({
      kind: 'schema-capture',
      captureType: 'description',
      targetEntityId: 'retail.orders',
      targetEntityType: 'TABLE',
      payload: { summary: 'Orders table' },
    });
  });

  it('should infer schema-capture from payload shape', () => {
    const a = parseChatStructuredPart({
      presentation: 'structured',
      partType: 'text',
      content: JSON.stringify({
        captureType: 'relation',
        targetEntityId: 'retail.orders',
        serializedPayload: { sourceTableId: 'retail.orders' },
      }),
    });
    expect(a?.kind).toBe('schema-capture');
  });

  it('should fallback to unknown artifact for unrecognized structured partType', () => {
    const payload = { artifactType: 'custom-thing', value: 42 };
    const a = parseChatStructuredPart({
      presentation: 'structured',
      partType: 'custom-thing',
      content: JSON.stringify(payload),
    });
    expect(a).toEqual({
      kind: 'unknown',
      partType: 'custom-thing',
      title: 'custom-thing',
      payload,
    });
  });

  it('should return null for conversation text part shape', () => {
    expect(
      parseChatStructuredPart({
        presentation: 'conversation',
        partType: 'text',
        content: 'hello',
      }),
    ).toBeNull();
  });

  it('should infer sql when presentation/discriminators look like V1 but content is artefact JSON', () => {
    const a = parseChatStructuredPart({
      presentation: 'conversation',
      partType: 'text',
      mode: 'replace',
      content: JSON.stringify({ sql: 'SELECT 1', dialectId: 'calcite' }),
    });
    expect(a).toEqual({ kind: 'sql', sql: 'SELECT 1', dialectId: 'calcite' });
  });

  it('should read part_type snake_case', () => {
    const a = parseChatStructuredPart({
      presentation: 'structured',
      part_type: 'facet-proposal',
      content: JSON.stringify({
        facetTypeKey: 'urn:t',
        metadataEntityId: 'e1',
      }),
    });
    expect(a?.kind).toBe('facet-proposal');
  });
});
