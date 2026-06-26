import type { ComponentType } from 'react';
import { FacetProposalArtifactCard } from '../artifacts/FacetProposalArtifactCard';
import { FacetCondensedPreview } from './FacetCondensedPreview';
import { SqlDataCondensedPreview } from './SqlDataCondensedPreview';
import type { ArtifactPreviewContext, ArtefactKind } from './types';

type PreviewComponent = ComponentType<ArtifactPreviewContext>;

const previewRegistry: Partial<Record<ArtefactKind, PreviewComponent>> = {
  'sql-data-composite': SqlDataCondensedPreview,
  'facet-proposal': FacetCondensedPreview,
};

export function resolvePreviewComponent(kind: ArtefactKind): PreviewComponent | null {
  return previewRegistry[kind] ?? null;
}

export function FacetCardPreview({ group }: ArtifactPreviewContext) {
  if (group.kind !== 'facet-proposal') return null;
  return <FacetProposalArtifactCard artifact={group.facet} />;
}

export function resolveCardComponent(kind: ArtefactKind): PreviewComponent | null {
  if (kind === 'facet-proposal') return FacetCardPreview;
  return null;
}
