import { describe, expect, it } from 'vitest';
import { parseArtifactWire, parseWireArtifacts } from '../artifactWireParse';

describe('artifactWireParse', () => {
  it('should parse sql wire artefact', () => {
    expect(
      parseArtifactWire({
        kind: 'sql',
        payload: { sql: 'SELECT 1', dialectId: 'ansi' },
      }),
    ).toEqual({ kind: 'sql', sql: 'SELECT 1', dialectId: 'ansi' });
  });

  it('should parse data wire artefact', () => {
    expect(
      parseArtifactWire({
        kind: 'data',
        payload: { executionId: 'exec-1', rowCount: 5 },
      }),
    ).toEqual({ kind: 'data', executionId: 'exec-1', rowCount: 5 });
  });

  it('should parse facet-proposal wire artefact for GET replay', () => {
    expect(
      parseArtifactWire({
        kind: 'facet-proposal',
        payload: {
          facetTypeKey: 'descriptive',
          metadataEntityId: 'sales.customers',
          payload: { summary: 'VIP customer segment' },
        },
      }),
    ).toEqual({
      kind: 'facet-proposal',
      facetTypeKey: 'descriptive',
      metadataEntityId: 'sales.customers',
      payload: { summary: 'VIP customer segment' },
    });
  });

  it('should normalize legacy schema-capture wire kind to facet-proposal', () => {
    expect(
      parseArtifactWire({
        kind: 'schema-capture',
        payload: {
          captureType: 'description',
          targetEntityId: 'skymill.passenger',
          serializedPayload: { summary: 'Passenger manifest' },
        },
      }),
    ).toEqual({
      kind: 'facet-proposal',
      facetTypeKey: 'descriptive',
      metadataEntityId: 'skymill.passenger',
      payload: { summary: 'Passenger manifest' },
    });
  });

  it('should parse wire list for GET replay', () => {
    const artifacts = parseWireArtifacts([
      { kind: 'sql', payload: { sql: 'SELECT 1' } },
      { kind: 'data', payload: { executionId: 'e1' } },
    ]);
    expect(artifacts).toHaveLength(2);
    expect(artifacts[0]?.kind).toBe('sql');
    expect(artifacts[1]?.kind).toBe('data');
  });
});
