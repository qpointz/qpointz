import type { ArtefactKind, ArtifactTreatment, ChatType } from './types';

export type ChatArtifactTreatmentRegistry = Record<
  ChatType,
  Partial<Record<ArtefactKind, ArtifactTreatment>>
>;

const SQL_COMPOSITE_GENERAL: ArtifactTreatment = {
  mode: 'condensed-preview',
  views: ['condensed', 'expanded'],
  transitions: ['expand', 'open-in-analysis'],
  actions: ['run', 'copy', 'export', 'expand', 'open-in-analysis'],
};

/** Tabbed facet shell (Facet + JSON tabs) — metadata authoring surfaces. */
const FACET_CONDENSED: ArtifactTreatment = {
  mode: 'condensed-preview',
  views: ['condensed'],
  actions: ['copy', 'open-in-model', 'reject', 'accept'],
};

/** v1 treatment matrix — chat type is primary, artefact kind is secondary. */
export const chatArtifactTreatments: ChatArtifactTreatmentRegistry = {
  general: {
    'sql-data-composite': SQL_COMPOSITE_GENERAL,
    'facet-proposal': FACET_CONDENSED,
  },
  'inline-analysis': {
    'sql-data-composite': {
      mode: 'inline-artifact-strip',
      actions: ['apply', 'apply-and-run', 'copy'],
    },
    'facet-proposal': {
      mode: 'inline-artifact-strip',
      actions: ['copy', 'open-in-model', 'reject', 'accept'],
    },
  },
  'inline-model': {
    'sql-data-composite': {
      mode: 'condensed-preview',
      views: ['condensed'],
      actions: ['copy'],
    },
    'facet-proposal': {
      mode: 'conversation-card',
      actions: [],
    },
  },
  'inline-knowledge': {
    'sql-data-composite': {
      mode: 'condensed-preview',
      views: ['condensed'],
      actions: ['copy'],
    },
    'facet-proposal': {
      mode: 'conversation-card',
      actions: [],
    },
  },
};

export function resolveArtifactTreatment(
  chatType: ChatType,
  kind: ArtefactKind,
): ArtifactTreatment {
  return (
    chatArtifactTreatments[chatType]?.[kind] ?? {
      mode: 'prose-only',
      actions: [],
    }
  );
}

export function treatmentAllowsExpand(chatType: ChatType, kind: ArtefactKind): boolean {
  const treatment = resolveArtifactTreatment(chatType, kind);
  return treatment.transitions?.includes('expand') ?? false;
}
