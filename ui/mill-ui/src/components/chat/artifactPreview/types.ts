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
  | 'inline-artifact-strip'
  | 'host-apply'
  | 'conversation-card'
  | 'prose-only';

export type ArtifactTransition = 'expand' | 'open-in-analysis' | 'apply-to-host';

export type ArtifactActionId =
  | 'run'
  | 'copy'
  | 'export'
  | 'expand'
  | 'open-in-analysis'
  | 'open-in-model'
  | 'accept'
  | 'reject'
  | 'apply'
  | 'apply-and-run';

export interface ArtifactTreatment {
  mode: ArtifactPresentationMode;
  views?: ('condensed' | 'expanded')[];
  transitions?: ArtifactTransition[];
  actions?: ArtifactActionId[];
}

/** Grouped artefacts rendered as one UI unit (e.g. SQL + optional data). */
export type SqlDataCompositeGroup = {
  kind: 'sql-data-composite';
  sql?: Extract<ChatMessageArtifact, { kind: 'sql' }>;
  data?: Extract<ChatMessageArtifact, { kind: 'data' }>;
};

export type FacetProposalGroup = {
  kind: 'facet-proposal';
  facet: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>;
};

export type ArtifactRenderGroup = SqlDataCompositeGroup | FacetProposalGroup;

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
