import { useCallback, useEffect, useState } from 'react';
import type { ChatMessageArtifact, Message } from '../../../types/chat';
import { chatService } from '../../../services/api';

export interface UseFacetProposalLifecycleParams {
  artifact: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }> | null | undefined;
  conversationId: string;
  message: Message;
  onArtifactsChange?: (artifacts: ChatMessageArtifact[]) => void;
}

/** Shared accept/reject lifecycle for facet proposals (general chat + inline strips). */
export function useFacetProposalLifecycle({
  artifact,
  conversationId,
  message,
  onArtifactsChange,
}: UseFacetProposalLifecycleParams) {
  const [lifecycleBusy, setLifecycleBusy] = useState(false);
  const [localStatus, setLocalStatus] = useState<string | undefined>(artifact?.status);

  const artifactId = artifact?.artifactId;
  const status = localStatus ?? artifact?.status ?? 'active';
  const isActive = status === 'active' || status === 'pending' || status === 'accepted';
  const isRejected = status === 'rejected' || status === 'declined' || status === 'retracted';

  useEffect(() => {
    setLocalStatus(artifact?.status);
  }, [artifact?.artifactId, artifact?.status]);

  const updateFacetStatus = useCallback(
    (nextStatus: string) => {
      setLocalStatus(nextStatus);
      if (!artifact || !onArtifactsChange) return;
      const prev = message.artifacts ?? [];
      onArtifactsChange(
        prev.map((entry) =>
          entry.kind === 'facet-proposal' && entry.artifactId === artifact.artifactId
            ? { ...entry, status: nextStatus }
            : entry,
        ),
      );
    },
    [artifact, message.artifacts, onArtifactsChange],
  );

  const handleAccept = useCallback(async () => {
    if (!artifactId) return;
    setLifecycleBusy(true);
    try {
      const updated = await chatService.acceptArtifact(conversationId, artifactId);
      if (updated?.kind === 'facet-proposal') {
        updateFacetStatus(updated.status ?? 'active');
      } else {
        updateFacetStatus('active');
      }
    } finally {
      setLifecycleBusy(false);
    }
  }, [artifactId, conversationId, updateFacetStatus]);

  const handleReject = useCallback(async () => {
    if (!artifactId) return;
    setLifecycleBusy(true);
    try {
      const ok = await chatService.rejectArtifact(conversationId, artifactId);
      if (ok) {
        updateFacetStatus('rejected');
      }
    } finally {
      setLifecycleBusy(false);
    }
  }, [artifactId, conversationId, updateFacetStatus]);

  return {
    status,
    isActive,
    isRejected,
    lifecycleBusy,
    handleAccept,
    handleReject,
    canReject: isActive && Boolean(artifactId),
    canAccept: isRejected && Boolean(artifactId),
  };
}
