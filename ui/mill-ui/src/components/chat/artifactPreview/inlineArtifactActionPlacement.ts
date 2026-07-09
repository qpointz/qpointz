import type { ArtefactKind, ArtifactActionId } from './types';

const STRIP_ACTIONS: Record<ArtefactKind, ArtifactActionId[]> = {
  'sql-data-composite': ['apply', 'apply-and-run'],
  'facet-proposal': ['accept', 'reject', 'open-in-model'],
};

const POPOVER_ACTIONS: Record<ArtefactKind, ArtifactActionId[]> = {
  'sql-data-composite': ['copy'],
  'facet-proposal': ['copy'],
};

/** Host/copilot icon actions shown on the inline pill row. */
export function stripActionsForInline(
  kind: ArtefactKind,
  treatmentActions: ArtifactActionId[],
): ArtifactActionId[] {
  return STRIP_ACTIONS[kind].filter((id) => treatmentActions.includes(id));
}

/** Secondary/view actions shown inside the popover. */
export function popoverActionsForInline(
  kind: ArtefactKind,
  treatmentActions: ArtifactActionId[],
): ArtifactActionId[] {
  return POPOVER_ACTIONS[kind].filter((id) => treatmentActions.includes(id));
}
