import { describe, it, expect } from 'vitest';
import {
  assistantReplyViewFromWire,
  deriveAssistantReplyView,
  structuredReplySectionTitle,
} from '../assistantReplyView';

describe('assistantReplyView', () => {
  it('should derive chart-primary from sql artifact with chart visualizations', () => {
    expect(
      deriveAssistantReplyView([
        {
          kind: 'sql',
          sql: 'SELECT month, revenue FROM sales',
          visualizations: [
            {
              kind: 'chart',
              key: 'main',
              chartType: 'bar',
              encodings: { x: { field: 'month' }, y: { field: 'revenue' } },
            },
          ],
        },
      ]),
    ).toBe('chart-primary');
  });

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
    expect(assistantReplyViewFromWire('chart-primary')).toBe('chart-primary');
    expect(assistantReplyViewFromWire(null)).toBeUndefined();
    expect(assistantReplyViewFromWire('unknown')).toBeUndefined();
  });

  it('should derive facet-primary from multi completion hint', () => {
    expect(
      deriveAssistantReplyView(undefined, {
        presentation: 'structured',
        partType: 'multi',
        structuredPartCount: 2,
        partTypes: ['facet-proposal', 'facet-proposal'],
      }),
    ).toBe('facet-primary');
  });

  it('should pluralize facet section title when multiple facet artefacts', () => {
    const artifacts = [
      {
        kind: 'facet-proposal' as const,
        facetTypeKey: 'descriptive',
        metadataEntityId: 'a',
        payload: {},
      },
      {
        kind: 'facet-proposal' as const,
        facetTypeKey: 'descriptive',
        metadataEntityId: 'b',
        payload: {},
      },
    ];
    expect(structuredReplySectionTitle('facet-primary', artifacts)).toBe('Facet proposals');
    expect(structuredReplySectionTitle('facet-primary', [artifacts[0]!])).toBe('Facet proposal');
  });
});
