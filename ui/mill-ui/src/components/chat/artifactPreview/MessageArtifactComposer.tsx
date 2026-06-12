import { useEffect } from 'react';
import type { ChatMessageArtifact, Message } from '../../../types/chat';
import { ArtifactCard } from '../artifacts/ArtifactCard';
import { applyArtifactToHost } from './hostIntegrations';
import { groupMessageArtifacts } from './artifactGroups';
import { resolveArtifactTreatment } from './chatArtifactTreatments';
import { resolveCardComponent, resolvePreviewComponent } from './registry';
import type { ChatType } from './types';

export interface MessageArtifactComposerProps {
  chatType: ChatType;
  message: Message;
  conversationId: string;
  chatTitle?: string;
  precedingUserQuestion?: string;
  onArtifactsChange?: (artifacts: ChatMessageArtifact[]) => void;
}

export function MessageArtifactComposer({
  chatType,
  message,
  conversationId,
  chatTitle,
  precedingUserQuestion,
  onArtifactsChange,
}: MessageArtifactComposerProps) {
  const groups = groupMessageArtifacts(message.artifacts);
  const passthroughArtifacts = (message.artifacts ?? []).filter(
    (artifact) => artifact.kind === 'schema-capture' || artifact.kind === 'unknown',
  );

  useEffect(() => {
    if (chatType !== 'inline-analysis') return;
    for (const artifact of message.artifacts ?? []) {
      if (artifact.kind === 'sql') {
        applyArtifactToHost(chatType, artifact);
      }
    }
  }, [chatType, message.artifacts]);

  if (!groups.length && !passthroughArtifacts.length) return null;

  return (
    <>
      {passthroughArtifacts.map((artifact, index) => (
        <ArtifactCard key={`${artifact.kind}-${index}`} artifact={artifact} />
      ))}
      {groups.map((group, index) => {
        const treatment = resolveArtifactTreatment(chatType, group.kind);

        if (treatment.mode === 'host-apply') {
          return null;
        }

        if (treatment.mode === 'conversation-card') {
          const Card = resolveCardComponent(group.kind);
          if (!Card) return null;
          return (
            <Card
              key={`${group.kind}-${index}`}
              chatType={chatType}
              message={message}
              group={group}
              conversationId={conversationId}
              chatTitle={chatTitle}
              precedingUserQuestion={precedingUserQuestion}
              onArtifactsChange={onArtifactsChange}
            />
          );
        }

        if (treatment.mode === 'condensed-preview') {
          const Preview = resolvePreviewComponent(group.kind);
          if (!Preview) return null;
          return (
            <Preview
              key={`${group.kind}-${index}`}
              chatType={chatType}
              message={message}
              group={group}
              conversationId={conversationId}
              chatTitle={chatTitle}
              precedingUserQuestion={precedingUserQuestion}
              onArtifactsChange={onArtifactsChange}
            />
          );
        }

        return null;
      })}
    </>
  );
}
