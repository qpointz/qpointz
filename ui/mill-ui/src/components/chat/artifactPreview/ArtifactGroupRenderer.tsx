import type { ReactNode } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import { ArtifactCard } from '../artifacts/ArtifactCard';
import type { ArtifactRenderGroup } from './types';
import { resolveArtifactTreatment } from './chatArtifactTreatments';
import { resolveCardComponent, resolvePreviewComponent } from './registry';
import type { ChatType } from './types';
import type { MessageArtifactComposerProps } from './MessageArtifactComposer';

export type ArtifactGroupRendererProps = Pick<
  MessageArtifactComposerProps,
  'chatType' | 'message' | 'conversationId' | 'chatTitle' | 'precedingUserQuestion' | 'onArtifactsChange'
> & {
  group: ArtifactRenderGroup;
  groupKey: string;
};

/** Renders one collapsed artefact group (facet, SQL composite, etc.). */
export function ArtifactGroupRenderer({
  chatType,
  message,
  conversationId,
  chatTitle,
  precedingUserQuestion,
  onArtifactsChange,
  group,
  groupKey,
}: ArtifactGroupRendererProps) {
  const treatment = resolveArtifactTreatment(chatType, group.kind);

  if (treatment.mode === 'host-apply') {
    return null;
  }

  if (treatment.mode === 'conversation-card') {
    const Card = resolveCardComponent(group.kind);
    if (!Card) return null;
    return (
      <Card
        key={groupKey}
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
        key={groupKey}
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
}

export function renderUnknownArtifacts(artifacts: readonly ChatMessageArtifact[]): ReactNode {
  const passthrough = artifacts.filter((artifact) => artifact.kind === 'unknown');
  return passthrough.map((artifact, index) => (
    <ArtifactCard key={`${artifact.kind}-${index}`} artifact={artifact} />
  ));
}
