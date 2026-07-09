import { Box, Stack } from '@mantine/core';
import type { Message } from '../../../types/chat';
import { MessageContent } from '../../common/MessageContent';
import { groupMessageArtifacts } from './artifactGroups';
import { ArtifactGroupRenderer, renderUnknownArtifacts } from './ArtifactGroupRenderer';
import { buildReplySegments, usesInterleavedArtifactLayout } from '../../../utils/replySegments';
import type { MessageArtifactComposerProps } from './MessageArtifactComposer';
import classes from './InlineArtifactStrip.module.css';

type InterleavedAssistantReplyProps = MessageArtifactComposerProps & {
  message: Message;
};

/** Renders assistant commentary and artefact boxes in conversational order. */
export function InterleavedAssistantReply({
  message,
  chatType,
  ...composerProps
}: InterleavedAssistantReplyProps) {
  const segments = buildReplySegments(message);
  const groups = groupMessageArtifacts(message.artifacts);
  const unknownArtifacts = renderUnknownArtifacts(message.artifacts ?? []);

  const isInlineChat = chatType.startsWith('inline-');

  if (!segments.length && !unknownArtifacts) {
    return message.content.trim() ? <MessageContent content={message.content} /> : null;
  }

  return (
    <Stack gap={isInlineChat ? 'xs' : 'sm'} style={{ width: isInlineChat ? 'fit-content' : '100%', maxWidth: isInlineChat ? 'min(88%, 22rem)' : undefined }}>
      {unknownArtifacts}
      {segments.map((segment, index) => {
        if (segment.kind === 'text') {
          return (
            <Box key={`reply-text-${index}`} style={{ width: isInlineChat ? '100%' : undefined, maxWidth: isInlineChat ? 'min(100%, 26rem)' : undefined }}>
              <MessageContent content={segment.text} />
            </Box>
          );
        }
        const group = groups[segment.groupIndex];
        if (!group) return null;
        const artifactNode = (
          <ArtifactGroupRenderer
            chatType={chatType}
            {...composerProps}
            message={message}
            group={group}
            groupKey={`reply-artifact-${index}-${segment.groupIndex}`}
          />
        );
        if (!isInlineChat) return artifactNode;
        return (
          <Box key={`reply-artifact-wrap-${index}`} className={classes.inlineArtifactReply}>
            {artifactNode}
          </Box>
        );
      })}
      {!usesInterleavedArtifactLayout(message) && message.content.trim() ? (
        <MessageContent content={message.content} />
      ) : null}
    </Stack>
  );
}
