import { Stack } from '@mantine/core';
import { useEffect } from 'react';
import type { Message } from '../../../types/chat';
import { MessageContent } from '../../common/MessageContent';
import { groupMessageArtifacts } from './artifactGroups';
import { applyArtifactToHost } from './hostIntegrations';
import { ArtifactGroupRenderer, renderUnknownArtifacts } from './ArtifactGroupRenderer';
import { buildReplySegments, usesInterleavedArtifactLayout } from '../../../utils/replySegments';
import type { MessageArtifactComposerProps } from './MessageArtifactComposer';
import type { ChatType } from './types';

type InterleavedAssistantReplyProps = MessageArtifactComposerProps & {
  message: Message;
};

function useInlineAnalysisSqlApply(chatType: ChatType, message: Message): void {
  useEffect(() => {
    if (chatType !== 'inline-analysis') return;
    for (const artifact of message.artifacts ?? []) {
      if (artifact.kind === 'sql') {
        applyArtifactToHost(chatType, artifact);
      }
    }
  }, [chatType, message.artifacts]);
}

/** Renders assistant commentary and artefact boxes in conversational order. */
export function InterleavedAssistantReply({
  message,
  chatType,
  ...composerProps
}: InterleavedAssistantReplyProps) {
  useInlineAnalysisSqlApply(chatType, message);
  const segments = buildReplySegments(message);
  const groups = groupMessageArtifacts(message.artifacts);
  const unknownArtifacts = renderUnknownArtifacts(message.artifacts ?? []);

  if (!segments.length && !unknownArtifacts) {
    return message.content.trim() ? <MessageContent content={message.content} /> : null;
  }

  return (
    <Stack gap="sm" style={{ width: '100%' }}>
      {unknownArtifacts}
      {segments.map((segment, index) => {
        if (segment.kind === 'text') {
          return <MessageContent key={`reply-text-${index}`} content={segment.text} />;
        }
        const group = groups[segment.groupIndex];
        if (!group) return null;
        return (
          <ArtifactGroupRenderer
            key={`reply-artifact-${index}-${segment.groupIndex}`}
            chatType={chatType}
            {...composerProps}
            message={message}
            group={group}
            groupKey={`reply-artifact-${index}-${segment.groupIndex}`}
          />
        );
      })}
      {!usesInterleavedArtifactLayout(message) && message.content.trim() ? (
        <MessageContent content={message.content} />
      ) : null}
    </Stack>
  );
}
