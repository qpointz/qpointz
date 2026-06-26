import type { AssistantReplySegment, ChatMessageArtifact } from '../types/chat';
import { groupMessageArtifacts } from '../components/chat/artifactPreview/artifactGroups';
import {
  commentaryForArtifactGroup,
  extractTrailingCommentary,
  groupIndexForNewArtifact,
  shouldAppendArtifactSegment,
} from './replySegments';

/**
 * Tracks interleaved text / artefact segments while an assistant reply streams over SSE.
 */
export class StreamingReplySegmentTracker {
  private readonly segments: AssistantReplySegment[] = [];
  private pendingText = '';
  private artifacts: ChatMessageArtifact[] = [];

  /** Append streamed assistant text (full accumulated content or delta). */
  setPendingText(text: string): void {
    this.pendingText = text;
  }

  /** Current artefact list mirrored from message state. */
  currentArtifacts(): readonly ChatMessageArtifact[] {
    return this.artifacts;
  }

  /** Segments built so far (copy). */
  snapshot(): readonly AssistantReplySegment[] {
    return [...this.segments];
  }

  private flushPendingText(): void {
    const trimmed = this.pendingText.trim();
    if (trimmed) {
      this.segments.push({ kind: 'text', text: trimmed });
      this.pendingText = '';
    }
  }

  /**
   * Call when a structured artefact arrives. Returns updated segments when the layout changes.
   */
  onArtifact(artifact: ChatMessageArtifact): readonly AssistantReplySegment[] {
    const before = this.artifacts;
    const appendSegment = shouldAppendArtifactSegment(artifact, before);
    if (appendSegment) {
      const trimmedPending = this.pendingText.trim();
      if (trimmedPending) {
        this.flushPendingText();
      } else {
        this.artifacts = [...before, artifact];
        const groups = groupMessageArtifacts(this.artifacts);
        const group = groups[groups.length - 1];
        if (group) {
          const commentary = commentaryForArtifactGroup(group);
          if (commentary) {
            this.segments.push({ kind: 'text', text: commentary });
          }
        }
        this.artifacts = before;
      }
    }
    this.artifacts = [...before, artifact];
    if (appendSegment) {
      const groupIndex = groupIndexForNewArtifact(artifact, this.artifacts);
      this.segments.push({ kind: 'artifact', groupIndex });
    }
    return this.snapshot();
  }

  /** Call when the text stream completes. */
  finalize(): readonly AssistantReplySegment[] {
    const trimmed = this.pendingText.trim();
    if (trimmed) {
      const artifactGroupCount = this.segments.filter((segment) => segment.kind === 'artifact').length;
      const trailing = extractTrailingCommentary(trimmed, artifactGroupCount) ?? trimmed;
      if (trailing.trim()) {
        this.segments.push({ kind: 'text', text: trailing.trim() });
      }
      this.pendingText = '';
    }
    return this.snapshot();
  }
}
