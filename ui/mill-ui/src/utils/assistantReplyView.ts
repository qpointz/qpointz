import type { AssistantReplyView, ChatMessageArtifact } from '../types/chat';
import { chartVisualizationsFromPayload } from '../components/charts/chartData';

/**
 * Parses optional GET-transcript field from [TurnResponseWire.assistantReplyView].
 */
export function assistantReplyViewFromWire(raw: string | null | undefined): AssistantReplyView | undefined {
  if (
    raw === 'conversation' ||
    raw === 'sql-primary' ||
    raw === 'chart-primary' ||
    raw === 'facet-primary' ||
    raw === 'schema-primary' ||
    raw === 'artifact-primary'
  ) {
    return raw;
  }
  return undefined;
}

function sqlArtifactHasChartVisualizations(artifacts: readonly ChatMessageArtifact[]): boolean {
  return artifacts.some(
    (artifact) =>
      artifact.kind === 'sql' &&
      chartVisualizationsFromPayload(artifact.visualizations).length > 0,
  );
}

/**
 * Derives the layout for an assistant message from structured artefacts and/or the end-of-turn
 * `item.completed` summary (presentation/partType) emitted by [ChatRuntimeEventToSseMapper].
 */
export function deriveAssistantReplyView(
  artifacts: readonly ChatMessageArtifact[] | undefined,
  completionHint?: {
    readonly presentation: string;
    readonly partType: string;
    readonly structuredPartCount?: number;
    readonly partTypes?: readonly string[];
  } | null,
): AssistantReplyView {
  const arts = artifacts ?? [];
  if (arts.some((a) => a.kind === 'facet-proposal')) return 'facet-primary';
  if (sqlArtifactHasChartVisualizations(arts)) return 'chart-primary';
  if (arts.some((a) => a.kind === 'sql' || a.kind === 'data')) return 'sql-primary';
  if (arts.some((a) => a.kind === 'unknown')) return 'artifact-primary';
  if (completionHint?.presentation === 'structured') {
    if (completionHint.partType === 'sql') return 'sql-primary';
    if (completionHint.partType === 'facet-proposal') return 'facet-primary';
    if (completionHint.partType === 'schema-capture') return 'facet-primary';
    if (completionHint.partType === 'multi') {
      const types = completionHint.partTypes ?? [];
      if (types.some((t) => t === 'facet-proposal' || t === 'schema-capture')) return 'facet-primary';
      if (types.includes('sql')) return 'sql-primary';
      if (types.length > 0) return 'artifact-primary';
    }
    if (completionHint.partType && completionHint.partType !== 'text') return 'artifact-primary';
  }
  return 'conversation';
}

/** Section label for structured assistant replies. */
export function structuredReplySectionTitle(
  view: AssistantReplyView,
  artifacts?: readonly ChatMessageArtifact[],
): string | null {
  switch (view) {
    case 'sql-primary':
      return 'SQL';
    case 'chart-primary':
      return 'Chart';
    case 'facet-primary': {
      const facetCount = artifacts?.filter((a) => a.kind === 'facet-proposal').length ?? 0;
      return facetCount > 1 ? 'Facet proposals' : 'Facet proposal';
    }
    case 'schema-primary':
      return 'Schema capture';
    case 'artifact-primary':
      return 'Artifact';
    default:
      return null;
  }
}
