import type { ChatMessageArtifact, Message } from '../../../types/chat';
import type { QueryResult } from '../../../types/query';

/** Active chat host — primary key for artefact treatment lookup. */
export type ChatType =
  | 'general'
  | 'inline-analysis'
  | 'inline-model'
  | 'inline-knowledge';

export type ArtefactKind = 'sql-data-composite' | 'facet-proposal';

export type ArtifactPresentationMode =
  | 'condensed-preview'
  | 'host-apply'
  | 'conversation-card'
  | 'prose-only';

export type ArtifactTransition = 'expand' | 'open-in-analysis' | 'apply-to-host';

export type ArtifactActionId = 'run' | 'copy' | 'export' | 'expand' | 'open-in-analysis';
/** Planned facet actions (not in enum until implemented): `promote`, `copy-json`. */

export interface ArtifactTreatment {
  mode: ArtifactPresentationMode;
  views?: ('condensed' | 'expanded')[];
  transitions?: ArtifactTransition[];
  actions?: ArtifactActionId[];
}

/** Grouped artefacts rendered as one UI unit (e.g. SQL + optional data). */
export interface ArtifactRenderGroup {
  kind: ArtefactKind;
  sql?: Extract<ChatMessageArtifact, { kind: 'sql' }>;
  data?: Extract<ChatMessageArtifact, { kind: 'data' }>;
  facet?: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>;
}

export interface ArtifactPreviewContext {
  chatType: ChatType;
  message: Message;
  group: ArtifactRenderGroup;
  /** Preceding user question in the thread (for Open in Analysis handoff). */
  precedingUserQuestion?: string;
  chatTitle?: string;
  conversationId: string;
  onArtifactsChange?: (artifacts: ChatMessageArtifact[]) => void;
}

export interface SqlPreviewState {
  activeTab: 'sql' | 'data';
  result: QueryResult | null;
  error: string | null;
  isExecuting: boolean;
  isPageLoading: boolean;
}
