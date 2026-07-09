import type { ChatMessageArtifact, Message } from '../../../types/chat';
import { groupMessageArtifacts } from './artifactGroups';
import { ArtifactGroupRenderer, renderUnknownArtifacts } from './ArtifactGroupRenderer';
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

  if (!groups.length && !(message.artifacts ?? []).some((a) => a.kind === 'unknown')) {
    return null;
  }

  return (
    <>
      {renderUnknownArtifacts(message.artifacts ?? [])}
      {groups.map((group, index) => (
        <ArtifactGroupRenderer
          key={`${group.kind}-${index}`}
          chatType={chatType}
          message={message}
          group={group}
          groupKey={`${group.kind}-${index}`}
          conversationId={conversationId}
          chatTitle={chatTitle}
          precedingUserQuestion={precedingUserQuestion}
          onArtifactsChange={onArtifactsChange}
        />
      ))}
    </>
  );
}
