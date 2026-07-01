import type { ChatMessageArtifact } from '../types/chat';
import type { TurnResponseWire } from '../types/chatWire';
import { parseWireArtifacts } from './artifactWireParse';

function normalizeFacetTypeKey(key: string): string {
  const trimmed = key.trim();
  const colon = trimmed.lastIndexOf(':');
  if (colon >= 0 && colon < trimmed.length - 1) {
    return trimmed.slice(colon + 1).toLowerCase();
  }
  return trimmed.toLowerCase();
}

function facetIdentity(
  artifact: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>,
): string {
  const catalogPath = artifact.catalogPath?.trim().toLowerCase() ?? '';
  return `${normalizeFacetTypeKey(artifact.facetTypeKey)}|${artifact.metadataEntityId.trim()}|${catalogPath}`;
}

/**
 * Copies durable `artifactId` and `status` from server replay onto streamed artefacts
 * matched by shape (facet type + entity, or SQL text).
 */
export function mergeArtifactIdsFromServer(
  streamed: readonly ChatMessageArtifact[],
  server: readonly ChatMessageArtifact[],
): ChatMessageArtifact[] {
  if (!streamed.length || !server.length) return [...streamed];

  const facetQueue = server.filter(
    (a): a is Extract<ChatMessageArtifact, { kind: 'facet-proposal' }> => a.kind === 'facet-proposal',
  );
  const usedFacet = new Set<number>();

  return streamed.map((artifact) => {
    if (artifact.artifactId) return artifact;

    if (artifact.kind === 'facet-proposal') {
      const key = facetIdentity(artifact);
      const idx = facetQueue.findIndex((candidate, i) => {
        if (usedFacet.has(i) || !candidate.artifactId) return false;
        return facetIdentity(candidate) === key;
      });
      if (idx < 0) {
        const orderIdx = facetQueue.findIndex((candidate, i) => {
          if (usedFacet.has(i) || !candidate.artifactId) return false;
          return normalizeFacetTypeKey(candidate.facetTypeKey) === normalizeFacetTypeKey(artifact.facetTypeKey);
        });
        if (orderIdx < 0) return artifact;
        usedFacet.add(orderIdx);
        const match = facetQueue[orderIdx]!;
        return {
          ...artifact,
          artifactId: match.artifactId,
          status: match.status ?? artifact.status,
        };
      }
      usedFacet.add(idx);
      const match = facetQueue[idx]!;
      return {
        ...artifact,
        artifactId: match.artifactId,
        status: match.status ?? artifact.status,
      };
    }

    if (artifact.kind === 'sql') {
      const match = server.find(
        (candidate) =>
          candidate.kind === 'sql' &&
          candidate.artifactId &&
          candidate.sql.trim() === artifact.sql.trim(),
      );
      if (match?.kind === 'sql') {
        return {
          ...artifact,
          artifactId: match.artifactId,
          status: match.status ?? artifact.status,
        };
      }
    }

    return artifact;
  });
}

/**
 * Merges durable artifact ids from a persisted assistant turn onto streamed artefacts.
 */
export function mergeStreamedArtifactsWithAssistantTurn(
  streamed: readonly ChatMessageArtifact[],
  assistantTurn: TurnResponseWire | undefined,
): ChatMessageArtifact[] {
  if (!assistantTurn || !streamed.length) return [...streamed];
  return mergeArtifactIdsFromServer(streamed, parseWireArtifacts(assistantTurn.artifacts));
}
