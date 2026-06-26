import { describe, expect, it } from 'vitest';
import type { ChatMessageArtifact } from '../../types/chat';
import {
  buildReplySegments,
  commentaryForArtifactGroup,
  extractTrailingCommentary,
  groupIndexForNewArtifact,
  shouldAppendArtifactSegment,
} from '../replySegments';
import { StreamingReplySegmentTracker } from '../streamingReplySegments';

describe('replySegments', () => {
  const facetOne: ChatMessageArtifact = {
    kind: 'facet-proposal',
    facetTypeKey: 'descriptive',
    metadataEntityId: 'urn:mill:entity:skymill.passenger.id',
    catalogPath: 'skymill.passenger.id',
    rationale: 'Unique passenger identifier.',
    payload: { description: 'unique passenger identifier' },
  };

  const facetTwo: ChatMessageArtifact = {
    kind: 'facet-proposal',
    facetTypeKey: 'descriptive',
    metadataEntityId: 'urn:mill:entity:skymill.passenger.first_name',
    catalogPath: 'skymill.passenger.first_name',
    rationale: 'Name on ticket.',
    payload: { description: 'name on ticket' },
  };

  it('shouldInterleaveFacetCommentaryAndBoxes_whenDerivedFromArtifacts', () => {
    const message = {
      id: 'm1',
      conversationId: 'c1',
      role: 'assistant' as const,
      timestamp: 1,
      content:
        'I successfully assigned the following facets:\n\n' +
        'For skymill.passenger.id: descriptive facet...\n\n' +
        'The data quality check for skymill.passenger.id failed because a required field is missing. Would you like to correct this?',
      artifacts: [facetOne, facetTwo],
    };

    const segments = buildReplySegments(message);
    expect(segments).toEqual([
      { kind: 'text', text: 'Unique passenger identifier.' },
      { kind: 'artifact', groupIndex: 0 },
      { kind: 'text', text: 'Name on ticket.' },
      { kind: 'artifact', groupIndex: 1 },
      {
        kind: 'text',
        text: 'The data quality check for skymill.passenger.id failed because a required field is missing. Would you like to correct this?',
      },
    ]);
  });

  it('shouldPreferStoredReplySegments_whenPresent', () => {
    const segments = buildReplySegments({
      id: 'm1',
      conversationId: 'c1',
      role: 'assistant',
      timestamp: 1,
      content: 'ignored',
      artifacts: [facetOne],
      replySegments: [
        { kind: 'text', text: 'Intro before box.' },
        { kind: 'artifact', groupIndex: 0 },
      ],
    });
    expect(segments).toHaveLength(2);
    expect(segments[0]).toEqual({ kind: 'text', text: 'Intro before box.' });
  });

  it('shouldBuildFacetCommentaryFromPayload_whenRationaleMissing', () => {
    const commentary = commentaryForArtifactGroup({
      kind: 'facet-proposal',
      facet: {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'urn:mill:entity:skymill.passenger.id',
        catalogPath: 'skymill.passenger.id',
        payload: { description: 'unique passenger identifier' },
      },
    });
    expect(commentary).toContain('skymill.passenger.id');
    expect(commentary).toContain('unique passenger identifier');
  });

  it('shouldExtractQuestionTail_whenBulkSummaryPresent', () => {
    const trailing = extractTrailingCommentary(
      'I successfully assigned facets for id and name.\n\nWould you like to correct the DQ payload?',
      2,
    );
    expect(trailing).toBe('Would you like to correct the DQ payload?');
  });

  it('shouldNotAddSegment_whenDataMergesIntoSqlGroup', () => {
    const sql: ChatMessageArtifact = { kind: 'sql', sql: 'SELECT 1', artifactId: 'sql-1' };
    const data: ChatMessageArtifact = {
      kind: 'data',
      executionId: 'exec-1',
      sql: 'SELECT 1',
      sourceArtifactId: 'sql-1',
    };
    expect(shouldAppendArtifactSegment(sql, [])).toBe(true);
    expect(shouldAppendArtifactSegment(data, [sql])).toBe(false);
    expect(groupIndexForNewArtifact(data, [sql, data])).toBe(0);
  });
});

describe('StreamingReplySegmentTracker', () => {
  it('shouldFlushTextBeforeArtifactAndKeepTrailingText', () => {
    const tracker = new StreamingReplySegmentTracker();
    tracker.setPendingText('Assigned descriptive facet for id.');
    const facet: ChatMessageArtifact = {
      kind: 'facet-proposal',
      facetTypeKey: 'descriptive',
      metadataEntityId: 'urn:mill:entity:skymill.passenger.id',
      catalogPath: 'skymill.passenger.id',
      payload: {},
    };
    const mid = tracker.onArtifact(facet);
    expect(mid).toEqual([
      { kind: 'text', text: 'Assigned descriptive facet for id.' },
      { kind: 'artifact', groupIndex: 0 },
    ]);

    tracker.setPendingText('Would you like to fix the DQ check?');
    const finalSegments = tracker.finalize();
    expect(finalSegments).toEqual([
      { kind: 'text', text: 'Assigned descriptive facet for id.' },
      { kind: 'artifact', groupIndex: 0 },
      { kind: 'text', text: 'Would you like to fix the DQ check?' },
    ]);
  });

  it('shouldInjectCommentary_whenArtifactsArriveWithoutLeadInText', () => {
    const tracker = new StreamingReplySegmentTracker();
    const facet: ChatMessageArtifact = {
      kind: 'facet-proposal',
      facetTypeKey: 'descriptive',
      metadataEntityId: 'urn:mill:entity:skymill.passenger.id',
      catalogPath: 'skymill.passenger.id',
      rationale: 'Unique passenger identifier.',
      payload: {},
    };
    const segments = tracker.onArtifact(facet);
    expect(segments).toEqual([
      { kind: 'text', text: 'Unique passenger identifier.' },
      { kind: 'artifact', groupIndex: 0 },
    ]);
  });
});
