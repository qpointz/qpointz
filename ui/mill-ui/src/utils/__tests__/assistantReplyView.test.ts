import { describe, it, expect } from 'vitest';
import {
  assistantReplyViewFromWire,
  deriveAssistantReplyView,
} from '../assistantReplyView';

describe('assistantReplyView', () => {
  it('should derive sql-primary from sql artifact', () => {
    expect(
      deriveAssistantReplyView([{ kind: 'sql', sql: 'SELECT 1' }]),
    ).toBe('sql-primary');
  });

  it('should prefer first facet-proposal when both sql and facet present', () => {
    expect(
      deriveAssistantReplyView([
        { kind: 'sql', sql: 'SELECT 1' },
        {
          kind: 'facet-proposal',
          facetTypeKey: 'urn:t',
          metadataEntityId: 'e1',
          payload: {},
        },
      ]),
    ).toBe('facet-primary');
  });

  it('should derive facet-primary from facet-proposal artifact', () => {
    expect(
      deriveAssistantReplyView([
        {
          kind: 'facet-proposal',
          facetTypeKey: 'descriptive',
          metadataEntityId: 'retail.orders',
          payload: {},
        },
      ]),
    ).toBe('facet-primary');
  });

  it('should derive artifact-primary from unknown artifact', () => {
    expect(
      deriveAssistantReplyView([
        {
          kind: 'unknown',
          partType: 'custom',
          title: 'custom',
          payload: { x: 1 },
        },
      ]),
    ).toBe('artifact-primary');
  });

  it('should use completion hint when no artifacts', () => {
    expect(
      deriveAssistantReplyView(undefined, { presentation: 'structured', partType: 'sql' }),
    ).toBe('sql-primary');
    expect(
      deriveAssistantReplyView(undefined, { presentation: 'structured', partType: 'schema-capture' }),
    ).toBe('facet-primary');
    expect(
      deriveAssistantReplyView(undefined, { presentation: 'structured', partType: 'custom' }),
    ).toBe('artifact-primary');
  });

  it('should parse wire transcript values', () => {
    expect(assistantReplyViewFromWire('sql-primary')).toBe('sql-primary');
    expect(assistantReplyViewFromWire(null)).toBeUndefined();
    expect(assistantReplyViewFromWire('unknown')).toBeUndefined();
  });
});
