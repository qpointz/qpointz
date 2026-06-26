import type { Message } from '../../../types/chat';
import { InterleavedAssistantReply } from './InterleavedAssistantReply';
import { usesInterleavedArtifactLayout } from '../../../utils/replySegments';
import { MessageContent } from '../../common/MessageContent';
import { MessageArtifactComposer, type MessageArtifactComposerProps } from './MessageArtifactComposer';

type ArtifactPreviewRouterProps = MessageArtifactComposerProps & {
  message: Message;
};

/** Routes assistant reply layout and artefact treatment by chat type. */
export function ArtifactPreviewRouter({
  message,
  ...composerProps
}: ArtifactPreviewRouterProps) {
  if (usesInterleavedArtifactLayout(message)) {
    return <InterleavedAssistantReply message={message} {...composerProps} />;
  }

  return (
    <>
      {message.content.trim() ? <MessageContent content={message.content} /> : null}
      <MessageArtifactComposer message={message} {...composerProps} />
    </>
  );
}
