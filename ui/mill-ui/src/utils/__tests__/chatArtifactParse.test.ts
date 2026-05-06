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
