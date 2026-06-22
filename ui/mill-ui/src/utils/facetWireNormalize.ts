import type { ChatMessageArtifact } from '../types/chat';

/**
 * Maps schema capture subtypes to metadata facet-type keys for descriptor lookup.
 */
export function facetTypeKeyFromCaptureType(captureType: string): string {
  switch (captureType.trim().toLowerCase()) {
    case 'description':
      return 'descriptive';
    case 'relation':
      return 'relation';
    default:
      return captureType.trim();
  }
}

function payloadFromObject(o: Record<string, unknown>): unknown {
  if ('serializedPayload' in o) return o.serializedPayload;
  if ('payload' in o) return o.payload;
  return o;
}

/**
 * Parses facet-proposal wire or legacy schema-capture-shaped payloads into one artefact kind.
 */
export function parseFacetProposalArtifact(payload: Record<string, unknown>): Extract<ChatMessageArtifact, { kind: 'facet-proposal' }> | null {
  const facetTypeKeyDirect = typeof payload.facetTypeKey === 'string' ? payload.facetTypeKey : '';
  const metadataEntityIdDirect = typeof payload.metadataEntityId === 'string' ? payload.metadataEntityId : '';
  if (facetTypeKeyDirect && metadataEntityIdDirect) {
    return {
      kind: 'facet-proposal',
      facetTypeKey: facetTypeKeyDirect,
      metadataEntityId: metadataEntityIdDirect,
      payload: payloadFromObject(payload),
    };
  }

  const captureType = typeof payload.captureType === 'string' ? payload.captureType : '';
  const targetEntityId = typeof payload.targetEntityId === 'string' ? payload.targetEntityId : '';
  if (!captureType || !targetEntityId) {
    return null;
  }
  if (payload.captureSucceeded === false) {
    return null;
  }

  const facetTypeKey =
    captureType.trim().toLowerCase() === 'facet_assignment'
      ? facetTypeKeyDirect
      : facetTypeKeyFromCaptureType(captureType);
  if (!facetTypeKey) {
    return null;
  }

  return {
    kind: 'facet-proposal',
    facetTypeKey,
    metadataEntityId: targetEntityId,
    payload: payloadFromObject(payload),
  };
}
